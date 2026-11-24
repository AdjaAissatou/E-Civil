package sn.etatcivil.demande.adaptateur.entree.web;

import org.springframework.security.oauth2.jwt.Jwt;
import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.Demandeur;
import sn.etatcivil.demande.domaine.Habilitation;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Traduit le jeton Keycloak en concepts du domaine : les permissions du jeton deviennent des habilitations. */
final class Identite {
    private Identite() {}

    static Set<Habilitation> habilitations(Jwt jwt) {
        Set<String> roles = Set.copyOf(UtilisateurJwt.depuis(jwt).roles());
        return Arrays.stream(Habilitation.values()).filter(h -> roles.contains(h.name()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Habilitation.class)));
    }

    static Acteur acteur(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return new Acteur(u.id(), u.nomComplet().isBlank() ? u.email() : u.nomComplet(), u.fonction(), habilitations(jwt));
    }

    static boolean consulteToutes(Jwt jwt) {
        return habilitations(jwt).contains(Habilitation.DEMANDES_CONSULTER);
    }

    static Demandeur demandeur(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return new Demandeur(u.id(), u.email(), u.prenom(), u.nom());
    }
}
