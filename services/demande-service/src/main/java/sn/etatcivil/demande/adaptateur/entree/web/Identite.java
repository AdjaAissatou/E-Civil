package sn.etatcivil.demande.adaptateur.entree.web;

import org.springframework.security.oauth2.jwt.Jwt;
import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.Demandeur;
import sn.etatcivil.demande.domaine.Role;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.util.List;

/** Traduit le jeton Keycloak en concepts du domaine (acteur, demandeur). */
final class Identite {
    private Identite() {}

    /** Ordre de priorité si un compte cumule plusieurs rôles. */
    private static final List<Role> PRIORITE = List.of(Role.ADMIN, Role.OFFICIER, Role.AGENT_REGISTRE, Role.DELEGUE, Role.CITOYEN);

    static Role role(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return PRIORITE.stream().filter(r -> u.a(r.name())).findFirst().orElse(Role.CITOYEN);
    }

    static Acteur acteur(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return new Acteur(u.id(), u.nomComplet().isBlank() ? u.email() : u.nomComplet(), role(jwt));
    }

    static Demandeur demandeur(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return new Demandeur(u.id(), u.email(), u.prenom(), u.nom());
    }
}
