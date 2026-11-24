package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.Administrateur;
import sn.etatcivil.habilitation.domaine.Permission;

import java.util.List;

public interface GererPermissions {
    List<Permission> permissions();

    void modifierPermission(String code, String libelle, String description, String avantage, String interdiction,
                  Administrateur auteur);
}
