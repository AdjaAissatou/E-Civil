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

    /** Fonctions principales, de la plus large à la plus restreinte, avec leur libellé. */
    private static final java.util.LinkedHashMap<String, String> FONCTIONS = new java.util.LinkedHashMap<>();
    static {
        FONCTIONS.put("ADMIN", "Administrateur");
        FONCTIONS.put("OFFICIER", "Officier de l'état civil");
        FONCTIONS.put("AGENT_REGISTRE", "Agent du registre");
        FONCTIONS.put("DELEGUE", "Délégué");
        FONCTIONS.put("CITOYEN", "Citoyen");
    }

    /**
     * Libellé de la fonction affiché dans l'historique : celui du rôle de base le plus large ; « Agent » pour un
     * rôle créé par l'administration, « Citoyen » à défaut.
     */
    public String fonction() {
        for (var f : FONCTIONS.entrySet()) if (a(f.getKey())) return f.getValue();
        return roles.stream().anyMatch(r -> r.startsWith("DEMANDES_CONSULTER") || r.startsWith("REGISTRE_")) ? "Agent" : "Citoyen";
    }

    public String nomComplet() {
        return (prenom + " " + nom).trim();
    }

    private static String valeur(String v, String defaut) {
        return v == null ? defaut : v;
    }
}
