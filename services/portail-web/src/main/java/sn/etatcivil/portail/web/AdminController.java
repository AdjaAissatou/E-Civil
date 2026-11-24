package sn.etatcivil.portail.web;

import sn.etatcivil.portail.i18n.Textes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.securite.Utilisateur;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tableau de bord de l'administration et catalogue des documents (tarifs, délais). */
@Controller
@RequestMapping("/admin")
class AdminController {

    private final Api api;

    AdminController(Api api) {
        this.api = api;
    }

    /** Chaque bloc n'apparaît que si la personne a la permission correspondante ; un service absent n'empêche pas les autres. */
    @GetMapping
    String tableauDeBord(@ModelAttribute("moi") Utilisateur moi, Model model) {
        if (moi.peut("STATISTIQUES_CONSULTER")) {
            try {
                Map<String, Long> parStatut = api.statistiquesDemandes();
                Map<String, Map.Entry<String, Long>> lignes = new LinkedHashMap<>();
                BackOfficeController.STATUTS.forEach((code, libelle) ->
                        lignes.put(code, Map.entry(libelle, parStatut.getOrDefault(code, 0L))));
                model.addAttribute("parStatut", lignes);
                model.addAttribute("total", parStatut.values().stream().mapToLong(Long::longValue).sum());
            } catch (ApiException e) {
                model.addAttribute("demandesIndisponibles", true);
            }
            try {
                model.addAttribute("registre", api.statistiquesRegistre());
            } catch (ApiException e) {
                model.addAttribute("registreIndisponible", true);
            }
        }
        if (moi.peut("STATISTIQUES_CONSULTER") || moi.peut("UTILISATEURS_GERER") || moi.peut("HABILITATIONS_GERER")) {
            try {
                model.addAttribute("synthese", api.syntheseHabilitations());
            } catch (ApiException e) {
                model.addAttribute("habilitationsIndisponibles", true);
            }
        }
        return "admin/accueil";
    }

    @GetMapping("/documents")
    String documents(Model model) {
        var natures = api.natures(false);
        java.util.Map<Long, Integer> rang = new java.util.HashMap<>();
        for (int i = 0; i < natures.size(); i++) rang.put(natures.get(i).id(), i);
        model.addAttribute("documents", api.tousLesDocuments().stream()
                .sorted(java.util.Comparator.comparingInt((sn.etatcivil.portail.clients.Vues.TypeDocument t) -> rang.getOrDefault(t.natureId(), 999))
                        .thenComparing(sn.etatcivil.portail.clients.Vues.TypeDocument::libelle)).toList());
        return "admin/documents";
    }

    @PostMapping("/documents/{id}")
    String modifier(@PathVariable Long id, @RequestParam int tarif, @RequestParam int delaiJours,
                    @RequestParam(defaultValue = "false") boolean actif, RedirectAttributes flash) {
        api.modifierDocument(id, tarif, delaiJours, actif);
        flash.addFlashAttribute("succes", Textes.t("message.document_mis_a_jour"));
        return "redirect:/admin/documents";
    }
}
