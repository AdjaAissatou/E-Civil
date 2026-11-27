package sn.etatcivil.habilitation.application.service;

import sn.etatcivil.habilitation.application.CasDUsage;
import sn.etatcivil.habilitation.application.port.entree.GererMonCompte;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIdentites;
import sn.etatcivil.habilitation.domaine.ProfilPersonnel;
import sn.etatcivil.habilitation.domaine.RegleMetierException;

@CasDUsage
public class ServiceMonCompte implements GererMonCompte {

    private final AnnuaireIdentites annuaire;

    public ServiceMonCompte(AnnuaireIdentites annuaire) {
        this.annuaire = annuaire;
    }

    @Override
    public ProfilPersonnel monProfil(String utilisateur) {
        return annuaire.profilPersonnel(utilisateur).orElseThrow(() -> new RegleMetierException("Compte introuvable"));
    }

    /** L'adresse email (identifiant de connexion) ne change pas ici : elle se modifie dans l'espace sécurisé. */
    @Override
    public ProfilPersonnel modifierMonProfil(String utilisateur, ProfilPersonnel p) {
        ProfilPersonnel actuel = monProfil(utilisateur);
        ProfilPersonnel nouveau = new ProfilPersonnel(p.prenom(), p.nom(), actuel.email(), p.telephone(), p.sexe(),
                p.dateNaissance(), p.lieuNaissance(), p.profession(), p.residence(), p.localite(), p.nombreEnfants(),
                p.numeroCni(), p.typeUtilisateur(), p.paysResidence(), p.notifications(), p.langue());
        nouveau.verifierNumeroCni(java.time.Year.now(java.time.ZoneId.of("Africa/Dakar")).getValue());
        annuaire.modifierProfilPersonnel(utilisateur, nouveau);
        return monProfil(utilisateur);
    }
}
