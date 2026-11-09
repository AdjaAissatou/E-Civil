package sn.etatcivil.demande.application.port.sortie;

import java.util.UUID;

/** Le service de délivrance des documents signés (document-service). */
public interface ServiceDocuments {

    record DocumentADelivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                             boolean copieIntegrale, UUID acteId, String format, String signataire) {}

    /** Fige l'acte et produit le document signé ; idempotent par demande. @return le code de vérification. */
    String delivrer(DocumentADelivrer document);
}
