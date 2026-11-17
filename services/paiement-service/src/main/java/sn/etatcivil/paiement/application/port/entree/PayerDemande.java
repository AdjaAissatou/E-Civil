package sn.etatcivil.paiement.application.port.entree;

import sn.etatcivil.paiement.domaine.Paiement;

import java.util.List;
import java.util.UUID;

/** Cas d'usage du paiement en ligne. */
public interface PayerDemande {
    /** @return l'adresse de la page de paiement sécurisée vers laquelle rediriger le citoyen */
    String initier(UUID demandeId, String demandeurId);

    /** Interroge l'opérateur (retour navigateur ou notification) et enregistre le résultat. */
    Paiement confirmer(String token);

    /** Notification serveur à serveur de l'opérateur, authentifiée par sa signature. */
    void recevoirNotification(String signature, String token);

    List<Paiement> paiements(UUID demandeId, String utilisateurId, boolean estAgent);

    boolean disponible();
}
