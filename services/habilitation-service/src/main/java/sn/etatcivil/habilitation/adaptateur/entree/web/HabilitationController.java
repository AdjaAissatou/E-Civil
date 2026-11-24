package sn.etatcivil.habilitation.adaptateur.entree.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.habilitation.application.port.entree.*;
import sn.etatcivil.habilitation.domaine.*;
import sn.etatcivil.socle.securite.Permissions;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Adaptateur REST de l'administration des habilitations ; chaque route exige la permission correspondante. */
@RestController
@RequestMapping("/api/habilitations")
class HabilitationController {

    private static final String UTILISATEURS_OU_HABILITATIONS =
            Permissions.A_UTILISATEURS_GERER + " or " + Permissions.A_HABILITATIONS_GERER;
    private static final String TABLEAU = Permissions.A_STATISTIQUES_CONSULTER + " or " + UTILISATEURS_OU_HABILITATIONS;

    private final GererUtilisateurs utilisateurs;
    private final GererProfils profils;
    private final GererRoles roles;
    private final GererPermissions permissions;
    private final ConsulterJournal journal;

    HabilitationController(GererUtilisateurs utilisateurs, GererProfils profils, GererRoles roles,
                           GererPermissions permissions, ConsulterJournal journal) {
        this.utilisateurs = utilisateurs;
        this.profils = profils;
        this.roles = roles;
        this.permissions = permissions;
        this.journal = journal;
    }

    // ------------------------------------------------------------------ vues JSON
    public record PageReponse<T>(List<T> contenu, int numero, int taille, long total, int nombrePages) {
        static <T> PageReponse<T> de(PageResultat<T> p) {
            return new PageReponse<>(p.contenu(), p.numero(), p.taille(), p.total(), p.nombrePages());
        }
    }

    public record UtilisateurReponse(String id, String email, String prenom, String nom, String nomComplet, boolean actif,
                                     Instant dateCreation, List<ProfilResume> profils) {
        static UtilisateurReponse de(Utilisateur u) {
            return new UtilisateurReponse(u.id(), u.email(), u.prenom(), u.nom(), u.nomComplet(), u.actif(),
                    u.dateCreation(), u.profils());
        }
    }

    public record RoleReponse(String code, String libelle, String description, Set<String> permissions, boolean deBase,
                              List<ProfilResume> profils) {
        static RoleReponse de(Role r, List<Profil> profils) {
            return new RoleReponse(r.code(), r.libelle(), r.description(), r.permissions(), r.deBase(),
                    profils.stream().map(p -> new ProfilResume(p.id(), p.nom())).toList());
        }
    }

    public record ProfilReponse(String id, String nom, String description, Set<String> roles, boolean parDefaut,
                                int nombreMembres, List<UtilisateurReponse> membres) {
        static ProfilReponse de(GererProfils.ProfilDetaille d, boolean avecMembres) {
            Profil p = d.profil();
            return new ProfilReponse(p.id(), p.nom(), p.description(), p.roles(), p.parDefaut(), d.membres().size(),
                    avecMembres ? d.membres().stream().map(UtilisateurReponse::de).toList() : List.of());
        }
    }

    public record OperationReponse(Instant date, String auteur, TypeOperation type, String libelleType, String cible,
                                   String detail) {
        static OperationReponse de(Operation o) {
            return new OperationReponse(o.date(), o.auteur(), o.type(), o.type().libelle(), o.cible(), o.detail());
        }
    }

    public record FicheReponse(UtilisateurReponse utilisateur, List<RoleReponse> roles, List<Permission> permissions,
                               List<OperationReponse> historique) {}

    public record SyntheseReponse(long utilisateurs, int profils, int roles, int permissions,
                                  List<OperationReponse> dernieresOperations) {}

    // ------------------------------------------------------------------ requêtes
    public record NouvelUtilisateurRequete(String email, String prenom, String nom, Set<String> profils, String motDePasse) {}
    public record ActivationRequete(boolean actif) {}
    public record MotDePasseRequete(String motDePasse) {}
    public record ProfilsRequete(Set<String> profils) {}
    public record ProfilRequete(String nom, String description, Set<String> roles) {}
    public record RoleRequete(String code, String libelle, String description, Set<String> permissions) {}
    public record PermissionRequete(String libelle, String description, String avantage, String interdiction) {}
    public record Cree(String id) {}

    // ------------------------------------------------------------------ tableau de bord et journal
    @GetMapping("/synthese")
    @PreAuthorize(TABLEAU)
    SyntheseReponse synthese() {
        ConsulterJournal.Synthese s = journal.synthese();
        return new SyntheseReponse(s.utilisateurs(), s.profils(), s.roles(), s.permissions(),
                s.dernieresOperations().stream().map(OperationReponse::de).toList());
    }

    @GetMapping("/journal")
    @PreAuthorize(Permissions.A_JOURNAL_CONSULTER)
    PageReponse<OperationReponse> journal(@RequestParam(required = false) String recherche,
                                          @RequestParam(defaultValue = "0") int page) {
        return PageReponse.de(journal.journal(recherche, new PageDemandee(page, 30)).map(OperationReponse::de));
    }

    // ------------------------------------------------------------------ utilisateurs
    @GetMapping("/utilisateurs")
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    PageReponse<UtilisateurReponse> utilisateurs(@RequestParam(required = false) String recherche,
                                                 @RequestParam(defaultValue = "0") int page) {
        return PageReponse.de(utilisateurs.utilisateurs(recherche, new PageDemandee(page, 20)).map(UtilisateurReponse::de));
    }

    @GetMapping("/utilisateurs/{id}")
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    FicheReponse fiche(@PathVariable String id) {
        GererUtilisateurs.FicheUtilisateur f = utilisateurs.fiche(id);
        return new FicheReponse(UtilisateurReponse.de(f.utilisateur()),
                f.roles().stream().map(r -> RoleReponse.de(r, List.of())).toList(), f.permissions(),
                f.historique().stream().map(OperationReponse::de).toList());
    }

    @PostMapping("/utilisateurs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    Cree creerUtilisateur(@RequestBody NouvelUtilisateurRequete r, @AuthenticationPrincipal Jwt jwt) {
        return new Cree(utilisateurs.creerUtilisateur(
                new NouvelUtilisateur(r.email(), r.prenom(), r.nom(), r.profils(), r.motDePasse()), auteur(jwt)));
    }

    @PostMapping("/utilisateurs/{id}/activation")
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    void activer(@PathVariable String id, @RequestBody ActivationRequete r, @AuthenticationPrincipal Jwt jwt) {
        utilisateurs.activer(id, r.actif(), auteur(jwt));
    }

    @PostMapping("/utilisateurs/{id}/mot-de-passe")
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    void motDePasse(@PathVariable String id, @RequestBody MotDePasseRequete r, @AuthenticationPrincipal Jwt jwt) {
        utilisateurs.reinitialiserMotDePasse(id, r.motDePasse(), auteur(jwt));
    }

    @PutMapping("/utilisateurs/{id}/profils")
    @PreAuthorize(Permissions.A_UTILISATEURS_GERER)
    void definirProfils(@PathVariable String id, @RequestBody ProfilsRequete r, @AuthenticationPrincipal Jwt jwt) {
        utilisateurs.definirProfils(id, r.profils(), auteur(jwt));
    }

    // ------------------------------------------------------------------ profils
    @GetMapping("/profils")
    @PreAuthorize(UTILISATEURS_OU_HABILITATIONS)
    List<ProfilReponse> profils() {
        return profils.profils().stream().map(d -> ProfilReponse.de(d, false)).toList();
    }

    @GetMapping("/profils/{id}")
    @PreAuthorize(UTILISATEURS_OU_HABILITATIONS)
    ProfilReponse profil(@PathVariable String id) {
        return ProfilReponse.de(profils.profil(id), true);
    }

    @PostMapping("/profils")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    Cree creerProfil(@RequestBody ProfilRequete r, @AuthenticationPrincipal Jwt jwt) {
        return new Cree(profils.creerProfil(r.nom(), r.description(), r.roles(), auteur(jwt)));
    }

    @PutMapping("/profils/{id}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void modifierProfil(@PathVariable String id, @RequestBody ProfilRequete r, @AuthenticationPrincipal Jwt jwt) {
        profils.modifierProfil(id, r.nom(), r.description(), r.roles(), auteur(jwt));
    }

    @DeleteMapping("/profils/{id}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void supprimerProfil(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        profils.supprimerProfil(id, auteur(jwt));
    }

    // ------------------------------------------------------------------ rôles
    @GetMapping("/roles")
    @PreAuthorize(UTILISATEURS_OU_HABILITATIONS)
    List<RoleReponse> roles() {
        return roles.roles().stream().map(d -> RoleReponse.de(d.role(), d.profils())).toList();
    }

    @GetMapping("/roles/{code}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    RoleReponse role(@PathVariable String code) {
        GererRoles.RoleDetaille d = roles.role(code);
        return RoleReponse.de(d.role(), d.profils());
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    Cree creerRole(@RequestBody RoleRequete r, @AuthenticationPrincipal Jwt jwt) {
        return new Cree(roles.creerRole(r.code(), r.libelle(), r.description(), r.permissions(), auteur(jwt)));
    }

    @PutMapping("/roles/{code}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void modifierRole(@PathVariable String code, @RequestBody RoleRequete r, @AuthenticationPrincipal Jwt jwt) {
        roles.modifierRole(code, r.libelle(), r.description(), r.permissions(), auteur(jwt));
    }

    @DeleteMapping("/roles/{code}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void supprimerRole(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
        roles.supprimerRole(code, auteur(jwt));
    }

    @PutMapping("/matrice")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void matrice(@RequestBody Map<String, Set<String>> permissionsParRole, @AuthenticationPrincipal Jwt jwt) {
        roles.modifierMatrice(permissionsParRole, auteur(jwt));
    }

    // ------------------------------------------------------------------ permissions
    @GetMapping("/permissions")
    @PreAuthorize(UTILISATEURS_OU_HABILITATIONS)
    List<Permission> permissions() {
        return permissions.permissions();
    }

    @PutMapping("/permissions/{code}")
    @PreAuthorize(Permissions.A_HABILITATIONS_GERER)
    void modifierPermission(@PathVariable String code, @RequestBody PermissionRequete r, @AuthenticationPrincipal Jwt jwt) {
        permissions.modifierPermission(code, r.libelle(), r.description(), r.avantage(), r.interdiction(), auteur(jwt));
    }

    private static Administrateur auteur(Jwt jwt) {
        UtilisateurJwt u = UtilisateurJwt.depuis(jwt);
        return new Administrateur(u.id(), u.nomComplet(), u.email());
    }
}
