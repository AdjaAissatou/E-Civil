package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.catalogue.TypeActe;
import sn.etatcivil.portail.registre.*;
import sn.etatcivil.portail.securite.UtilisateurConnecte;

/** Transcription des registres papier dans le registre central. */
@Controller
@RequestMapping("/registre/actes")
public class RegistreController {

    private final RegistreClient registre;

    public RegistreController(RegistreClient registre) {
        this.registre = registre;
    }

    @GetMapping
    String liste(@RequestParam(required = false) TypeActe type, @RequestParam(required = false) String statut,
                 @RequestParam(required = false) String nom, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("actes", registre.lister(type, statut, nom, page));
        model.addAttribute("types", TypeActe.values());
        model.addAttribute("type", type);
        model.addAttribute("statut", statut);
        model.addAttribute("nom", nom);
        return "registre/actes";
    }

    @GetMapping("/nouveau")
    String nouveau(@RequestParam(required = false) TypeActe type, Model model) {
        SaisieActe s = new SaisieActe();
        s.setTypeActe(type == null ? TypeActe.NAISSANCE : type);
        return formulaire(model, s, null);
    }

    @PostMapping
    String creer(@Valid @ModelAttribute("acte") SaisieActe s, BindingResult erreurs,
                 @AuthenticationPrincipal UtilisateurConnecte moi, Model model, RedirectAttributes flash) {
        if (erreurs.hasErrors()) return formulaire(model, s, null);
        s.nettoyer();
        s.setAgent(moi.getUsername());
        ActeRegistre a;
        try {
            a = registre.creer(s);
        } catch (RefusRegistreException e) {
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, s, null);
        }
        flash.addFlashAttribute("succes", "Acte transcrit sous le numéro " + a.numeroActe()
                + ". Il doit maintenant être validé par un officier de l'état civil.");
        return "redirect:/registre/actes/" + a.id();
    }

    @GetMapping("/{id}")
    String fiche(@PathVariable Long id, Model model) {
        model.addAttribute("a", registre.obtenir(id));
        return "registre/acte";
    }

    @GetMapping("/{id}/modifier")
    String modifier(@PathVariable Long id, Model model) {
        ActeRegistre a = registre.obtenir(id);
        return formulaire(model, SaisieActe.depuis(a), a);
    }

    @PostMapping("/{id}")
    String enregistrer(@PathVariable Long id, @Valid @ModelAttribute("acte") SaisieActe s, BindingResult erreurs,
                       @AuthenticationPrincipal UtilisateurConnecte moi, Model model, RedirectAttributes flash) {
        ActeRegistre existant = registre.obtenir(id);
        if (erreurs.hasErrors()) return formulaire(model, s, existant);
        s.nettoyer();
        s.setAgent(moi.getUsername());
        try {
            registre.modifier(id, s);
        } catch (RefusRegistreException e) {
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, s, existant);
        }
        flash.addFlashAttribute("succes", "Acte mis à jour");
        return "redirect:/registre/actes/" + id;
    }

    private String formulaire(Model model, SaisieActe s, ActeRegistre existant) {
        model.addAttribute("acte", s);
        model.addAttribute("existant", existant);
        model.addAttribute("types", TypeActe.values());
        return "registre/formulaire";
    }
}
