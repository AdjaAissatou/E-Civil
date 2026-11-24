package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.ProfilPersonnel;

/** « Mon compte » : chacun consulte et modifie ses informations personnelles et ses préférences. */
public interface GererMonCompte {
    ProfilPersonnel monProfil(String utilisateur);

    ProfilPersonnel modifierMonProfil(String utilisateur, ProfilPersonnel profil);
}
