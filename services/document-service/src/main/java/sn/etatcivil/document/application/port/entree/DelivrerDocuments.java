package sn.etatcivil.document.application.port.entree;

import sn.etatcivil.document.domaine.DocumentDelivre;

import java.util.Optional;
import java.util.UUID;

public interface DelivrerDocuments {

    /**
     * @param modele  EXTRAIT, EXTRAIT_SANS_FILIATION, COPIE_INTEGRALE, LIVRET_FAMILLE ou CERTIFICAT_…
     * @param motif   motif de la demande (imprimé sur les certificats)
     * @param constat constat fait dans le registre, obligatoire pour un certificat
     */
    record DocumentADelivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                             String modele, UUID acteId, String format, String signataire, String motif, String constat,
                             sn.etatcivil.document.domaine.Declaration declaration, Integer dureeValiditeJours,
                             String typeMention) {

        public DocumentADelivrer(UUID demandeId, String reference, String demandeurId, String libelleDocument,
                                 String modele, UUID acteId, String format, String signataire, String motif, String constat) {
            this(demandeId, reference, demandeurId, libelleDocument, modele, acteId, format, signataire, motif, constat,
                    null, null, null);
        }
    }

    /** Idempotent : un second appel pour la même demande renvoie le document déjà délivré. */
    DocumentDelivre delivrer(DocumentADelivrer commande);

    /** PDF du document, si l'utilisateur y a droit. */
    byte[] pdf(UUID demandeId, String utilisateurId, boolean estAgent);

    /** Page publique de vérification : le document n'est renvoyé que si son empreinte est intacte. */
    Optional<DocumentDelivre> verifier(String code);
}
