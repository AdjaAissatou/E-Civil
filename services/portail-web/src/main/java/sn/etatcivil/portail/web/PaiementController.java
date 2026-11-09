package sn.etatcivil.portail.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/paiements")
class PaiementController {

    private final Api api;

    PaiementController(Api api) {
        this.api = api;
    }

    /** paiement-service crée la facture PayDunya ; le citoyen est redirigé vers la page de paiement sécurisée. */
    @PostMapping("/demandes/{id}")
    String payer(@PathVariable UUID id) {
        return "redirect:" + api.initierPaiement(id);
    }

    /** Retour de PayDunya (?token=…) : paiement-service interroge PayDunya pour confirmer. */
    @GetMapping("/retour")
    String retour(@RequestParam(required = false) String token, RedirectAttributes flash) {
        if (token == null) return "redirect:/demandes";
        Map<?, ?> r = api.confirmerPaiement(token);
        switch (String.valueOf(r.get("statut"))) {
            case "PAYE" -> flash.addFlashAttribute("succes", "Paiement confirmé. La recherche de votre acte a commencé.");
            case "ANNULE" -> flash.addFlashAttribute("erreur", "Le paiement a été annulé. Vous pouvez réessayer quand vous voulez.");
            default -> flash.addFlashAttribute("info", "Paiement en cours de traitement par l'opérateur. Cette page se mettra à jour dès sa confirmation.");
        }
        return "redirect:/demandes/" + r.get("demandeId");
    }

    @GetMapping("/annulation")
    String annulation(@RequestParam("demande") UUID demandeId, RedirectAttributes flash) {
        flash.addFlashAttribute("info", "Paiement interrompu. Votre demande reste en attente de paiement.");
        return "redirect:/demandes/" + demandeId;
    }
}
