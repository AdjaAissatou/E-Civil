package sn.etatcivil.evenements;

import java.time.Instant;

/**
 * Une demande a changé de statut. Le message est autoportant : les abonnés (notification, paiement)
 * n'ont pas besoin de rappeler demande-service pour agir.
 */
public record DemandeStatutChange(
        String demandeId,
        String reference,
        String ancienStatut,
        String nouveauStatut,
        String demandeurId,
        String emailDemandeur,
        String prenomDemandeur,
        String libelleDocument,
        String personneConcernee,
        int montant,
        String format,
        String motifRejet,
        Instant date,
        /** {@code false} si le demandeur a refusé les emails ; absent (anciens messages) = oui. */
        Boolean notifier) {

    public DemandeStatutChange(String demandeId, String reference, String ancienStatut, String nouveauStatut,
                               String demandeurId, String emailDemandeur, String prenomDemandeur, String libelleDocument,
                               String personneConcernee, int montant, String format, String motifRejet, Instant date) {
        this(demandeId, reference, ancienStatut, nouveauStatut, demandeurId, emailDemandeur, prenomDemandeur,
                libelleDocument, personneConcernee, montant, format, motifRejet, date, null);
    }

    public boolean emailsAcceptes() {
        return !Boolean.FALSE.equals(notifier);
    }
}
