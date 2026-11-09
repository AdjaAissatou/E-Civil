package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.catalogue.TypeDocument;
import sn.etatcivil.portail.catalogue.TypeDocumentRepository;
import sn.etatcivil.portail.demande.DemandeService;
import sn.etatcivil.portail.demande.StatutDemande;
import sn.etatcivil.portail.registre.RegistreClient;
import sn.etatcivil.portail.registre.RegistreIndisponibleException;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.utilisateur.*;

import java.util.EnumMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DemandeService demandes;
    private final CompteService comptes;
    private final UtilisateurRepository utilisateurs;
    private final TypeDocumentRepository typesDocument;
    private final RegistreClient registre;

    public AdminController(DemandeService demandes, CompteService comptes, UtilisateurRepository utilisateurs,
                           TypeDocumentRepository typesDocument, RegistreClient registre) {
        this.demandes = demandes;
        this.comptes = comptes;
        this.utilisateurs = utilisateurs;
        this.typesDocument = typesDocument;
        this.registre = registre;
    }

    @GetMapping
    String tableauDeBord(Model model) {
        Map<StatutDemande, Long> parStatut = new EnumMap<>(StatutDemande.class);
        for (StatutDemande s : StatutDemande.values()) parStatut.put(s, demandes.nombre(s));
        model.addAttribute("parStatut", parStatut);
        model.addAttribute("total", parStatut.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("citoyens", utilisateurs.countByRole(Role.CITOYEN));
        model.addAttribute("agents", utilisateurs.count() - utilisateurs.countByRole(Role.CITOYEN));
        try {
            model.addAttribute("registre", registre.statistiques());
        } catch (RegistreIndisponibleException e) {
            model.addAttribute("registreIndisponible", true);
        }
        return "admin/accueil";
    }

    @GetMapping("/utilisateurs")
    String utilisateurs(Model model) {
        if (!model.containsAttribute("nouveau")) {
            Inscription f = new Inscription();
            f.setRole(Role.DELEGUE);
            model.addAttribute("nouveau", f);
        }
        model.addAttribute("comptes", comptes.tous());
        model.addAttribute("roles", Role.values());
        return "admin/utilisateurs";
    }

    @PostMapping("/utilisateurs")
    String creer(@Valid @ModelAttribute("nouveau") Inscription f, BindingResult erreurs, Model model,
                 RedirectAttributes flash) {
        if (erreurs.hasErrors()) {
            model.addAttribute("erreur", "Vérifiez le formulaire de création de compte");
            model.addAttribute("comptes", comptes.tous());
            model.addAttribute("roles", Role.values());
            return "admin/utilisateurs";
        }
        Utilisateur u = comptes.creerCompte(f);
        flash.addFlashAttribute("succes", "Compte créé pour " + u.getEmail() + " (" + u.getRole().getLibelle() + ")");
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/role")
    String role(@PathVariable Long id, @RequestParam Role role, @AuthenticationPrincipal UtilisateurConnecte moi,
                RedirectAttributes flash) {
        comptes.changerRole(id, role, comptes.trouver(moi.getId()));
        flash.addFlashAttribute("succes", "Rôle modifié");
        return "redirect:/admin/utilisateurs";
    }

    @PostMapping("/utilisateurs/{id}/activation")
    String activation(@PathVariable Long id, @RequestParam boolean actif, @AuthenticationPrincipal UtilisateurConnecte moi,
                      RedirectAttributes flash) {
        comptes.changerActivation(id, actif, comptes.trouver(moi.getId()));
        flash.addFlashAttribute("succes", actif ? "Compte réactivé" : "Compte désactivé");
        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/documents")
    String documents(Model model) {
        model.addAttribute("documents", typesDocument.findAllByOrderByIdAsc());
        return "admin/documents";
    }

    @PostMapping("/documents/{id}")
    @Transactional
    public String modifierDocument(@PathVariable Long id, @RequestParam int tarif, @RequestParam int delaiJours,
                                   @RequestParam(defaultValue = "false") boolean actif, RedirectAttributes flash) {
        if (tarif < 200) throw new ActionRefuseeException("PayDunya exige un montant d'au moins 200 FCFA");
        if (delaiJours < 1 || delaiJours > 60) throw new ActionRefuseeException("Délai invalide");
        TypeDocument t = typesDocument.findById(id).orElseThrow(() -> new ActionRefuseeException("Document inconnu"));
        t.setTarif(tarif);
        t.setDelaiJours(delaiJours);
        t.setActif(actif);
        flash.addFlashAttribute("succes", "« " + t.getLibelle() + " » mis à jour");
        return "redirect:/admin/documents";
    }
}
