package sn.etatcivil.socle.securite;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/** Identité extraite du jeton Keycloak de l'utilisateur connecté. */
public record UtilisateurJwt(String id, String email, String prenom, String nom, List<String> roles) {

    public static UtilisateurJwt depuis(Jwt jwt) {
        return new UtilisateurJwt(jwt.getSubject(), jwt.getClaimAsString("email"),
                valeur(jwt.getClaimAsString("given_name"), ""), valeur(jwt.getClaimAsString("family_name"), ""),
                RolesKeycloak.roles(jwt.getClaims()));
    }

    public boolean a(String role) {
        return roles.contains(role);
    }

    public String nomComplet() {
        return (prenom + " " + nom).trim();
    }

    private static String valeur(String v, String defaut) {
        return v == null ? defaut : v;
    }
}
