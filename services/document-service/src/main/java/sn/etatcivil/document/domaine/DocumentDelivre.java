package sn.etatcivil.document.domaine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * Agrégat : document signé par un officier. Son contenu est figé ; l'empreinte SHA-256 et le code
 * de vérification permettent à n'importe qui de contrôler qu'un document présenté est authentique.
 */
public class DocumentDelivre {

    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final UUID id;
    private final UUID demandeId;
    private final String reference;
    private final String demandeurId;
    private final String libelleDocument;
    private final boolean copieIntegrale;
    private final String format;
    private final ContenuActe contenu;
    private final Complements complements;
    private final String signataire;
    private final Instant dateSignature;
    private final String codeVerification;
    private final String empreinte;

    public DocumentDelivre(UUID id, UUID demandeId, String reference, String demandeurId, String libelleDocument,
                           boolean copieIntegrale, String format, ContenuActe contenu, Complements complements,
                           String signataire, Instant dateSignature, String codeVerification, String empreinte) {
        this.id = id;
        this.demandeId = demandeId;
        this.reference = reference;
        this.demandeurId = demandeurId;
        this.libelleDocument = libelleDocument;
        this.copieIntegrale = copieIntegrale;
        this.format = format;
        this.contenu = contenu;
        this.complements = complements;
        this.signataire = signataire;
        this.dateSignature = dateSignature;
        this.codeVerification = codeVerification;
        this.empreinte = empreinte;
    }

    public static DocumentDelivre delivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                                           String format, ContenuActe contenu, Complements complements, String signataire,
                                           String code, Instant maintenant) {
        if (complements.etabliSurComparution()) {
            if (contenu != null) throw new RegleMetierException("Ce certificat s'établit sur comparution, sans acte");
            if (complements.declaration() == null) throw new RegleMetierException("La déclaration de la personne est obligatoire");
        } else {
            if (contenu == null) throw new RegleMetierException("L'acte du registre est obligatoire pour ce document");
            if (!contenu.estValide()) throw new RegleMetierException("L'acte " + contenu.numeroActe() + " n'est pas validé : il ne peut pas être délivré");
        }
        if (signataire == null || signataire.isBlank()) throw new RegleMetierException("Le signataire est obligatoire");
        if ((complements.estCertificat() || complements.estExtraitDeMention())
                && (complements.constat() == null || complements.constat().isBlank())) {
            throw new RegleMetierException("Un certificat ne peut être délivré sans le constat fait dans le registre");
        }
        if (complements.estCertificatDePersonne() && !"NAISSANCE".equals(contenu.typeActe())) {
            throw new RegleMetierException("Un certificat s'établit à partir de l'acte de naissance");
        }
        if (complements.estLivret() && !"MARIAGE".equals(contenu.typeActe())) {
            throw new RegleMetierException("Le livret de famille s'établit à partir de l'acte de mariage");
        }
        boolean integrale = Complements.COPIE_INTEGRALE.equals(complements.modele());
        Instant date = maintenant.truncatedTo(ChronoUnit.SECONDS);
        return new DocumentDelivre(UUID.randomUUID(), demandeId, reference, demandeurId, libelleDocument, integrale,
                format, contenu, complements, signataire, date, code,
                calculerEmpreinte(reference, libelleDocument, integrale, contenu, complements, signataire, date, code));
    }

    /** Code lisible, sans caractères ambigus (0/O, 1/I) : ABCDE-23456. */
    public static String nouveauCode(RandomGenerator aleatoire) {
        StringBuilder s = new StringBuilder(11);
        for (int i = 0; i < 10; i++) {
            if (i == 5) s.append('-');
            s.append(ALPHABET[aleatoire.nextInt(ALPHABET.length)]);
        }
        return s.toString();
    }

    /** Recalcule l'empreinte : toute modification du contenu en base serait détectée. */
    public boolean integre() {
        return empreinte.equals(calculerEmpreinte(reference, libelleDocument, copieIntegrale, contenu, complements,
                signataire, dateSignature, codeVerification));
    }

    /** Le demandeur télécharge son document numérique ; les agents peuvent l'imprimer (format papier). */
    public boolean telechargeablePar(String utilisateurId, boolean estAgent) {
        return estAgent || (demandeurId.equals(utilisateurId) && "NUMERIQUE".equals(format));
    }

    /** Les documents signés avant l'ajout des compléments n'en ont pas : leur empreinte est calculée comme avant. */
    private static String calculerEmpreinte(String reference, String libelle, boolean integrale, ContenuActe contenu,
                                            Complements complements, String signataire, Instant date, String code) {
        String texte = String.join("\n", reference, libelle, String.valueOf(integrale), signataire, date.toString(), code,
                contenu == null ? "" : contenu.texteCanonique());
        if (complements != null) texte = texte + "\n" + complements.texteCanonique();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public UUID id() { return id; }
    public UUID demandeId() { return demandeId; }
    public String reference() { return reference; }
    public String demandeurId() { return demandeurId; }
    public String libelleDocument() { return libelleDocument; }
    public boolean copieIntegrale() { return copieIntegrale; }
    public String format() { return format; }
    public ContenuActe contenu() { return contenu; }
    /** Compléments du document ; pour un document signé avant leur introduction, déduits de l'ancien indicateur. */
    public Complements complements() {
        return complements != null ? complements
                : new Complements(copieIntegrale ? Complements.COPIE_INTEGRALE : "EXTRAIT", null, null, null);
    }
    public String signataire() { return signataire; }
    public Instant dateSignature() { return dateSignature; }
    public String codeVerification() { return codeVerification; }
    public String empreinte() { return empreinte; }
}
