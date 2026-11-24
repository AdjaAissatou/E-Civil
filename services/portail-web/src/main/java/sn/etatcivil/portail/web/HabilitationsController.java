package sn.etatcivil.portail.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.Vues.Permission;
import sn.etatcivil.portail.clients.Vues.Role;

import java.util.*;

/**
 * Administration des habilitations : comptes, profils, rôles, permissions et journal. Les règles (dernier
 * administrateur, rôles de base, profil par défaut…) sont vérifiées par le service des habilitations ; ses
 * refus reviennent sur la page sous forme de message (voir {@link GestionErreursWeb}).
 */
@Controller
@RequestMapping("/admin")
class HabilitationsController {

    /** Ordre d'affichage des familles de permissions. */
    static final List<String> FAMILLES = List.of("Citoyen", "Instruction", "Registre", "Officier", "Administration");

    private final Api api;

    HabilitationsController(Api api) {
        this.api = api;
    }

    // ------------------------------------------------------------------ utilisateurs

    @GetMapping("/utilisateurs")
    String utilisateurs(@RequestParam(required = false) String recherche, @RequestParam(defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("comptes", api.comptes(recherche, Math.max(page, 0)));
        model.addAttribute("recherche", recherche);
        return "admin/utilisateurs";
    }

    @GetMapping("/utilisateurs/nouveau")
    String nouvelUtilisateur(Model model) {
        model.addAttribute("profils", api.profils());
        return "admin/nouvel-utilisateur";
    }

    @PostMapping("/utilisateurs")
    String creerUtilisateur(@RequestParam String email, @RequestParam String prenom, @RequestParam String nom,
                            @RequestParam(name = "profil", required = false) Set<String> profils,
                            @RequestParam String motDePasse, RedirectAttributes flash) {
        String id = api.creerCompte(email, prenom, nom, profils == null ? Set.of() : profils, motDePasse);
        flash.addFlashAttribute("succes", "Compte créé. Communiquez le mot de passe provisoire à " + email.trim()
                + " : il devra le changer à sa première connexion.");
        return "redirect:/admin/utilisateurs/" + id;
    }

    @GetMapping("/utilisateurs/{id}")
    String fiche(@PathVariable String id, Model model) {
        var fiche = api.compte(id);
        model.addAttribute("fiche", fiche);
        model.addAttribute("profils", api.profils());
        model.addAttribute("permissionsParFamille", parFamille(fiche.permissions()));
        return "admin/utilisateur";
    }

    @PostMapping("/utilisateurs/{id}/profils")
    String profilsDuCompte(@PathVariable String id, @RequestParam(name = "profil", required = false) Set<String> profils,
                           RedirectAttributes flash) {
        api.profilsDuCompte(id, profils == null ? Set.of() : profils);
        flash.addFlashAttribute("succes", "Profils enregistrés. La personne retrouvera ses nouveaux droits à sa prochaine connexion.");
        return "redirect:/admin/utilisateurs/" + id;
    }

    @PostMapping("/utilisateurs/{id}/activation")
    String activation(@PathVariable String id, @RequestParam boolean actif, RedirectAttributes flash) {
        api.activerCompte(id, actif);
        flash.addFlashAttribute("succes", actif ? "Compte réactivé" : "Compte désactivé : la personne ne peut plus se connecter");
        return "redirect:/admin/utilisateurs/" + id;
    }

    @PostMapping("/utilisateurs/{id}/mot-de-passe")
    String motDePasse(@PathVariable String id, @RequestParam String motDePasse, RedirectAttributes flash) {
        api.motDePasseProvisoire(id, motDePasse);
        flash.addFlashAttribute("succes", "Mot de passe provisoire enregistré : il devra être changé à la prochaine connexion.");
        return "redirect:/admin/utilisateurs/" + id;
    }

    // ------------------------------------------------------------------ profils

    @GetMapping("/profils")
    String profils(Model model) {
        model.addAttribute("profils", api.profils());
        model.addAttribute("roles", rolesParCode(api.roles()));
        return "admin/profils";
    }

    @GetMapping("/profils/nouveau")
    String nouveauProfil(Model model) {
        model.addAttribute("profil", null);
        model.addAttribute("roles", api.roles());
        model.addAttribute("permissions", permissionsParCode(api.permissions()));
        return "admin/profil";
    }

    @GetMapping("/profils/{id}")
    String profil(@PathVariable String id, Model model) {
        model.addAttribute("profil", api.profil(id));
        model.addAttribute("roles", api.roles());
        model.addAttribute("permissions", permissionsParCode(api.permissions()));
        return "admin/profil";
    }

    @PostMapping("/profils")
    String creerProfil(@RequestParam String nom, @RequestParam(required = false) String description,
                       @RequestParam(name = "role", required = false) Set<String> roles, RedirectAttributes flash) {
        String id = api.creerProfil(nom, description, roles);
        flash.addFlashAttribute("succes", "Profil « " + nom.trim() + " » créé. Attribuez-le depuis la fiche des utilisateurs.");
        return "redirect:/admin/profils/" + id;
    }

    @PostMapping("/profils/{id}")
    String modifierProfil(@PathVariable String id, @RequestParam String nom, @RequestParam(required = false) String description,
                          @RequestParam(name = "role", required = false) Set<String> roles, RedirectAttributes flash) {
        api.modifierProfil(id, nom, description, roles);
        flash.addFlashAttribute("succes", "Profil enregistré. Ses membres retrouveront leurs nouveaux droits à leur prochaine connexion.");
        return "redirect:/admin/profils/" + id;
    }

    @PostMapping("/profils/{id}/suppression")
    String supprimerProfil(@PathVariable String id, RedirectAttributes flash) {
        api.supprimerProfil(id);
        flash.addFlashAttribute("succes", "Profil supprimé");
        return "redirect:/admin/profils";
    }

    // ------------------------------------------------------------------ rôles

    @GetMapping("/roles")
    String roles(Model model) {
        model.addAttribute("roles", api.roles());
        model.addAttribute("permissions", permissionsParCode(api.permissions()));
        return "admin/roles";
    }

    @GetMapping("/roles/nouveau")
    String nouveauRole(Model model) {
        model.addAttribute("role", null);
        model.addAttribute("permissionsParFamille", parFamille(api.permissions()));
        return "admin/role";
    }

    @GetMapping("/roles/{code}")
    String role(@PathVariable String code, Model model) {
        model.addAttribute("role", api.role(code));
        model.addAttribute("permissionsParFamille", parFamille(api.permissions()));
        return "admin/role";
    }

    @PostMapping("/roles")
    String creerRole(@RequestParam String code, @RequestParam String libelle, @RequestParam(required = false) String description,
                     @RequestParam(name = "permission", required = false) Set<String> permissions, RedirectAttributes flash) {
        String cree = api.creerRole(code, libelle, description, permissions);
        flash.addFlashAttribute("succes", "Rôle « " + libelle.trim() + " » créé. Ajoutez-le à un profil pour l'attribuer.");
        return "redirect:/admin/roles/" + cree;
    }

    @PostMapping("/roles/{code}")
    String modifierRole(@PathVariable String code, @RequestParam String libelle, @RequestParam(required = false) String description,
                        @RequestParam(name = "permission", required = false) Set<String> permissions, RedirectAttributes flash) {
        api.modifierRole(code, libelle, description, permissions);
        flash.addFlashAttribute("succes", "Rôle enregistré");
        return "redirect:/admin/roles/" + code;
    }

    @PostMapping("/roles/{code}/suppression")
    String supprimerRole(@PathVariable String code, RedirectAttributes flash) {
        api.supprimerRole(code);
        flash.addFlashAttribute("succes", "Rôle supprimé");
        return "redirect:/admin/roles";
    }

    // ------------------------------------------------------------------ permissions

    @GetMapping("/permissions")
    String permissions(Model model) {
        model.addAttribute("roles", api.roles());
        model.addAttribute("permissionsParFamille", parFamille(api.permissions()));
        return "admin/permissions";
    }

    /** Matrice rôles × permissions : chaque case cochée arrive sous la forme « ROLE:PERMISSION ». */
    @PostMapping("/permissions/matrice")
    String matrice(@RequestParam(name = "case", required = false) List<String> cases, RedirectAttributes flash) {
        Map<String, Set<String>> matrice = new LinkedHashMap<>();
        api.roles().forEach(r -> matrice.put(r.code(), new TreeSet<>()));
        if (cases != null) {
            for (String c : cases) {
                int i = c.indexOf(':');
                if (i > 0 && matrice.containsKey(c.substring(0, i))) matrice.get(c.substring(0, i)).add(c.substring(i + 1));
            }
        }
        api.modifierMatrice(matrice);
        flash.addFlashAttribute("succes", "Matrice enregistrée. Les comptes concernés retrouveront leurs droits à leur prochaine connexion.");
        return "redirect:/admin/permissions";
    }

    @GetMapping("/permissions/{code}")
    String permission(@PathVariable String code, Model model) {
        Permission p = api.permissions().stream().filter(x -> x.code().equals(code)).findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
        model.addAttribute("permission", p);
        model.addAttribute("roles", api.roles().stream().filter(r -> r.accorde(code)).toList());
        return "admin/permission";
    }

    @PostMapping("/permissions/{code}")
    String modifierPermission(@PathVariable String code, @RequestParam String libelle,
                              @RequestParam(required = false) String description, @RequestParam(required = false) String avantage,
                              @RequestParam(required = false) String interdiction, RedirectAttributes flash) {
        api.modifierPermission(code, libelle, description, avantage, interdiction);
        flash.addFlashAttribute("succes", "Permission enregistrée");
        return "redirect:/admin/permissions/" + code;
    }

    // ------------------------------------------------------------------ journal

    @GetMapping("/journal")
    String journal(@RequestParam(required = false) String recherche, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("operations", api.journal(recherche, Math.max(page, 0)));
        model.addAttribute("recherche", recherche);
        return "admin/journal";
    }

    // ------------------------------------------------------------------ outils

    static Map<String, List<Permission>> parFamille(List<Permission> permissions) {
        Map<String, List<Permission>> r = new LinkedHashMap<>();
        FAMILLES.forEach(f -> r.put(f, new ArrayList<>()));
        permissions.forEach(p -> r.computeIfAbsent(p.famille() == null ? "Autres" : p.famille(), f -> new ArrayList<>()).add(p));
        r.values().removeIf(List::isEmpty);
        return r;
    }

    private static Map<String, Permission> permissionsParCode(List<Permission> permissions) {
        Map<String, Permission> r = new LinkedHashMap<>();
        permissions.forEach(p -> r.put(p.code(), p));
        return r;
    }

    private static Map<String, Role> rolesParCode(List<Role> roles) {
        Map<String, Role> r = new LinkedHashMap<>();
        roles.forEach(x -> r.put(x.code(), x));
        return r;
    }
}
