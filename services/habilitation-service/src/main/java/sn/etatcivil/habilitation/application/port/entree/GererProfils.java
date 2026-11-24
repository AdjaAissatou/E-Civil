package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.Administrateur;
import sn.etatcivil.habilitation.domaine.Profil;
import sn.etatcivil.habilitation.domaine.Utilisateur;

import java.util.List;
import java.util.Set;

public interface GererProfils {

    record ProfilDetaille(Profil profil, List<Utilisateur> membres) {}

    List<ProfilDetaille> profils();

    ProfilDetaille profil(String id);

    String creerProfil(String nom, String description, Set<String> roles, Administrateur auteur);

    void modifierProfil(String id, String nom, String description, Set<String> roles, Administrateur auteur);

    void supprimerProfil(String id, Administrateur auteur);
}
