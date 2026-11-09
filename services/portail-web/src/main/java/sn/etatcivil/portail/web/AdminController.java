package sn.etatcivil.portail.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
class AdminController {

    private final Api api;
    private final String consoleKeycloak;

    AdminController(Api api, @Value("${ecivil.keycloak.url-publique}") String keycloak) {
        this.api = api;
        this.consoleKeycloak = keycloak + "/admin/ecivil/console/#/ecivil/users";
    }

    @GetMapping
    String tableauDeBord(Model model) {
        Map<String, Long> parStatut = api.statistiquesDemandes();
        Map<String, Map.Entry<String, Long>> lignes = new LinkedHashMap<>();
        BackOfficeController.STATUTS.forEach((code, libelle) -> lignes.put(code, Map.entry(libelle, parStatut.getOrDefault(code, 0L))));
        model.addAttribute("parStatut", lignes);
        model.addAttribute("total", parStatut.values().stream().mapToLong(Long::longValue).sum());
        try {
            model.addAttribute("registre", api.statistiquesRegistre());
        } catch (ApiException e) {
            model.addAttribute("registreIndisponible", true);
        }
        model.addAttribute("consoleKeycloak", consoleKeycloak);
        return "admin/accueil";
    }

    /** Les comptes et les rôles sont gérés dans Keycloak (serveur d'identité). */
    @GetMapping("/comptes")
    String comptes() {
        return "redirect:" + consoleKeycloak;
    }

    @GetMapping("/documents")
    String documents(Model model) {
        model.addAttribute("documents", api.tousLesDocuments());
        return "admin/documents";
    }

    @PostMapping("/documents/{id}")
    String modifier(@PathVariable Long id, @RequestParam int tarif, @RequestParam int delaiJours,
                    @RequestParam(defaultValue = "false") boolean actif, RedirectAttributes flash) {
        api.modifierDocument(id, tarif, delaiJours, actif);
        flash.addFlashAttribute("succes", "Document mis à jour");
        return "redirect:/admin/documents";
    }
}
