package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.*;

import java.util.List;
import java.util.Set;

public interface GererUtilisateurs {

    /** Compte, avec ses permissions effectives et les opérations qui l'ont concerné. */
    record FicheUtilisateur(Utilisateur utilisateur, List<Role> roles, List<Permission> permissions,
                            List<Operation> historique) {}

    PageResultat<Utilisateur> utilisateurs(String recherche, PageDemandee page);

    FicheUtilisateur fiche(String id);

    String creerUtilisateur(NouvelUtilisateur nouveau, Administrateur auteur);

    void activer(String id, boolean actif, Administrateur auteur);

    void reinitialiserMotDePasse(String id, String motDePasse, Administrateur auteur);

    void definirProfils(String id, Set<String> profils, Administrateur auteur);
}
