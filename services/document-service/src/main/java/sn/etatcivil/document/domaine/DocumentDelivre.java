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
    private final String signataire;
    private final Instant dateSignature;
    private final String codeVerification;
    private final String empreinte;

    public DocumentDelivre(UUID id, UUID demandeId, String reference, String demandeurId, String libelleDocument,
                           boolean copieIntegrale, String format, ContenuActe contenu, String signataire,
                           Instant dateSignature, String codeVerification, String empreinte) {
        this.id = id;
        this.demandeId = demandeId;
        this.reference = reference;
        this.demandeurId = demandeurId;
        this.libelleDocument = libelleDocument;
        this.copieIntegrale = copieIntegrale;
        this.format = format;
        this.contenu = contenu;
        this.signataire = signataire;
        this.dateSignature = dateSignature;
        this.codeVerification = codeVerification;
        this.empreinte = empreinte;
    }

    public static DocumentDelivre delivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                                           boolean copieIntegrale, String format, ContenuActe contenu, String signataire,
                                           String code, Instant maintenant) {
        if (!contenu.estValide()) throw new RegleMetierException("L'acte " + contenu.numeroActe() + " n'est pas validé : il ne peut pas être délivré");
        if (signataire == null || signataire.isBlank()) throw new RegleMetierException("Le signataire est obligatoire");
        Instant date = maintenant.truncatedTo(ChronoUnit.SECONDS);
        return new DocumentDelivre(UUID.randomUUID(), demandeId, reference, demandeurId, libelleDocument, copieIntegrale,
                format, contenu, signataire, date, code,
                calculerEmpreinte(reference, libelleDocument, copieIntegrale, contenu, signataire, date, code));
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
        return empreinte.equals(calculerEmpreinte(reference, libelleDocument, copieIntegrale, contenu, signataire,
                dateSignature, codeVerification));
    }

    /** Le demandeur télécharge son document numérique ; les agents peuvent l'imprimer (format papier). */
    public boolean telechargeablePar(String utilisateurId, boolean estAgent) {
        return estAgent || (demandeurId.equals(utilisateurId) && "NUMERIQUE".equals(format));
    }

    private static String calculerEmpreinte(String reference, String libelle, boolean integrale, ContenuActe contenu,
                                            String signataire, Instant date, String code) {
        String texte = String.join("\n", reference, libelle, String.valueOf(integrale), signataire, date.toString(), code,
                contenu.texteCanonique());
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
    public String signataire() { return signataire; }
    public Instant dateSignature() { return dateSignature; }
    public String codeVerification() { return codeVerification; }
    public String empreinte() { return empreinte; }
}
