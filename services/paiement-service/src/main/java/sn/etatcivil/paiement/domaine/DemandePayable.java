package sn.etatcivil.paiement.domaine;

import java.util.UUID;

/**
 * Copie locale des informations utiles d'une demande, tenue à jour par les événements de demande-service.
 * Le service de paiement reste ainsi autonome : il ne rappelle jamais demande-service.
 */
public record DemandePayable(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                             int montant, boolean payable) {

    public void verifierPayablePar(String utilisateurId) {
        if (!demandeurId.equals(utilisateurId)) throw new RegleMetierException("Demande introuvable");
        if (!payable) throw new RegleMetierException("Cette demande n'est pas en attente de paiement");
    }
}
