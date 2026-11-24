package sn.etatcivil.portail.securite;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utilisateur connecté, tel que l'affichent les pages. Ses droits sont les permissions que Keycloak inscrit
 * dans son jeton (obtenues par ses profils et ses rôles).
 */
public record Utilisateur(String id, String email, String prenom, String nom, List<String> roles) {

    /** Fonctions de base, de la plus large à la plus restreinte, pour l'affichage. */
    private static final Map<String, String> FONCTIONS = new java.util.LinkedHashMap<>();
    static {
        FONCTIONS.put("ADMIN", "Administrateur");
        FONCTIONS.put("OFFICIER", "Officier de l'état civil");
        FONCTIONS.put("AGENT_REGISTRE", "Agent du registre");
        FONCTIONS.put("DELEGUE", "Délégué");
        FONCTIONS.put("CITOYEN", "Citoyen");
    }

    public static Utilisateur depuis(OidcUser u) {
        return new Utilisateur(u.getSubject(), u.getEmail(), u.getGivenName() == null ? "" : u.getGivenName(),
                u.getFamilyName() == null ? "" : u.getFamilyName(), roles(u.getClaims()));
    }

    @SuppressWarnings("unchecked")
    static List<String> roles(Map<String, Object> claims) {
        if (claims.get("realm_access") instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> c) {
            return c.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public boolean peut(String permission) {
        return roles.contains(permission);
    }

    public String nomComplet() {
        String n = (prenom + " " + nom).trim();
        return n.isEmpty() ? email : n;
    }

    /** Agent : quelqu'un qui consulte les demandes des autres ou le registre. */
    public boolean estAgent() {
        return peut("DEMANDES_CONSULTER") || peut("REGISTRE_CONSULTER");
    }

    public String libelleRole() {
        for (var f : FONCTIONS.entrySet()) if (roles.contains(f.getKey())) return f.getValue();
        return estAgent() ? "Agent" : "Citoyen";
    }

    /** Page d'arrivée après la connexion, selon ce que la personne peut faire. */
    public String accueil() {
        if (peut("UTILISATEURS_GERER") || peut("HABILITATIONS_GERER")) return "/admin";
        if (peut("DOCUMENTS_SIGNER") || peut("REGISTRE_VALIDER")) return "/officier";
        if (peut("REGISTRE_SAISIR") || peut("DEMANDES_RECHERCHER")) return "/registre";
        if (peut("DEMANDES_VALIDER") || peut("DOCUMENTS_LIVRER")) return "/delegue";
        if (peut("DEMANDES_CONSULTER")) return "/back-office/demandes";
        if (peut("STATISTIQUES_CONSULTER") || peut("CATALOGUE_GERER") || peut("JOURNAL_CONSULTER")) return "/admin";
        return "/demandes";
    }
}
