package sn.etatcivil.demande.application.port.entree;

/** Préférences de notification d'un demandeur, choisies dans « Mon compte ». */
public interface GererPreferences {
    void definirNotifications(String demandeurId, boolean notifications);
}
