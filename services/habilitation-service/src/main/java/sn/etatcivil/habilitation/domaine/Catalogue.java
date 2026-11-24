package sn.etatcivil.habilitation.domaine;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration de départ des habilitations, inspirée de celle de la version d'origine : 15 permissions,
 * 5 rôles de base et 6 profils. Elle n'est appliquée qu'à l'initialisation ; l'administrateur la fait ensuite évoluer.
 */
public final class Catalogue {
    private Catalogue() {}

    // ------------------------------------------------------------------ codes des permissions
    public static final String DEMANDES_DEPOSER = "DEMANDES_DEPOSER";
    public static final String DEMANDES_CONSULTER = "DEMANDES_CONSULTER";
    public static final String DEMANDES_VALIDER = "DEMANDES_VALIDER";
    public static final String DEMANDES_RECHERCHER = "DEMANDES_RECHERCHER";
    public static final String DEMANDES_SUPERVISER = "DEMANDES_SUPERVISER";
    public static final String DOCUMENTS_LIVRER = "DOCUMENTS_LIVRER";
    public static final String DOCUMENTS_SIGNER = "DOCUMENTS_SIGNER";
    public static final String REGISTRE_CONSULTER = "REGISTRE_CONSULTER";
    public static final String REGISTRE_SAISIR = "REGISTRE_SAISIR";
    public static final String REGISTRE_VALIDER = "REGISTRE_VALIDER";
    public static final String CATALOGUE_GERER = "CATALOGUE_GERER";
    public static final String STATISTIQUES_CONSULTER = "STATISTIQUES_CONSULTER";
    public static final String UTILISATEURS_GERER = "UTILISATEURS_GERER";
    public static final String HABILITATIONS_GERER = "HABILITATIONS_GERER";
    public static final String JOURNAL_CONSULTER = "JOURNAL_CONSULTER";

    /** Permissions qui font d'une personne un administrateur : il doit toujours en rester un. */
    public static final Set<String> ADMINISTRATION = Set.of(UTILISATEURS_GERER, HABILITATIONS_GERER);

    public static final List<Permission> PERMISSIONS = List.of(
            new Permission(DEMANDES_DEPOSER, "Déposer ses demandes",
                    "Déposer, payer, suivre et annuler ses propres demandes, télécharger ses documents.",
                    "Accès en ligne à toutes les démarches", "Aucun accès aux demandes des autres", "Citoyen"),
            new Permission(DEMANDES_CONSULTER, "Consulter toutes les demandes",
                    "Ouvrir la liste et la fiche de toutes les demandes, télécharger les documents délivrés.",
                    "Vue d'ensemble du traitement", "Lecture seule : aucune décision", "Instruction"),
            new Permission(DEMANDES_VALIDER, "Valider les demandes",
                    "Valider une demande déposée pour l'ouvrir au paiement, ou la rejeter avant paiement.",
                    "Contrôle de recevabilité", "Signature interdite", "Instruction"),
            new Permission(DOCUMENTS_LIVRER, "Livrer les documents papier",
                    "Marquer comme expédié un document signé demandé au format papier.",
                    "Suivi des expéditions", "Aucune modification du document", "Instruction"),
            new Permission(DEMANDES_SUPERVISER, "Superviser les demandes",
                    "Rejeter une demande à n'importe quelle étape de son traitement.",
                    "Résolution des cas bloqués", "Signature interdite", "Instruction"),
            new Permission(REGISTRE_CONSULTER, "Consulter le registre national",
                    "Rechercher et consulter les actes du registre national.",
                    "Accès aux actes d'état civil", "Modification interdite", "Registre"),
            new Permission(REGISTRE_SAISIR, "Saisir le registre",
                    "Transcrire les actes des registres papier, corriger un acte non validé, ajouter des mentions marginales.",
                    "Alimentation du registre national", "Validation interdite", "Registre"),
            new Permission(REGISTRE_VALIDER, "Valider les actes",
                    "Valider un acte transcrit : il devient délivrable et ne peut plus être modifié.",
                    "Validation avec force légale", "Modification après validation interdite", "Registre"),
            new Permission(DEMANDES_RECHERCHER, "Rechercher les actes des demandes",
                    "Relancer la recherche d'un acte, rattacher manuellement un acte, rejeter une demande dont l'acte n'existe pas.",
                    "Traitement des actes non retrouvés", "Signature interdite", "Registre"),
            new Permission(DOCUMENTS_SIGNER, "Signer les documents",
                    "Contrôler l'acte retrouvé, le contester, signer et délivrer le document.",
                    "Signature légale des documents", "Suppression interdite", "Officier"),
            new Permission(CATALOGUE_GERER, "Gérer le catalogue",
                    "Modifier les tarifs, les délais et la disponibilité des documents.",
                    "Paramétrage de l'offre", "Modification des demandes déjà déposées interdite", "Administration"),
            new Permission(STATISTIQUES_CONSULTER, "Consulter les statistiques",
                    "Accéder au tableau de bord : demandes par statut, registre.",
                    "Pilotage de l'activité", "Export massif interdit", "Administration"),
            new Permission(UTILISATEURS_GERER, "Gérer les utilisateurs",
                    "Créer les comptes des agents, attribuer les profils, activer, désactiver, réinitialiser un mot de passe.",
                    "Gestion des comptes", "Suppression de comptes interdite", "Administration"),
            new Permission(HABILITATIONS_GERER, "Gérer les habilitations",
                    "Créer et modifier les profils et les rôles, attribuer les permissions.",
                    "Organisation des droits", "Création de nouvelles permissions interdite", "Administration"),
            new Permission(JOURNAL_CONSULTER, "Consulter le journal",
                    "Consulter le journal des opérations d'administration.",
                    "Traçabilité", "Modification du journal interdite", "Administration"));

    public static final Set<String> CODES_PERMISSIONS = PERMISSIONS.stream().map(Permission::code)
            .collect(Collectors.toUnmodifiableSet());

    /** Rôle de base : son code est utilisé par l'application, il ne peut pas être supprimé. */
    public record RoleDeBase(String code, String libelle, String description, Set<String> permissions) {}

    public static final List<RoleDeBase> ROLES_DE_BASE = List.of(
            new RoleDeBase("CITOYEN", "Citoyen", "Personne qui demande des documents pour elle-même ou ses proches.",
                    Set.of(DEMANDES_DEPOSER)),
            new RoleDeBase("DELEGUE", "Délégué", "Examine les demandes déposées et expédie les documents papier.",
                    Set.of(DEMANDES_CONSULTER, DEMANDES_VALIDER, DOCUMENTS_LIVRER)),
            new RoleDeBase("AGENT_REGISTRE", "Agent du registre",
                    "Transcrit les actes des registres papier et traite les actes non retrouvés.",
                    Set.of(DEMANDES_CONSULTER, REGISTRE_CONSULTER, REGISTRE_SAISIR, DEMANDES_RECHERCHER)),
            new RoleDeBase("OFFICIER", "Officier de l'état civil",
                    "Valide les actes transcrits, contrôle et signe les documents.",
                    Set.of(DEMANDES_CONSULTER, REGISTRE_CONSULTER, REGISTRE_SAISIR, REGISTRE_VALIDER, DEMANDES_RECHERCHER,
                            DOCUMENTS_SIGNER)),
            new RoleDeBase("ADMIN", "Administrateur", "Administre la plateforme : catalogue, comptes, habilitations.",
                    CODES_PERMISSIONS.stream().filter(c -> !c.equals(DEMANDES_DEPOSER)).collect(Collectors.toUnmodifiableSet())));

    public static final Set<String> CODES_ROLES_DE_BASE = ROLES_DE_BASE.stream().map(RoleDeBase::code)
            .collect(Collectors.toUnmodifiableSet());

    /** Profil de départ ; le profil par défaut est donné à chaque personne qui s'inscrit. */
    public record ProfilDeBase(String nom, String description, Set<String> roles, boolean parDefaut) {}

    public static final ProfilDeBase PROFIL_CITOYEN = new ProfilDeBase("Citoyen",
            "Toute personne inscrite sur le portail.", Set.of("CITOYEN"), true);
    public static final ProfilDeBase PROFIL_ADMINISTRATEUR = new ProfilDeBase("Administrateur",
            "Administration de la plateforme.", Set.of("ADMIN"), false);

    public static final List<ProfilDeBase> PROFILS_DE_BASE = List.of(
            PROFIL_CITOYEN,
            new ProfilDeBase("Délégué de commune", "Délégué d'une mairie : instruction des demandes.", Set.of("DELEGUE"), false),
            new ProfilDeBase("Agent du registre", "Saisie et mise à jour du registre national.", Set.of("AGENT_REGISTRE"), false),
            new ProfilDeBase("Officier de l'état civil", "Validation des actes et signature des documents.", Set.of("OFFICIER"), false),
            new ProfilDeBase("Chef de centre", "Officier qui instruit aussi les demandes de son centre.",
                    Set.of("OFFICIER", "DELEGUE"), false),
            PROFIL_ADMINISTRATEUR);

    /** Profil de base attribué, à la migration, aux comptes qui avaient directement ce rôle. */
    public static String profilDuRole(String role) {
        return switch (role) {
            case "DELEGUE" -> "Délégué de commune";
            case "AGENT_REGISTRE" -> "Agent du registre";
            case "OFFICIER" -> "Officier de l'état civil";
            case "ADMIN" -> PROFIL_ADMINISTRATEUR.nom();
            default -> PROFIL_CITOYEN.nom();
        };
    }

    /** Noms réservés par Keycloak ou par l'application (jetons de service). */
    public static final Set<String> CODES_RESERVES = Set.of("SERVICE", "OFFLINE_ACCESS", "UMA_AUTHORIZATION");

    static Set<String> union(Set<String> a, Set<String> b) {
        return Stream.concat(a.stream(), b.stream()).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
