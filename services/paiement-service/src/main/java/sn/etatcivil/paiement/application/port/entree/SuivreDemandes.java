package sn.etatcivil.paiement.application.port.entree;

import java.util.UUID;

/** Tient à jour la liste des demandes payables à partir des événements de demande-service. */
public interface SuivreDemandes {
    void statutChange(UUID demandeId, String reference, String demandeurId, String libelleDocument, int montant,
                      String nouveauStatut);
}
