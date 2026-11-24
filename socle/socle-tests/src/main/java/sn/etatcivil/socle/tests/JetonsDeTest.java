package sn.etatcivil.socle.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contenu du claim {@code realm_access.roles} d'un vrai jeton Keycloak pour une personne qui a ce rôle de base :
 * le rôle lui-même et les permissions qui le composent (configuration de départ des habilitations).
 */
public final class JetonsDeTest {
    private JetonsDeTest() {}

    private static final Map<String, List<String>> PERMISSIONS = Map.of(
            "CITOYEN", List.of("DEMANDES_DEPOSER"),
            "DELEGUE", List.of("DEMANDES_CONSULTER", "DEMANDES_VALIDER", "DOCUMENTS_LIVRER"),
            "AGENT_REGISTRE", List.of("DEMANDES_CONSULTER", "REGISTRE_CONSULTER", "REGISTRE_SAISIR", "DEMANDES_RECHERCHER"),
            "OFFICIER", List.of("DEMANDES_CONSULTER", "REGISTRE_CONSULTER", "REGISTRE_SAISIR", "REGISTRE_VALIDER",
                    "DEMANDES_RECHERCHER", "DOCUMENTS_SIGNER"),
            "ADMIN", List.of("DEMANDES_CONSULTER", "DEMANDES_VALIDER", "DEMANDES_RECHERCHER", "DEMANDES_SUPERVISER",
                    "DOCUMENTS_LIVRER", "DOCUMENTS_SIGNER", "REGISTRE_CONSULTER", "REGISTRE_SAISIR", "REGISTRE_VALIDER",
                    "CATALOGUE_GERER", "STATISTIQUES_CONSULTER", "UTILISATEURS_GERER", "HABILITATIONS_GERER", "JOURNAL_CONSULTER"));

    public static List<String> roles(String role) {
        List<String> r = new ArrayList<>(List.of(role));
        r.addAll(PERMISSIONS.getOrDefault(role, List.of()));
        return r;
    }
}
