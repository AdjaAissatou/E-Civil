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
                             String motif, String constat,
                             sn.etatcivil.demande.domaine.PersonneConcernee personne,
                             sn.etatcivil.demande.domaine.ComplementsDemande complements,
                             Integer dureeValiditeJours, String typeMention) {

        public DocumentADelivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                                 String modele, UUID acteId, String format, String signataire, String motif, String constat) {
            this(demandeId, reference, demandeurId, libelleDocument, modele, acteId, format, signataire, motif, constat,
                    null, null, null, null);
        }
    }

    /** Fige l'acte et produit le document signé ; idempotent par demande. @return le code de vérification. */
    String delivrer(DocumentADelivrer document);
}
