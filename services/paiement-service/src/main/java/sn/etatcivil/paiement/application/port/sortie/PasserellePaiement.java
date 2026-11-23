package sn.etatcivil.paiement.application.port.sortie;

import sn.etatcivil.paiement.domaine.EtatFacture;

/** Opérateur de paiement (PayDunya aujourd'hui ; un autre opérateur ne demanderait qu'un nouvel adaptateur). */
public interface PasserellePaiement {

    record Facture(String token, String urlPaiement) {}

    record Retours(String urlRetour, String urlAnnulation, String urlNotification) {}

    Facture creerFacture(int montant, String description, String reference, Retours retours);

    EtatFacture consulter(String token);

    /** Vérifie que la notification provient bien de l'opérateur. */
    boolean notificationAuthentique(String signature);

    boolean configuree();
}
