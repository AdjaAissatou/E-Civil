package sn.etatcivil.habilitation.adaptateur.entree.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import sn.etatcivil.habilitation.application.port.entree.GererMonCompte;
import sn.etatcivil.habilitation.domaine.ProfilPersonnel;

import java.time.LocalDate;

/**
 * Identité du demandeur, pour les agents qui instruisent ses demandes : ils comparent ce qu'il a déclaré
 * à l'inscription (NIN, date de naissance…) à sa pièce d'identité. Ses préférences ne sont pas transmises.
 */
@RestController
class DemandeurController {

    private final GererMonCompte comptes;

    DemandeurController(GererMonCompte comptes) {
        this.comptes = comptes;
    }

    public record Demandeur(String prenom, String nom, String email, String telephone, String sexe, LocalDate dateNaissance,
                            String lieuNaissance, String profession, String residence, String localite,
                            Integer nombreEnfants, String numeroCni, String typeUtilisateur, String paysResidence) {
        static Demandeur de(ProfilPersonnel p) {
            return new Demandeur(p.prenom(), p.nom(), p.email(), p.telephone(), p.sexe(), p.dateNaissance(), p.lieuNaissance(),
                    p.profession(), p.residence(), p.localite(), p.nombreEnfants(), p.numeroCni(), p.typeUtilisateur(),
                    p.paysResidence());
        }
    }

    @GetMapping("/api/habilitations/demandeurs/{id}")
    @PreAuthorize("hasAnyRole('DEMANDES_CONSULTER','SERVICE')")
    Demandeur demandeur(@PathVariable String id) {
        return Demandeur.de(comptes.monProfil(id));
    }
}
