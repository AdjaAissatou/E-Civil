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
        Instant date) {
}
