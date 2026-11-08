package sn.etatcivil.portail.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import sn.etatcivil.portail.demande.DemandeRepository;
import sn.etatcivil.portail.document.ActePdfService;

import java.util.Locale;

/**
 * Page publique : une administration ou un employeur qui reçoit un document peut vérifier
 * qu'il a bien été délivré par E-Civil, grâce au code imprimé ou au QR code.
 */
@Controller
public class VerificationController {

    private final DemandeRepository demandes;
    private final ActePdfService pdf;

    public VerificationController(DemandeRepository demandes, ActePdfService pdf) {
        this.demandes = demandes;
        this.pdf = pdf;
    }

    @GetMapping("/verification")
    String formulaire(@RequestParam(required = false) String code) {
        if (code != null && !code.isBlank()) {
            return "redirect:/verification/" + code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9-]", "");
        }
        return "verification";
    }

    @GetMapping("/verification/{code}")
    String verifier(@PathVariable String code, Model model) {
        model.addAttribute("code", code);
        demandes.findByCodeVerification(code.toUpperCase(Locale.ROOT)).ifPresent(d -> {
            model.addAttribute("document", pdf.contenu(d));
            model.addAttribute("empreinte", d.getEmpreinte());
        });
        return "verification";
    }
}
