package sn.etatcivil.habilitation.adaptateur.entree.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.habilitation.application.port.entree.GererMonCompte;
import sn.etatcivil.habilitation.domaine.ProfilPersonnel;

/** « Mon compte » : chacun ne voit et ne modifie que son propre profil (identifiant tiré du jeton). */
@RestController
@RequestMapping("/api/habilitations/moi")
class MonCompteController {

    private final GererMonCompte compte;

    MonCompteController(GererMonCompte compte) {
        this.compte = compte;
    }

    @GetMapping
    ProfilPersonnel lire(@AuthenticationPrincipal Jwt jwt) {
        return compte.monProfil(jwt.getSubject());
    }

    @PutMapping
    ProfilPersonnel modifier(@RequestBody ProfilPersonnel profil, @AuthenticationPrincipal Jwt jwt) {
        return compte.modifierMonProfil(jwt.getSubject(), profil);
    }
}
