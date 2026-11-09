package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;
import sn.etatcivil.portail.securite.Utilisateur;

import java.util.UUID;

/** Espace citoyen : dépôt, suivi, paiement et téléchargement. */
@Controller
@RequestMapping("/demandes")
class DemandeController {

    private final Api api;

    DemandeController(Api api) {
        this.api = api;
    }

    @GetMapping
    String mesDemandes(Model model) {
        model.addAttribute("demandes", api.mesDemandes());
        return "citoyen/demandes";
    }

    @GetMapping("/nouvelle")
    String nouvelle(@RequestParam("type") Long typeId, @AuthenticationPrincipal OidcUser u, Model model) {
        Vues.TypeDocument type = api.document(typeId);
        Formulaires.Demande f = new Formulaires.Demande();
        f.setTypeDocumentId(type.id());
        // Le citoyen demande le plus souvent son propre acte : on pré-remplit avec son identité
        Utilisateur moi = Utilisateur.depuis(u);
        f.setNom(moi.nom());
        f.setPrenom(moi.prenom());
        return formulaire(model, f, type);
    }

    @PostMapping
    String deposer(@Valid @ModelAttribute("demande") Formulaires.Demande f, BindingResult erreurs, Model model,
                   RedirectAttributes flash) {
        Vues.TypeDocument type = api.document(f.getTypeDocumentId());
        if (erreurs.hasErrors()) return formulaire(model, f, type);
        Vues.Demande d;
        try {
            d = api.deposer(f);
        } catch (ApiException e) {
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, f, type);
        }
        flash.addFlashAttribute("succes", "Demande " + d.reference() + " enregistrée. Vous serez prévenu(e) par email "
                + "dès qu'elle aura été examinée.");
        return "redirect:/demandes/" + d.id();
    }

    @GetMapping("/{id}")
    String detail(@PathVariable UUID id, @AuthenticationPrincipal OidcUser u, Model model) {
        Vues.Demande d = api.demande(id);
        if (Utilisateur.depuis(u).estAgent() && !d.demandeurId().equals(u.getSubject())) {
            return "redirect:/back-office/demandes/" + id;
        }
        model.addAttribute("d", d);
        model.addAttribute("paiements", api.paiements(id));
        model.addAttribute("paiementDisponible", "A_PAYER".equals(d.statut()) && api.paiementDisponible());
        return "citoyen/demande";
    }

    @PostMapping("/{id}/annulation")
    String annuler(@PathVariable UUID id, RedirectAttributes flash) {
        api.action(id, "annulation", null);
        flash.addFlashAttribute("succes", "Demande annulée");
        return "redirect:/demandes/" + id;
    }

    @GetMapping("/{id}/document.pdf")
    ResponseEntity<byte[]> document(@PathVariable UUID id) {
        Vues.Demande d = api.demande(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(d.reference() + ".pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(api.pdf(id));
    }

    private String formulaire(Model model, Formulaires.Demande f, Vues.TypeDocument type) {
        model.addAttribute("demande", f);
        model.addAttribute("type", type);
        model.addAttribute("motifs", api.motifs());
        return "citoyen/nouvelle";
    }
}
