package sn.etatcivil.demande.application.port.sortie;

import java.util.UUID;

/** Le service de délivrance des documents signés (document-service). */
public interface ServiceDocuments {

    /**
     * @param modele  ce que contient le document (extrait, copie intégrale, certificat…)
     * @param motif   motif de la demande, imprimé sur les certificats
     * @param constat ce que l'état civil a constaté dans le registre (certificats), sinon {@code null}
     */
    record DocumentADelivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                             String modele, UUID acteId, String format, String signataire,
                             String motif, String constat) {}

    /** Fige l'acte et produit le document signé ; idempotent par demande. @return le code de vérification. */
    String delivrer(DocumentADelivrer document);
}
