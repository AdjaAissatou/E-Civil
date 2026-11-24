package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Transcription des registres papier dans le registre national. */
@Controller
@RequestMapping("/registre/actes")
class RegistreController {

    static final Map<String, String> TYPES = new LinkedHashMap<>(Map.of());
    static {
        TYPES.put("NAISSANCE", "Naissance");
        TYPES.put("MARIAGE", "Mariage");
        TYPES.put("DIVORCE", "Divorce");
        TYPES.put("DECES", "Décès");
        TYPES.put("RECONNAISSANCE", "Reconnaissance");
    }

    private final Api api;

    RegistreController(Api api) {
        this.api = api;
    }

    @GetMapping
    String liste(@RequestParam(required = false) String type, @RequestParam(required = false) String statut,
                 @RequestParam(required = false) String nom, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("actes", api.actes(type, statut, nom, page));
        model.addAttribute("types", TYPES);
        model.addAttribute("type", type);
        model.addAttribute("statut", statut);
        model.addAttribute("nom", nom);
        return "registre/actes";
    }

    @GetMapping("/nouveau")
    String nouveau(@RequestParam(required = false) String type, Model model) {
        SaisieActe s = new SaisieActe();
        s.setTypeActe(type != null && TYPES.containsKey(type) ? type : "NAISSANCE");
        return formulaire(model, s, null);
    }

    @PostMapping
    String creer(@Valid @ModelAttribute("acte") SaisieActe s, BindingResult erreurs, Model model, RedirectAttributes flash) {
        if (erreurs.hasErrors()) return formulaire(model, s, null);
        s.nettoyer();
        Vues.Acte a;
        try {
            a = api.transcrire(s);
        } catch (ApiException e) {
            if (e.indisponible()) throw e;
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, s, null);
        }
        flash.addFlashAttribute("succes", "Acte transcrit sous le numéro " + a.numeroActe()
                + ". Il doit maintenant être validé par un officier de l'état civil.");
        return "redirect:/registre/actes/" + a.id();
    }

    @GetMapping("/{id}")
    String fiche(@PathVariable UUID id, Model model) {
        Vues.Acte a = api.acte(id);
        model.addAttribute("a", a);
        model.addAttribute("journal", api.journalActe(id));
        model.addAttribute("typesMentions", api.typesMentions().stream().filter(t -> t.actes().contains(a.typeActe())).toList());
        return "registre/acte";
    }

    @PostMapping("/{id}/mentions")
    String mention(@PathVariable UUID id, @RequestParam String type,
                   @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
                   @RequestParam(required = false) String reference, @RequestParam(required = false) String details,
                   RedirectAttributes flash) {
        api.ajouterMention(id, type, date, reference, details);
        flash.addFlashAttribute("succes", "Mention portée en marge de l'acte");
        return "redirect:/registre/actes/" + id;
    }

    @GetMapping("/{id}/modifier")
    String modifier(@PathVariable UUID id, Model model) {
        Vues.Acte a = api.acte(id);
        return formulaire(model, SaisieActe.depuis(a), a);
    }

    @PostMapping("/{id}")
    String enregistrer(@PathVariable UUID id, @Valid @ModelAttribute("acte") SaisieActe s, BindingResult erreurs,
                       Model model, RedirectAttributes flash) {
        Vues.Acte existant = api.acte(id);
        if (erreurs.hasErrors()) return formulaire(model, s, existant);
        s.nettoyer();
        try {
            api.corriger(id, s);
        } catch (ApiException e) {
            if (e.indisponible()) throw e;
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, s, existant);
        }
        flash.addFlashAttribute("succes", "Acte mis à jour");
        return "redirect:/registre/actes/" + id;
    }

    private String formulaire(Model model, SaisieActe s, Vues.Acte existant) {
        model.addAttribute("acte", s);
        model.addAttribute("existant", existant);
        model.addAttribute("types", TYPES);
        model.addAttribute("communes", api.communes());
        return "registre/formulaire";
    }
}
