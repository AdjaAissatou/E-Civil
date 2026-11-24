package sn.etatcivil.habilitation.adaptateur.sortie.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIdentites;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIndisponibleException;
import sn.etatcivil.habilitation.domaine.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * L'annuaire, c'est Keycloak (API d'administration) :
 * <ul>
 *   <li>une <b>permission</b> est un rôle du realm marqué {@code ecivil.type = permission} ;</li>
 *   <li>un <b>rôle</b> est un rôle composite du realm marqué {@code ecivil.type = role}, composé de permissions ;</li>
 *   <li>un <b>profil</b> est un groupe : ses membres reçoivent ses rôles, donc leurs permissions.</li>
 * </ul>
 * Keycloak calcule ainsi lui-même les permissions effectives et les inscrit dans chaque jeton.
 */
@Component
class AnnuaireKeycloak implements AnnuaireIdentites {

    static final String TYPE = "ecivil.type";
    static final String LIBELLE = "ecivil.libelle";
    static final String AVANTAGE = "ecivil.avantage";
    static final String INTERDICTION = "ecivil.interdiction";
    static final String FAMILLE = "ecivil.famille";
    static final String DE_BASE = "ecivil.deBase";
    static final String DESCRIPTION = "ecivil.description";
    private static final String COMPTE_DE_SERVICE = "service-account-";
    private static final int MAXIMUM = 5000;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record RoleJson(String id, String name, String description, Boolean composite, Map<String, List<String>> attributes) {
        String attribut(String cle) {
            List<String> v = attributes == null ? null : attributes.get(cle);
            return v == null || v.isEmpty() ? null : v.get(0);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GroupeJson(String id, String name, Map<String, List<String>> attributes) {
        String attribut(String cle) {
            List<String> v = attributes == null ? null : attributes.get(cle);
            return v == null || v.isEmpty() ? null : v.get(0);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CompteJson(String id, String username, String email, String firstName, String lastName, Boolean enabled,
                      Long createdTimestamp) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RealmJson(RoleJson defaultRole) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Identifiant(String type, String value, boolean temporary) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record NouveauCompteJson(String username, String email, String firstName, String lastName, boolean enabled,
                             boolean emailVerified, List<Identifiant> credentials) {}

    private static final ParameterizedTypeReference<List<RoleJson>> ROLES = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<GroupeJson>> GROUPES = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<CompteJson>> COMPTES = new ParameterizedTypeReference<>() {};

    private final RestClient http;
    private final JetonAdministration jeton;

    AnnuaireKeycloak(RestClient.Builder builder,
                     @Value("${ecivil.keycloak.url:http://localhost:8180}") String url,
                     @Value("${ecivil.keycloak.realm:ecivil}") String realm,
                     @Value("${KEYCLOAK_ADMIN:admin}") String administrateur,
                     @Value("${KEYCLOAK_ADMIN_PASSWORD:}") String motDePasse) {
        var fabrique = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        fabrique.setReadTimeout(Duration.ofSeconds(20));
        this.jeton = new JetonAdministration(builder.clone().baseUrl(url).requestFactory(fabrique).build(), administrateur, motDePasse);
        this.http = builder.clone().baseUrl(url + "/admin/realms/" + realm).requestFactory(fabrique)
                .requestInterceptor((requete, corps, execution) -> {
                    requete.getHeaders().setBearerAuth(jeton.valeur());
                    return execution.execute(requete, corps);
                }).build();
    }

    // ================================================================== permissions et rôles

    @Override
    public List<Permission> permissions() {
        return rolesDuRealm().stream().filter(r -> "permission".equals(r.attribut(TYPE)))
                .map(r -> new Permission(r.name(), valeur(r.attribut(LIBELLE), r.name()), r.description(),
                        r.attribut(AVANTAGE), r.attribut(INTERDICTION), r.attribut(FAMILLE)))
                .toList();
    }

    @Override
    public void enregistrerPermission(Permission p) {
        Map<String, List<String>> attributs = attributs(TYPE, "permission", LIBELLE, p.libelle(), AVANTAGE, p.avantage(),
                INTERDICTION, p.interdiction(), FAMILLE, p.famille());
        RoleJson corps = new RoleJson(null, p.code(), p.description(), null, attributs);
        if (nomDeRoleUtilise(p.code())) appel(() -> http.put().uri("/roles/{n}", p.code()).body(corps).retrieve().toBodilessEntity());
        else appel(() -> http.post().uri("/roles").body(corps).retrieve().toBodilessEntity());
    }

    @Override
    public List<Role> roles() {
        Set<String> permissions = new HashSet<>(Catalogue.CODES_PERMISSIONS);
        return rolesDuRealm().stream().filter(this::estRoleMetier).map(r -> versRole(r, permissions)).toList();
    }

    @Override
    public Optional<Role> role(String code) {
        return roleJson(code).filter(this::estRoleMetier).map(r -> versRole(r, Catalogue.CODES_PERMISSIONS));
    }

    @Override
    public boolean nomDeRoleUtilise(String code) {
        return roleJson(code).isPresent();
    }

    @Override
    public void creerRole(Role r) {
        appel(() -> http.post().uri("/roles").body(roleJson(r)).retrieve().toBodilessEntity());
        synchroniserPermissions(r);
    }

    @Override
    public void modifierRole(Role r) {
        appel(() -> http.put().uri("/roles/{n}", r.code()).body(roleJson(r)).retrieve().toBodilessEntity());
        synchroniserPermissions(r);
    }

    @Override
    public void supprimerRole(String code) {
        appel(() -> http.delete().uri("/roles/{n}", code).retrieve().toBodilessEntity());
    }

    // ================================================================== profils

    @Override
    public List<Profil> profils() {
        Set<String> parDefaut = groupesParDefaut();
        Set<String> metiers = roles().stream().map(Role::code).collect(Collectors.toSet());
        return lire(() -> http.get().uri(u -> u.path("/groups").queryParam("briefRepresentation", false)
                .queryParam("max", MAXIMUM).build()).retrieve().body(GROUPES))
                .stream().map(g -> versProfil(g, parDefaut, metiers)).toList();
    }

    @Override
    public Optional<Profil> profil(String id) {
        try {
            GroupeJson g = appel(() -> http.get().uri("/groups/{id}", id).retrieve().body(GroupeJson.class));
            Set<String> metiers = roles().stream().map(Role::code).collect(Collectors.toSet());
            return Optional.ofNullable(g).map(x -> versProfil(x, groupesParDefaut(), metiers));
        } catch (ProfilOuCompteIntrouvable e) {
            return Optional.empty();
        }
    }

    @Override
    public String creerProfil(Profil p) {
        GroupeJson corps = new GroupeJson(null, p.nom(), attributs(DESCRIPTION, p.description()));
        ResponseEntity<Void> r = appel(() -> http.post().uri("/groups").body(corps).retrieve().toBodilessEntity());
        String id = identifiantCree(r);
        synchroniserRolesDuProfil(id, p.roles());
        if (p.parDefaut()) definirProfilParDefaut(id);
        return id;
    }

    @Override
    public void modifierProfil(Profil p) {
        GroupeJson corps = new GroupeJson(p.id(), p.nom(), attributs(DESCRIPTION, p.description()));
        appel(() -> http.put().uri("/groups/{id}", p.id()).body(corps).retrieve().toBodilessEntity());
        synchroniserRolesDuProfil(p.id(), p.roles());
    }

    @Override
    public void supprimerProfil(String id) {
        appel(() -> http.delete().uri("/groups/{id}", id).retrieve().toBodilessEntity());
    }

    @Override
    public List<Utilisateur> membres(String profilId) {
        return lire(() -> http.get().uri(u -> u.path("/groups/{id}/members").queryParam("first", 0)
                        .queryParam("max", MAXIMUM).queryParam("briefRepresentation", true).build(profilId))
                .retrieve().body(COMPTES)).stream()
                .filter(c -> !estCompteDeService(c)).map(c -> versUtilisateur(c, List.of())).toList();
    }

    @Override
    public void definirProfilParDefaut(String profilId) {
        appel(() -> http.put().uri("/default-groups/{id}", profilId).retrieve().toBodilessEntity());
    }

    // ================================================================== comptes

    @Override
    public PageResultat<Utilisateur> utilisateurs(String recherche, PageDemandee page) {
        Function<UriBuilder, UriBuilder> filtre = u -> recherche == null || recherche.isBlank() ? u : u.queryParam("search", recherche);
        Integer total = appel(() -> http.get().uri(u -> filtre.apply(u.path("/users/count")).build()).retrieve().body(Integer.class));
        int services = recherche == null || recherche.isBlank() ? comptesDeService() : 0;
        List<CompteJson> comptes = lire(() -> http.get().uri(u -> filtre.apply(u.path("/users")
                        .queryParam("first", page.numero() * page.taille()).queryParam("max", page.taille() + services)
                        .queryParam("briefRepresentation", true)).build())
                .retrieve().body(COMPTES)).stream().filter(c -> !estCompteDeService(c)).limit(page.taille()).toList();
        return new PageResultat<>(comptes.stream().map(c -> versUtilisateur(c, profilsDe(c.id()))).toList(),
                page.numero(), page.taille(), Math.max(0, (total == null ? 0 : total) - services));
    }

    @Override
    public Optional<Utilisateur> utilisateur(String id) {
        try {
            CompteJson c = appel(() -> http.get().uri("/users/{id}", id).retrieve().body(CompteJson.class));
            return Optional.ofNullable(c).filter(x -> !estCompteDeService(x)).map(x -> versUtilisateur(x, profilsDe(id)));
        } catch (ProfilOuCompteIntrouvable e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean emailUtilise(String email) {
        return !lire(() -> http.get().uri(u -> u.path("/users").queryParam("email", email).queryParam("exact", true).build())
                .retrieve().body(COMPTES)).isEmpty();
    }

    @Override
    public String creerUtilisateur(NouvelUtilisateur n) {
        var corps = new NouveauCompteJson(n.email(), n.email(), n.prenom(), n.nom(), true, true,
                List.of(new Identifiant("password", n.motDePasseProvisoire(), true)));
        return identifiantCree(appel(() -> http.post().uri("/users").body(corps).retrieve().toBodilessEntity()));
    }

    @Override
    public void activer(String utilisateur, boolean actif) {
        appel(() -> http.put().uri("/users/{id}", utilisateur).body(Map.of("enabled", actif)).retrieve().toBodilessEntity());
    }

    @Override
    public void motDePasseProvisoire(String utilisateur, String motDePasse) {
        appel(() -> http.put().uri("/users/{id}/reset-password", utilisateur)
                .body(new Identifiant("password", motDePasse, true)).retrieve().toBodilessEntity());
    }

    @Override
    public void ajouterAuProfil(String utilisateur, String profil) {
        appel(() -> http.put().uri("/users/{u}/groups/{g}", utilisateur, profil).retrieve().toBodilessEntity());
    }

    @Override
    public void retirerDuProfil(String utilisateur, String profil) {
        appel(() -> http.delete().uri("/users/{u}/groups/{g}", utilisateur, profil).retrieve().toBodilessEntity());
    }

    @Override
    public Set<String> rolesEffectifs(String utilisateur) {
        return lire(() -> http.get().uri("/users/{id}/role-mappings/realm/composite", utilisateur).retrieve().body(ROLES))
                .stream().map(RoleJson::name).collect(Collectors.toSet());
    }

    @Override
    public void deconnecter(String utilisateur) {
        appel(() -> http.post().uri("/users/{id}/logout", utilisateur).retrieve().toBodilessEntity());
    }

    // ================================================================== migration

    @Override
    public List<String> utilisateursAvecRoleDirect(String role) {
        if (!nomDeRoleUtilise(role)) return List.of();
        return lire(() -> http.get().uri(u -> u.path("/roles/{r}/users").queryParam("first", 0).queryParam("max", MAXIMUM)
                        .build(role)).retrieve().body(COMPTES))
                .stream().filter(c -> !estCompteDeService(c)).map(CompteJson::id).toList();
    }

    @Override
    public Set<String> rolesDirects(String utilisateur) {
        return lire(() -> http.get().uri("/users/{id}/role-mappings/realm", utilisateur).retrieve().body(ROLES))
                .stream().map(RoleJson::name).collect(Collectors.toSet());
    }

    @Override
    public void retirerRolesDirects(String utilisateur, Set<String> roles) {
        List<RoleJson> reps = roles.stream().map(this::roleJson).flatMap(Optional::stream).toList();
        if (reps.isEmpty()) return;
        appel(() -> http.method(HttpMethod.DELETE).uri("/users/{id}/role-mappings/realm", utilisateur).body(reps)
                .retrieve().toBodilessEntity());
    }

    @Override
    public void retirerDesRolesParDefaut(String role) {
        String parDefaut = roleParDefaut();
        boolean present = lire(() -> http.get().uri("/roles/{r}/composites/realm", parDefaut).retrieve().body(ROLES))
                .stream().anyMatch(r -> r.name().equals(role));
        if (!present) return;
        List<RoleJson> reps = roleJson(role).map(List::of).orElse(List.of());
        appel(() -> http.method(HttpMethod.DELETE).uri("/roles/{r}/composites", parDefaut).body(reps)
                .retrieve().toBodilessEntity());
    }

    @Override
    public String roleParDefaut() {
        RealmJson r = appel(() -> http.get().uri("").retrieve().body(RealmJson.class));
        return r != null && r.defaultRole() != null ? r.defaultRole().name() : "default-roles-ecivil";
    }

    // ================================================================== interne

    private List<RoleJson> rolesDuRealm() {
        return lire(() -> http.get().uri(u -> u.path("/roles").queryParam("briefRepresentation", false)
                .queryParam("first", 0).queryParam("max", MAXIMUM).build()).retrieve().body(ROLES));
    }

    private Optional<RoleJson> roleJson(String nom) {
        try {
            return Optional.ofNullable(appel(() -> http.get().uri("/roles/{n}", nom).retrieve().body(RoleJson.class)));
        } catch (ProfilOuCompteIntrouvable e) {
            return Optional.empty();
        }
    }

    /** Un rôle métier est marqué comme tel ; les rôles de base le sont même avant l'initialisation. */
    private boolean estRoleMetier(RoleJson r) {
        return "role".equals(r.attribut(TYPE)) || (r.attribut(TYPE) == null && Catalogue.CODES_ROLES_DE_BASE.contains(r.name()));
    }

    private Role versRole(RoleJson r, Set<String> codesPermissions) {
        Set<String> permissions = lire(() -> http.get().uri("/roles/{n}/composites/realm", r.name()).retrieve().body(ROLES))
                .stream().map(RoleJson::name).filter(codesPermissions::contains).collect(Collectors.toSet());
        boolean deBase = "true".equals(r.attribut(DE_BASE)) || Catalogue.CODES_ROLES_DE_BASE.contains(r.name());
        return new Role(r.name(), valeur(r.attribut(LIBELLE), r.name()), r.description(), permissions, deBase);
    }

    private RoleJson roleJson(Role r) {
        return new RoleJson(null, r.code(), r.description(), null,
                attributs(TYPE, "role", LIBELLE, r.libelle(), DE_BASE, String.valueOf(r.deBase())));
    }

    /** Rend les permissions composant le rôle identiques à celles voulues (sans toucher à d'autres composites). */
    private void synchroniserPermissions(Role r) {
        Map<String, RoleJson> permissions = rolesDuRealm().stream().filter(x -> Catalogue.CODES_PERMISSIONS.contains(x.name()))
                .collect(Collectors.toMap(RoleJson::name, x -> x));
        Set<String> actuelles = lire(() -> http.get().uri("/roles/{n}/composites/realm", r.code()).retrieve().body(ROLES))
                .stream().map(RoleJson::name).filter(Catalogue.CODES_PERMISSIONS::contains).collect(Collectors.toSet());
        List<RoleJson> ajouts = r.permissions().stream().filter(p -> !actuelles.contains(p)).map(permissions::get)
                .filter(Objects::nonNull).toList();
        List<RoleJson> retraits = actuelles.stream().filter(p -> !r.permissions().contains(p)).map(permissions::get)
                .filter(Objects::nonNull).toList();
        if (!ajouts.isEmpty()) {
            appel(() -> http.post().uri("/roles/{n}/composites", r.code()).body(ajouts).retrieve().toBodilessEntity());
        }
        if (!retraits.isEmpty()) {
            appel(() -> http.method(HttpMethod.DELETE).uri("/roles/{n}/composites", r.code()).body(retraits)
                    .retrieve().toBodilessEntity());
        }
    }

    private void synchroniserRolesDuProfil(String id, Set<String> voulus) {
        Set<String> metiers = roles().stream().map(Role::code).collect(Collectors.toSet());
        Set<String> actuels = lire(() -> http.get().uri("/groups/{id}/role-mappings/realm", id).retrieve().body(ROLES))
                .stream().map(RoleJson::name).filter(metiers::contains).collect(Collectors.toSet());
        List<RoleJson> ajouts = voulus.stream().filter(r -> !actuels.contains(r)).map(this::roleJson)
                .flatMap(Optional::stream).toList();
        List<RoleJson> retraits = actuels.stream().filter(r -> !voulus.contains(r)).map(this::roleJson)
                .flatMap(Optional::stream).toList();
        if (!ajouts.isEmpty()) {
            appel(() -> http.post().uri("/groups/{id}/role-mappings/realm", id).body(ajouts).retrieve().toBodilessEntity());
        }
        if (!retraits.isEmpty()) {
            appel(() -> http.method(HttpMethod.DELETE).uri("/groups/{id}/role-mappings/realm", id).body(retraits)
                    .retrieve().toBodilessEntity());
        }
    }

    private Profil versProfil(GroupeJson g, Set<String> parDefaut, Set<String> metiers) {
        Set<String> roles = lire(() -> http.get().uri("/groups/{id}/role-mappings/realm", g.id()).retrieve().body(ROLES))
                .stream().map(RoleJson::name).filter(metiers::contains).collect(Collectors.toSet());
        return new Profil(g.id(), g.name(), g.attribut(DESCRIPTION), roles, parDefaut.contains(g.id()));
    }

    private Set<String> groupesParDefaut() {
        return lire(() -> http.get().uri("/default-groups").retrieve().body(GROUPES)).stream().map(GroupeJson::id)
                .collect(Collectors.toSet());
    }

    private List<ProfilResume> profilsDe(String utilisateur) {
        return lire(() -> http.get().uri("/users/{id}/groups", utilisateur).retrieve().body(GROUPES)).stream()
                .map(g -> new ProfilResume(g.id(), g.name())).toList();
    }

    private int comptesDeService() {
        return (int) lire(() -> http.get().uri(u -> u.path("/users").queryParam("search", COMPTE_DE_SERVICE)
                .queryParam("briefRepresentation", true).queryParam("max", 100).build()).retrieve().body(COMPTES))
                .stream().filter(AnnuaireKeycloak::estCompteDeService).count();
    }

    private static boolean estCompteDeService(CompteJson c) {
        return c.username() != null && c.username().startsWith(COMPTE_DE_SERVICE);
    }

    private static Utilisateur versUtilisateur(CompteJson c, List<ProfilResume> profils) {
        return new Utilisateur(c.id(), c.email() == null ? c.username() : c.email(), c.firstName(), c.lastName(),
                !Boolean.FALSE.equals(c.enabled()),
                c.createdTimestamp() == null ? null : Instant.ofEpochMilli(c.createdTimestamp()), profils);
    }

    private static String identifiantCree(ResponseEntity<Void> r) {
        URI lieu = r.getHeaders().getLocation();
        if (lieu == null) throw new IllegalStateException("Keycloak n'a pas indiqué l'identifiant créé");
        String chemin = lieu.getPath();
        return chemin.substring(chemin.lastIndexOf('/') + 1);
    }

    private static Map<String, List<String>> attributs(String... clesValeurs) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        for (int i = 0; i < clesValeurs.length; i += 2) {
            if (clesValeurs[i + 1] != null && !clesValeurs[i + 1].isBlank()) m.put(clesValeurs[i], List.of(clesValeurs[i + 1]));
        }
        return m;
    }

    private static String valeur(String v, String defaut) {
        return v == null || v.isBlank() ? defaut : v;
    }

    private <T> List<T> lire(Supplier<List<T>> appel) {
        List<T> r = appel(appel);
        return r == null ? List.of() : r;
    }

    /** Traduit les erreurs HTTP de Keycloak en erreurs compréhensibles. */
    private <T> T appel(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (HttpClientErrorException e) {
            HttpStatusCode s = e.getStatusCode();
            if (s.value() == 401) jeton.oublier();
            if (s.value() == 404) throw new ProfilOuCompteIntrouvable();
            if (s.value() == 409) throw new RegleMetierException("Cet élément existe déjà dans l'annuaire");
            if (s.value() == 400) {
                String message = e.getResponseBodyAsString();
                throw new RegleMetierException(message.contains("invalidPassword")
                        ? "Le mot de passe ne respecte pas la politique de sécurité (8 caractères au moins, différent de l'email)"
                        : "L'annuaire a refusé la modification");
            }
            throw new AnnuaireIndisponibleException("L'annuaire a refusé la requête (" + s.value() + ")", e);
        } catch (RestClientException | IllegalStateException e) {
            throw new AnnuaireIndisponibleException("L'annuaire des identités ne répond pas", e);
        }
    }

    /** Élément absent de l'annuaire : les lectures le traduisent en « vide », les modifications en refus. */
    static class ProfilOuCompteIntrouvable extends RegleMetierException {
        ProfilOuCompteIntrouvable() {
            super("Élément introuvable dans l'annuaire");
        }
    }
}
