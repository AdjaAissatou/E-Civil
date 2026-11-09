package sn.etatcivil.paiement.domaine;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Agrégat : une tentative de paiement en ligne pour une demande. */
public class Paiement {

    /** Une facture non payée reste réutilisable pendant cette durée (évite d'en créer une à chaque clic). */
    public static final Duration VALIDITE_FACTURE = Duration.ofHours(1);

    private final UUID id;
    private final UUID demandeId;
    private final String token;
    private final int montant;
    private final String urlPaiement;
    private final Instant dateCreation;
    private StatutPaiement statut;
    private String urlRecu;
    private Instant dateConfirmation;
    private final Integer version;

    public Paiement(UUID id, UUID demandeId, String token, int montant, String urlPaiement, Instant dateCreation,
                    StatutPaiement statut, String urlRecu, Instant dateConfirmation, Integer version) {
        this.id = id;
        this.demandeId = demandeId;
        this.token = token;
        this.montant = montant;
        this.urlPaiement = urlPaiement;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.urlRecu = urlRecu;
        this.dateConfirmation = dateConfirmation;
        this.version = version;
    }

    public static Paiement initier(UUID demandeId, String token, int montant, String urlPaiement, Instant maintenant) {
        return new Paiement(UUID.randomUUID(), demandeId, token, montant, urlPaiement, maintenant, StatutPaiement.INITIE,
                null, null, null);
    }

    public boolean reutilisable(Instant maintenant) {
        return statut == StatutPaiement.INITIE && dateCreation.plus(VALIDITE_FACTURE).isAfter(maintenant);
    }

    /**
     * Applique l'état rapporté par l'opérateur.
     * @return {@code true} si le paiement vient d'être confirmé (à publier une seule fois)
     */
    public boolean appliquer(EtatFacture etat, Instant maintenant) {
        if (statut != StatutPaiement.INITIE) return false;
        switch (etat.etat()) {
            case PAYEE -> {
                if (etat.montant() != montant) {
                    throw new RegleMetierException("Le montant payé (" + etat.montant() + " FCFA) ne correspond pas au montant dû ("
                            + montant + " FCFA). Contactez l'administration avec la référence de votre demande.");
                }
                statut = StatutPaiement.PAYE;
                urlRecu = etat.urlRecu();
                dateConfirmation = maintenant;
                return true;
            }
            case ANNULEE -> statut = StatutPaiement.ANNULE;
            case EN_ATTENTE, INCONNUE -> { /* rien à faire, on réinterrogera plus tard */ }
        }
        return false;
    }

    public UUID id() { return id; }
    public UUID demandeId() { return demandeId; }
    public String token() { return token; }
    public int montant() { return montant; }
    public String urlPaiement() { return urlPaiement; }
    public Instant dateCreation() { return dateCreation; }
    public StatutPaiement statut() { return statut; }
    public String urlRecu() { return urlRecu; }
    public Instant dateConfirmation() { return dateConfirmation; }
    public Integer version() { return version; }
}
