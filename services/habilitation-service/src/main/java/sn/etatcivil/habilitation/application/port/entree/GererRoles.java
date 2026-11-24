package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.Administrateur;
import sn.etatcivil.habilitation.domaine.Profil;
import sn.etatcivil.habilitation.domaine.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GererRoles {

    record RoleDetaille(Role role, List<Profil> profils) {}

    List<RoleDetaille> roles();

    RoleDetaille role(String code);

    String creerRole(String code, String libelle, String description, Set<String> permissions, Administrateur auteur);

    void modifierRole(String code, String libelle, String description, Set<String> permissions, Administrateur auteur);

    void supprimerRole(String code, Administrateur auteur);

    /** Matrice rôles × permissions enregistrée d'un coup : seuls les rôles changés sont modifiés. */
    void modifierMatrice(Map<String, Set<String>> permissionsParRole, Administrateur auteur);
}
