package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.utilisateur.CompteService;
import sn.etatcivil.portail.utilisateur.Inscription;

@Controller
public class CompteController {

    private final CompteService comptes;

    public CompteController(CompteService comptes) {
        this.comptes = comptes;
    }

    @GetMapping("/connexion")
    String connexion(@AuthenticationPrincipal UtilisateurConnecte moi) {
        return moi != null ? "redirect:/espace" : "connexion";
    }

    @GetMapping("/inscription")
    String formulaire(Model model) {
        model.addAttribute("inscription", new Inscription());
        return "inscription";
    }

    @PostMapping("/inscription")
    String inscrire(@Valid @ModelAttribute("inscription") Inscription f, BindingResult erreurs,
                    Model model, RedirectAttributes flash) {
        if (erreurs.hasErrors()) return "inscription";
        try {
            comptes.inscrireCitoyen(f);
        } catch (ActionRefuseeException e) {
            model.addAttribute("erreur", e.getMessage());
            return "inscription";
        }
        flash.addFlashAttribute("succes", "Votre compte est créé. Connectez-vous pour faire votre première demande.");
        return "redirect:/connexion";
    }

    @GetMapping("/profil")
    String profil(@AuthenticationPrincipal UtilisateurConnecte moi, Model model) {
        model.addAttribute("compte", comptes.trouver(moi.getId()));
        return "profil";
    }

    @PostMapping("/profil")
    String enregistrerProfil(@AuthenticationPrincipal UtilisateurConnecte moi,
                             @RequestParam(required = false) String telephone,
                             @RequestParam(required = false) String adresse, RedirectAttributes flash) {
        if (telephone != null && !telephone.isBlank() && !telephone.matches("\\+?[0-9 ]{7,20}")) {
            throw new ActionRefuseeException("Numéro de téléphone invalide");
        }
        comptes.mettreAJourProfil(moi.getId(), vide(telephone), vide(adresse));
        flash.addFlashAttribute("succes", "Profil mis à jour");
        return "redirect:/profil";
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
