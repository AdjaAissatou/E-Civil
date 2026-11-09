package sn.etatcivil.portail.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.paiement.Paiement;
import sn.etatcivil.portail.paiement.PaiementService;
import sn.etatcivil.portail.paiement.StatutPaiement;
import sn.etatcivil.portail.securite.UtilisateurConnecte;

import java.util.Map;

@Controller
@RequestMapping("/paiements")
public class PaiementController {

    private static final Logger log = LoggerFactory.getLogger(PaiementController.class);

    private final PaiementService paiements;

    public PaiementController(PaiementService paiements) {
        this.paiements = paiements;
    }

    /** Crée la facture PayDunya et redirige le citoyen vers la page de paiement sécurisée. */
    @PostMapping("/demandes/{id}")
    String payer(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi) {
        return "redirect:" + paiements.initier(id, moi);
    }

    /** Retour du navigateur après paiement : PayDunya ajoute ?token=… ; on confirme auprès de l'API. */
    @GetMapping("/retour")
    String retour(@RequestParam(required = false) String token, RedirectAttributes flash) {
        if (token == null) return "redirect:/demandes";
        Paiement p = paiements.confirmer(token);
        if (p.getStatut() == StatutPaiement.PAYE) {
            flash.addFlashAttribute("succes", "Paiement confirmé. La recherche de votre acte a commencé.");
        } else if (p.getStatut() == StatutPaiement.ANNULE) {
            flash.addFlashAttribute("erreur", "Le paiement a été annulé. Vous pouvez réessayer quand vous voulez.");
        } else {
            flash.addFlashAttribute("info", "Paiement en cours de traitement par l'opérateur. "
                    + "Cette page se mettra à jour dès sa confirmation.");
        }
        return "redirect:/demandes/" + p.getDemandeId();
    }

    @GetMapping("/annulation")
    String annulation(@RequestParam("demande") Long demandeId, RedirectAttributes flash) {
        flash.addFlashAttribute("info", "Paiement interrompu. Votre demande reste en attente de paiement.");
        return "redirect:/demandes/" + demandeId;
    }

    /** Notification instantanée de paiement envoyée par les serveurs PayDunya (formulaire data[...]). */
    @PostMapping("/paydunya/ipn")
    @ResponseBody
    ResponseEntity<String> notification(@RequestParam Map<String, String> donnees) {
        try {
            paiements.traiterNotification(donnees.get("data[hash]"), donnees.get("data[invoice][token]"));
            return ResponseEntity.ok("OK");
        } catch (ActionRefuseeException e) {
            log.warn("Notification PayDunya refusée : {}", e.getMessage());
            return ResponseEntity.badRequest().body("REFUSE");
        }
    }
}
