package sn.etatcivil.socle.securite;

/**
 * Codes des permissions vérifiées par les services. Keycloak les inscrit dans le jeton de chaque personne
 * (rôles du realm, obtenus par ses profils et ses rôles) ; ils deviennent des autorités « ROLE_… ».
 * Les constantes {@code A_…} s'utilisent directement dans {@code @PreAuthorize}.
 */
public final class Permissions {
    private Permissions() {}

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

    /** Rôle technique des comptes de service (appels entre microservices). */
    public static final String SERVICE = "SERVICE";

    // Expressions pour @PreAuthorize
    public static final String A_DEMANDES_DEPOSER = "hasRole('DEMANDES_DEPOSER')";
    public static final String A_DEMANDES_CONSULTER = "hasRole('DEMANDES_CONSULTER')";
    public static final String A_DEMANDES_VALIDER = "hasRole('DEMANDES_VALIDER')";
    public static final String A_DEMANDES_RECHERCHER = "hasRole('DEMANDES_RECHERCHER')";
    public static final String A_DOCUMENTS_LIVRER = "hasRole('DOCUMENTS_LIVRER')";
    public static final String A_DOCUMENTS_SIGNER = "hasRole('DOCUMENTS_SIGNER')";
    public static final String A_REGISTRE_CONSULTER = "hasRole('REGISTRE_CONSULTER')";
    public static final String A_REGISTRE_SAISIR = "hasRole('REGISTRE_SAISIR')";
    public static final String A_REGISTRE_VALIDER = "hasRole('REGISTRE_VALIDER')";
    public static final String A_CATALOGUE_GERER = "hasRole('CATALOGUE_GERER')";
    public static final String A_STATISTIQUES_CONSULTER = "hasRole('STATISTIQUES_CONSULTER')";
    public static final String A_UTILISATEURS_GERER = "hasRole('UTILISATEURS_GERER')";
    public static final String A_HABILITATIONS_GERER = "hasRole('HABILITATIONS_GERER')";
    public static final String A_JOURNAL_CONSULTER = "hasRole('JOURNAL_CONSULTER')";
    public static final String A_SERVICE = "hasRole('SERVICE')";
}
