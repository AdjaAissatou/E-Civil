package sn.etatcivil.demande.domaine;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Variété de document délivrable (Extrait d'acte de naissance, Certificat de célibat…), rattachée à une nature.
 * Elle fixe ce que contient le document (modèle), l'acte sur lequel il s'appuie, les rubriques du formulaire,
 * le tarif en francs CFA, le délai, la durée de validité et les pièces à fournir. Une copie désigne son original.
 */
public class TypeDocument {

    /** Montant minimal accepté par PayDunya. */
    public static final int TARIF_MINIMAL = 200;

    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{2,59}");

    private final Long id;
    private final String code;
    private String libelle;
    private String description;
    private Long natureId;
    private String libelleNature;
    private TypeActe typeActe;
    private ModeleDocument modele;
    private OrigineActe origine;
    private String typeMention;
    private Set<Champ> champs;
    private int tarif;
    private int delaiJours;
    private Integer dureeValiditeJours;
    private String motsCles;
    private String piecesAFournir;
    private boolean estOriginal;
    private Long copieDeId;
    private boolean actif;

    /** Reconstitution depuis le stockage. */
    public TypeDocument(Long id, String code, String libelle, String description, Long natureId, String libelleNature,
                        TypeActe typeActe, ModeleDocument modele, OrigineActe origine, String typeMention, Set<Champ> champs,
                        int tarif, int delaiJours, Integer dureeValiditeJours, String motsCles, String piecesAFournir,
                        boolean estOriginal, Long copieDeId, boolean actif) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.natureId = natureId;
        this.libelleNature = libelleNature;
        this.typeActe = typeActe;
        this.modele = modele;
        this.origine = origine;
        this.typeMention = typeMention;
        this.champs = champs == null || champs.isEmpty() ? EnumSet.noneOf(Champ.class) : EnumSet.copyOf(champs);
        this.tarif = tarif;
        this.delaiJours = delaiJours;
        this.dureeValiditeJours = dureeValiditeJours;
        this.motsCles = motsCles;
        this.piecesAFournir = piecesAFournir;
        this.estOriginal = estOriginal;
        this.copieDeId = copieDeId;
        this.actif = actif;
    }

    /** @param origine origine exigée de l'acte (jugement supplétif, transcription), ou {@code null} : toute origine */
    public TypeDocument(Long id, String code, String libelle, String description, TypeActe typeActe,
                        ModeleDocument modele, OrigineActe origine, int tarif, int delaiJours, boolean actif) {
        this(id, code, libelle, description, null, null, typeActe, modele, origine, null, champsParDefaut(typeActe),
                tarif, delaiJours, null, null, null, false, null, actif);
    }

    /** Caractéristiques modifiables d'une variété. */
    public record Caracteristiques(String libelle, String description, Long natureId, TypeActe typeActe,
                                   ModeleDocument modele, OrigineActe origine, String typeMention, Set<Champ> champs,
                                   int tarif, int delaiJours, Integer dureeValiditeJours, String motsCles,
                                   String piecesAFournir, boolean estOriginal, Long copieDeId, boolean actif) {}

    public static TypeDocument creer(String code, Caracteristiques c) {
        String k = code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (!CODE.matcher(k).matches()) {
            throw new RegleMetierException("Le code du document : 3 à 60 lettres majuscules, chiffres ou « _ » (ex. EXTRAIT_NAISSANCE)");
        }
        TypeDocument t = new TypeDocument(null, k, "", null, null, null, TypeActe.NAISSANCE, ModeleDocument.EXTRAIT,
                null, null, Set.of(), TARIF_MINIMAL, 1, null, null, null, false, null, true);
        t.modifier(c);
        return t;
    }

    /** Un changement de tarif ne s'applique qu'aux nouvelles demandes : le montant est figé au dépôt. */
    public void modifier(int tarif, int delaiJours, boolean actif) {
        verifierTarifEtDelai(tarif, delaiJours);
        this.tarif = tarif;
        this.delaiJours = delaiJours;
        this.actif = actif;
    }

    /** Modification complète ; les demandes déjà déposées gardent ce qu'elles ont copié au dépôt. */
    public void modifier(Caracteristiques c) {
        if (c.libelle() == null || c.libelle().isBlank()) throw new RegleMetierException("Le libellé du document est obligatoire");
        if (c.libelle().trim().length() > 150) throw new RegleMetierException("Le libellé ne doit pas dépasser 150 caractères");
        if (c.natureId() == null) throw new RegleMetierException("Choisissez la nature du document");
        if (c.modele() == null || c.typeActe() == null) throw new RegleMetierException("Le modèle et le type d'acte sont obligatoires");
        TypeActe impose = c.modele().typeActeImpose();
        if (impose != null && c.typeActe() != impose) {
            throw new RegleMetierException("Un document « " + c.modele().libelle() + " » s'appuie sur un acte de "
                    + impose.libelle().toLowerCase(Locale.ROOT));
        }
        if (c.modele() == ModeleDocument.EXTRAIT_MENTION && (c.typeMention() == null || c.typeMention().isBlank())) {
            throw new RegleMetierException("Indiquez la mention marginale que ce document reproduit");
        }
        if (c.modele() != ModeleDocument.EXTRAIT_MENTION && c.typeMention() != null && !c.typeMention().isBlank()) {
            throw new RegleMetierException("Seul un extrait de mention marginale désigne une mention");
        }
        if (c.origine() != null && !c.modele().exigeActe()) {
            throw new RegleMetierException("Un document établi sur comparution ne s'appuie sur aucun acte : pas d'origine exigée");
        }
        verifierTarifEtDelai(c.tarif(), c.delaiJours());
        if (c.dureeValiditeJours() != null && (c.dureeValiditeJours() < 1 || c.dureeValiditeJours() > 3650)) {
            throw new RegleMetierException("La durée de validité doit être comprise entre 1 et 3650 jours (ou illimitée)");
        }
        if (c.estOriginal() && c.copieDeId() != null) throw new RegleMetierException("Un original n'est la copie d'aucun document");
        if (c.copieDeId() != null && c.copieDeId().equals(id)) throw new RegleMetierException("Un document ne peut pas être sa propre copie");
        if (c.motsCles() != null && c.motsCles().length() > 255) throw new RegleMetierException("Les mots-clés ne doivent pas dépasser 255 caractères");
        if (c.piecesAFournir() != null && c.piecesAFournir().length() > 1000) {
            throw new RegleMetierException("Les pièces à fournir ne doivent pas dépasser 1000 caractères");
        }
        this.libelle = c.libelle().trim();
        this.description = vide(c.description()) ? null : c.description().trim();
        this.natureId = c.natureId();
        this.typeActe = c.typeActe();
        this.modele = c.modele();
        this.origine = c.origine();
        this.typeMention = vide(c.typeMention()) ? null : c.typeMention().trim();
        this.champs = c.champs() == null || c.champs().isEmpty() ? EnumSet.noneOf(Champ.class) : EnumSet.copyOf(c.champs());
        this.tarif = c.tarif();
        this.delaiJours = c.delaiJours();
        this.dureeValiditeJours = c.dureeValiditeJours();
        this.motsCles = vide(c.motsCles()) ? null : c.motsCles().trim();
        this.piecesAFournir = vide(c.piecesAFournir()) ? null : c.piecesAFournir().trim();
        this.estOriginal = c.estOriginal();
        this.copieDeId = c.copieDeId();
        this.actif = c.actif();
    }

    private static void verifierTarifEtDelai(int tarif, int delaiJours) {
        if (tarif < TARIF_MINIMAL) throw new RegleMetierException("Le tarif doit être d'au moins " + TARIF_MINIMAL + " FCFA");
        if (delaiJours < 1 || delaiJours > 60) throw new RegleMetierException("Le délai doit être compris entre 1 et 60 jours");
    }

    /** Rubriques du formulaire selon le type d'acte (documents créés avant les variétés). */
    public static Set<Champ> champsParDefaut(TypeActe type) {
        return switch (type) {
            case NAISSANCE, RECONNAISSANCE -> EnumSet.of(Champ.PARENTS);
            case MARIAGE, DIVORCE -> EnumSet.of(Champ.PARENTS, Champ.CONJOINT);
            case DECES -> EnumSet.of(Champ.PARENTS, Champ.DECES);
        };
    }

    private static boolean vide(String s) { return s == null || s.isBlank(); }

    public boolean demande(Champ c) { return champs.contains(c); }

    public Long id() { return id; }
    public String code() { return code; }
    public String libelle() { return libelle; }
    public String description() { return description; }
    public Long natureId() { return natureId; }
    public String libelleNature() { return libelleNature; }
    public TypeActe typeActe() { return typeActe; }
    public ModeleDocument modele() { return modele; }
    public OrigineActe origine() { return origine; }
    public String typeMention() { return typeMention; }
    public Set<Champ> champs() { return Collections.unmodifiableSet(champs); }
    public boolean copieIntegrale() { return modele == ModeleDocument.COPIE_INTEGRALE; }
    public int tarif() { return tarif; }
    public int delaiJours() { return delaiJours; }
    public Integer dureeValiditeJours() { return dureeValiditeJours; }
    public String motsCles() { return motsCles; }
    public String piecesAFournir() { return piecesAFournir; }
    public boolean estOriginal() { return estOriginal; }
    public Long copieDeId() { return copieDeId; }
    public boolean actif() { return actif; }
}
