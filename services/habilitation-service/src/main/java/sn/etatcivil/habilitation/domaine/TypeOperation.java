package sn.etatcivil.habilitation.domaine;

public enum TypeOperation {
    INITIALISATION("Initialisation des habilitations"),
    UTILISATEUR_CREE("Compte créé"),
    UTILISATEUR_ACTIVE("Compte activé"),
    UTILISATEUR_DESACTIVE("Compte désactivé"),
    MOT_DE_PASSE_REINITIALISE("Mot de passe réinitialisé"),
    PROFILS_MODIFIES("Profils du compte modifiés"),
    PROFIL_CREE("Profil créé"),
    PROFIL_MODIFIE("Profil modifié"),
    PROFIL_SUPPRIME("Profil supprimé"),
    ROLE_CREE("Rôle créé"),
    ROLE_MODIFIE("Rôle modifié"),
    ROLE_SUPPRIME("Rôle supprimé"),
    PERMISSION_MODIFIEE("Permission modifiée");

    private final String libelle;

    TypeOperation(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
