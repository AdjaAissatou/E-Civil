package sn.etatcivil.habilitation.application.port.sortie;

import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.domaine.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * L'annuaire des identités (Keycloak) : comptes, profils (groupes), rôles et permissions (rôles du realm).
 * Il reste la seule source de vérité ; les jetons qu'il délivre portent les permissions effectives de chacun.
 * @throws AnnuaireIndisponibleException si l'annuaire ne répond pas
 */
public interface AnnuaireIdentites {

    // ------------------------------------------------------------------ permissions et rôles
    List<Permission> permissions();

    /** Crée la permission si elle n'existe pas, sinon met à jour ses textes. */
    void enregistrerPermission(Permission permission);

    /** Rôles métier (hors permissions et rôles techniques), avec leurs permissions. */
    List<Role> roles();

    Optional<Role> role(String code);

    /** Un rôle quelconque du realm porte-t-il ce nom (permission, rôle métier ou technique) ? */
    boolean nomDeRoleUtilise(String code);

    void creerRole(Role role);

    /** Met à jour les textes et rend les permissions accordées identiques à celles du rôle. */
    void modifierRole(Role role);

    void supprimerRole(String code);

    // ------------------------------------------------------------------ profils
    List<Profil> profils();

    Optional<Profil> profil(String id);

    /** @return l'identifiant du profil créé */
    String creerProfil(Profil profil);

    void modifierProfil(Profil profil);

    void supprimerProfil(String id);

    List<Utilisateur> membres(String profilId);

    void definirProfilParDefaut(String profilId);

    // ------------------------------------------------------------------ comptes
    PageResultat<Utilisateur> utilisateurs(String recherche, PageDemandee page);

    Optional<Utilisateur> utilisateur(String id);

    boolean emailUtilise(String email);

    /** @return l'identifiant du compte créé, avec un mot de passe provisoire à changer à la première connexion */
    String creerUtilisateur(NouvelUtilisateur nouveau);

    void activer(String utilisateur, boolean actif);

    void motDePasseProvisoire(String utilisateur, String motDePasse);

    void ajouterAuProfil(String utilisateur, String profil);

    void retirerDuProfil(String utilisateur, String profil);

    /** Tous les rôles du realm dont le compte bénéficie (directement, par ses profils et par composition). */
    Set<String> rolesEffectifs(String utilisateur);

    /** Ferme les sessions ouvertes : les nouveaux droits s'appliquent à la connexion suivante. */
    void deconnecter(String utilisateur);

    // ------------------------------------------------------------------ migration des comptes existants
    List<String> utilisateursAvecRoleDirect(String role);

    Set<String> rolesDirects(String utilisateur);

    void retirerRolesDirects(String utilisateur, Set<String> roles);

    /** Retire ce rôle de ceux que reçoit automatiquement chaque nouveau compte. */
    void retirerDesRolesParDefaut(String role);

    /** Le rôle attribué à chaque nouveau compte (ex. default-roles-ecivil). */
    String roleParDefaut();
}
