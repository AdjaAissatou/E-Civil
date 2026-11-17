package sn.etatcivil.portail.securite;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Utilisateur connecté, tel que l'affichent les pages (identité Keycloak). */
public record Utilisateur(String id, String email, String prenom, String nom, String role, List<String> roles) {

    private static final List<String> PRIORITE = List.of("ADMIN", "OFFICIER", "AGENT_REGISTRE", "DELEGUE", "CITOYEN");

    public static Utilisateur depuis(OidcUser u) {
        List<String> roles = roles(u.getClaims());
        String role = PRIORITE.stream().filter(roles::contains).findFirst().orElse("CITOYEN");
        return new Utilisateur(u.getSubject(), u.getEmail(), u.getGivenName() == null ? "" : u.getGivenName(),
                u.getFamilyName() == null ? "" : u.getFamilyName(), role, roles);
    }

    @SuppressWarnings("unchecked")
    static List<String> roles(Map<String, Object> claims) {
        if (claims.get("realm_access") instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> c) {
            return c.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public String nomComplet() {
        String n = (prenom + " " + nom).trim();
        return n.isEmpty() ? email : n;
    }

    public boolean estAgent() { return !"CITOYEN".equals(role); }

    public String libelleRole() {
        return switch (role) {
            case "ADMIN" -> "Administrateur";
            case "OFFICIER" -> "Officier de l'état civil";
            case "AGENT_REGISTRE" -> "Agent du registre";
            case "DELEGUE" -> "Délégué";
            default -> "Citoyen";
        };
    }
}
