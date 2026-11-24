package sn.etatcivil.habilitation.adaptateur.sortie.keycloak;

import java.util.*;

/**
 * Attributs du profil utilisateur déclarés dans Keycloak (« user profile ») : ils apparaissent sur la page
 * d'inscription et sont validés par Keycloak. Les préférences (notifications, langue) ne sont modifiables que par
 * le portail, au nom de l'utilisateur, depuis « Mon compte ».
 */
final class ProfilUtilisateurKeycloak {
    private ProfilUtilisateurKeycloak() {}

    static final String GROUPE = "informations-personnelles";

    static final List<String> NOMS = List.of("telephone", "sexe", "dateNaissance", "lieuNaissance", "profession",
            "residence", "localite", "nombreEnfants", "numeroCni", "typeUtilisateur", "paysResidence",
            "notifications", "langue");

    static Map<String, Object> groupe() {
        return Map.of("name", GROUPE, "displayHeader", "Informations personnelles",
                "displayDescription", "Ces informations servent à préremplir vos demandes de documents.");
    }

    static List<Map<String, Object>> attributs() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(attribut("telephone", "Téléphone", true, true, Map.of("inputType", "html5-tel",
                        "inputHelperTextBefore", "Ex. +221 77 123 45 67"),
                Map.of("length", Map.of("max", 20), "pattern", Map.of("pattern", "^\\+?[0-9 ().-]{7,20}$",
                        "error-message", "Numéro de téléphone invalide"))));
        a.add(attribut("sexe", "Sexe", true, true, Map.of("inputType", "select",
                        "inputOptionLabels", Map.of("M", "Masculin", "F", "Féminin")),
                Map.of("options", Map.of("options", List.of("M", "F")))));
        a.add(attribut("dateNaissance", "Date de naissance", true, true, Map.of("inputType", "html5-date"),
                Map.of("iso-date", Map.of())));
        a.add(attribut("lieuNaissance", "Lieu de naissance", false, true, Map.of(), Map.of("length", Map.of("max", 150))));
        a.add(attribut("profession", "Profession", false, true, Map.of(), Map.of("length", Map.of("max", 150))));
        a.add(attribut("residence", "Adresse de résidence", false, true, Map.of(), Map.of("length", Map.of("max", 255))));
        a.add(attribut("localite", "Commune ou localité", false, true, Map.of(), Map.of("length", Map.of("max", 150))));
        a.add(attribut("nombreEnfants", "Nombre d'enfants", false, true, Map.of("inputType", "html5-number"),
                Map.of("integer", Map.of("min", 0, "max", 50))));
        a.add(attribut("numeroCni", "Numéro de carte d'identité", false, true, Map.of(), Map.of("length", Map.of("max", 30))));
        a.add(attribut("typeUtilisateur", "Je réside", true, true, Map.of("inputType", "select-radiobuttons",
                        "inputOptionLabels", Map.of("RESIDENT", "au Sénégal", "DIASPORA", "à l'étranger (diaspora)")),
                Map.of("options", Map.of("options", List.of("RESIDENT", "DIASPORA")))));
        a.add(attribut("paysResidence", "Pays de résidence (diaspora)", false, true, Map.of(), Map.of("length", Map.of("max", 150))));
        a.add(attribut("notifications", "Notifications par email", false, false, Map.of(),
                Map.of("options", Map.of("options", List.of("oui", "non")))));
        a.add(attribut("langue", "Langue du portail", false, false, Map.of(),
                Map.of("options", Map.of("options", List.of("fr", "wo", "en")))));
        return a;
    }

    private static Map<String, Object> attribut(String nom, String libelle, boolean requis, boolean modifiableParLaPersonne,
                                                Map<String, Object> annotations, Map<String, Object> validations) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", nom);
        m.put("displayName", libelle);
        m.put("validations", validations);
        m.put("annotations", annotations);
        // requis à l'inscription et dans le compte, jamais imposé à l'administrateur (comptes d'agents)
        if (requis) m.put("required", Map.of("roles", List.of("user")));
        m.put("permissions", Map.of("view", List.of("admin", "user"),
                "edit", modifiableParLaPersonne ? List.of("admin", "user") : List.of("admin")));
        m.put("multivalued", false);
        if (modifiableParLaPersonne) m.put("group", GROUPE);
        return m;
    }

    /** Ajoute ou remplace nos attributs dans la configuration existante, sans toucher aux autres. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> fusionner(Map<String, Object> config) {
        Map<String, Object> c = new LinkedHashMap<>(config == null ? Map.of() : config);
        List<Map<String, Object>> existants = new ArrayList<>((List<Map<String, Object>>) c.getOrDefault("attributes", List.of()));
        existants.removeIf(x -> NOMS.contains(String.valueOf(x.get("name"))));
        existants.addAll(attributs());
        c.put("attributes", existants);
        List<Map<String, Object>> groupes = new ArrayList<>((List<Map<String, Object>>) c.getOrDefault("groups", List.of()));
        groupes.removeIf(g -> GROUPE.equals(g.get("name")));
        groupes.add(groupe());
        c.put("groups", groupes);
        return c;
    }
}
