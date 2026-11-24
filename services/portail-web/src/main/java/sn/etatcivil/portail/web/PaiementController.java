package sn.etatcivil.portail.web;

import sn.etatcivil.portail.i18n.Textes;

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
            case "PAYE" -> flash.addFlashAttribute("succes", Textes.t("message.paiement_confirme_la_recherche_de_votre"));
            case "ANNULE" -> flash.addFlashAttribute("erreur", Textes.t("message.le_paiement_a_ete_annule_vous"));
            default -> flash.addFlashAttribute("info", Textes.t("message.paiement_en_cours_de_traitement_par"));
        }
        return "redirect:/demandes/" + r.get("demandeId");
    }

    @GetMapping("/annulation")
    String annulation(@RequestParam("demande") UUID demandeId, RedirectAttributes flash) {
        flash.addFlashAttribute("info", Textes.t("message.paiement_interrompu_votre_demande_reste_en"));
        return "redirect:/demandes/" + demandeId;
    }
}
