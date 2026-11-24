package sn.etatcivil.portail.web;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Espaces des agents : files d'attente par rôle et fiche commune d'une demande. */
@Controller
class BackOfficeController {

    static final Map<String, String> STATUTS = new LinkedHashMap<>();
    static {
        STATUTS.put("SOUMISE", "Soumise");
        STATUTS.put("A_PAYER", "À payer");
        STATUTS.put("PAYEE", "Payée");
        STATUTS.put("ACTE_TROUVE", "Acte retrouvé");
        STATUTS.put("ACTE_INTROUVABLE", "Acte non retrouvé");
        STATUTS.put("SIGNEE", "Signée");
        STATUTS.put("LIVREE", "Livrée");
        STATUTS.put("REJETEE", "Rejetée");
        STATUTS.put("ANNULEE", "Annulée");
    }

    private final Api api;

    BackOfficeController(Api api) {
        this.api = api;
    }

    // ---------------------------------------------------------------- files d'attente

    /** Filtres communs des files (comme les écrans du délégué et de l'officier de la version 1). */
    @ModelAttribute("filtre")
    Api.FiltreDemandes filtre(@RequestParam(required = false) String reference,
                              @RequestParam(required = false) Long typeDocument,
                              @RequestParam(required = false) String format,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return new Api.FiltreDemandes(reference, typeDocument, format == null || format.isBlank() ? null : format, du, au);
    }

    @GetMapping("/delegue")
    String delegue(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Demandes à examiner", "Vérifiez la cohérence de chaque demande avant de l'ouvrir au paiement.",
                List.of("SOUMISE"), f, page, List.of("SOUMISE", "A_PAYER", "SIGNEE", "LIVREE", "REJETEE"));
    }

    @GetMapping("/delegue/livraisons")
    String livraisons(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Documents signés", "Les documents papier sont à imprimer puis à expédier (ou à remettre à la "
                + "mission diplomatique indiquée).", List.of("SIGNEE"), f, page, List.of("SIGNEE", "LIVREE"));
    }

    @GetMapping("/registre")
    String registre(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Actes non retrouvés", "Retrouvez l'acte dans les registres papier, transcrivez-le puis relancez la "
                + "recherche ou rattachez-le par son numéro. Une validation d'acte relance aussi automatiquement les demandes.",
                List.of("ACTE_INTROUVABLE", "PAYEE"), f, page, List.of("PAYEE", "ACTE_INTROUVABLE", "ACTE_TROUVE"));
    }

    @GetMapping("/officier")
    String officier(@ModelAttribute("filtre") Api.FiltreDemandes f, Model model) {
        model.addAttribute("aSigner", api.demandes(List.of("ACTE_TROUVE"), f, 0).contenu());
        compteurs(model, f, List.of("ACTE_TROUVE", "SIGNEE", "REJETEE"));
        model.addAttribute("documents", documentsDuCatalogue());
        try {
            model.addAttribute("aValider", api.actes(null, "SAISI", null, 0).contenu());
        } catch (ApiException e) {
            model.addAttribute("registreIndisponible", true);
            model.addAttribute("aValider", List.of());
        }
        return "officier/accueil";
    }

    @GetMapping("/back-office/demandes")
    String toutes(@RequestParam(required = false) String statut, @ModelAttribute("filtre") Api.FiltreDemandes f,
                  @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("titre", "Toutes les demandes");
        model.addAttribute("page", api.demandes(statut == null || statut.isBlank() ? null : List.of(statut), f, page));
        model.addAttribute("tousStatuts", true);
        model.addAttribute("statuts", STATUTS);
        model.addAttribute("statut", statut);
        compteurs(model, f, List.copyOf(STATUTS.keySet()));
        model.addAttribute("documents", documentsDuCatalogue());
        return "agent/demandes";
    }

    @GetMapping("/back-office/demandes/{id}")
    String fiche(@PathVariable UUID id, Model model) {
        Vues.Demande d = api.demande(id);
        model.addAttribute("d", d);
        model.addAttribute("paiements", api.paiements(id));
        if (d.acteId() != null && "ACTE_TROUVE".equals(d.statut())) {
            try {
                model.addAttribute("acte", api.acte(d.acteId()));
            } catch (ApiException e) {
                model.addAttribute("registreIndisponible", true);
            }
        }
        return "agent/demande";
    }

    // ---------------------------------------------------------------- actions

    @PostMapping("/delegue/demandes/{id}/validation")
    String valider(@PathVariable UUID id, RedirectAttributes flash) {
        api.action(id, "validation", null);
        flash.addFlashAttribute("succes", "Demande validée : le demandeur est invité à payer en ligne.");
        return "redirect:/delegue";
    }

    @PostMapping("/delegue/demandes/{id}/livraison")
    String livrer(@PathVariable UUID id, RedirectAttributes flash) {
        api.action(id, "livraison", null);
        flash.addFlashAttribute("succes", "Document marqué comme expédié.");
        return "redirect:/delegue/livraisons";
    }

    @PostMapping("/back-office/demandes/{id}/rejet")
    String rejeter(@PathVariable UUID id, @RequestParam String motif, RedirectAttributes flash) {
        api.action(id, "rejet", Map.of("motif", motif));
        flash.addFlashAttribute("succes", "Demande rejetée, le demandeur en est informé.");
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/registre/demandes/{id}/recherche")
    String relancer(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Demande d = api.actionAvecResultat(id, "recherche", null);
        if ("ACTE_TROUVE".equals(d.statut())) {
            flash.addFlashAttribute("succes", "Acte " + d.numeroActe() + " retrouvé : la demande part en signature.");
        } else {
            flash.addFlashAttribute("erreur", d.noteRecherche());
        }
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/registre/demandes/{id}/rattachement")
    String rattacher(@PathVariable UUID id, @RequestParam String numeroActe, RedirectAttributes flash) {
        api.actionAvecResultat(id, "rattachement", Map.of("numeroActe", numeroActe));
        flash.addFlashAttribute("succes", "Acte rattaché : la demande part en signature.");
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/officier/demandes/{id}/signature")
    String signer(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Demande d = api.actionAvecResultat(id, "signature", null);
        flash.addFlashAttribute("succes", "Document signé. Code de vérification : " + d.codeVerification());
        return "redirect:/officier";
    }

    @PostMapping("/officier/demandes/{id}/contestation")
    String contester(@PathVariable UUID id, @RequestParam String raison, RedirectAttributes flash) {
        api.action(id, "contestation", Map.of("raison", raison));
        flash.addFlashAttribute("succes", "La demande est renvoyée à l'agent du registre.");
        return "redirect:/officier";
    }

    @PostMapping("/officier/actes/{id}/validation")
    String validerActe(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Acte a = api.validerActe(id);
        flash.addFlashAttribute("succes", "Acte " + a.numeroActe() + " validé dans le registre national.");
        return "redirect:/officier";
    }

    private String file(Model model, String titre, String aide, List<String> statuts, Api.FiltreDemandes f, int page,
                        List<String> compteurs) {
        model.addAttribute("titre", titre);
        model.addAttribute("aide", aide);
        model.addAttribute("page", api.demandes(statuts, f, page));
        compteurs(model, f, compteurs);
        model.addAttribute("documents", documentsDuCatalogue());
        return "agent/demandes";
    }

    /** Nombre de demandes par statut pour les filtres en cours (cartes en tête de file). */
    private void compteurs(Model model, Api.FiltreDemandes f, List<String> statuts) {
        Map<String, Map.Entry<String, Long>> c = new LinkedHashMap<>();
        try {
            Map<String, Long> n = api.statistiquesDemandes(f);
            statuts.forEach(s -> c.put(s, Map.entry(STATUTS.get(s), n.getOrDefault(s, 0L))));
        } catch (ApiException e) {
            // compteurs secondaires : la file reste affichée
        }
        model.addAttribute("compteurs", c);
    }

    private List<Vues.TypeDocument> documentsDuCatalogue() {
        try {
            return api.documents().stream().sorted(java.util.Comparator.comparing(Vues.TypeDocument::libelle)).toList();
        } catch (ApiException e) {
            return List.of();
        }
    }
}
