package sn.etatcivil.portail.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sn.etatcivil.portail.catalogue.TypeDocumentRepository;
import sn.etatcivil.portail.demande.DemandeService;
import sn.etatcivil.portail.demande.StatutDemande;
import sn.etatcivil.portail.securite.UtilisateurConnecte;

@Controller
public class AccueilController {

    private final TypeDocumentRepository typesDocument;
    private final DemandeService demandes;

    public AccueilController(TypeDocumentRepository typesDocument, DemandeService demandes) {
        this.typesDocument = typesDocument;
        this.demandes = demandes;
    }

    @GetMapping("/")
    String accueil(Model model) {
        model.addAttribute("documents", typesDocument.findByActifTrueOrderByIdAsc());
        // Chiffres réels issus de la base (plus aucune statistique inventée)
        model.addAttribute("delivres", demandes.nombre(StatutDemande.SIGNEE) + demandes.nombre(StatutDemande.LIVREE));
        return "accueil";
    }

    @GetMapping("/documents")
    String documents(Model model) {
        model.addAttribute("documents", typesDocument.findByActifTrueOrderByIdAsc());
        return "documents";
    }

    /** Page d'arrivée après connexion, selon le rôle. */
    @GetMapping("/espace")
    String espace(@AuthenticationPrincipal UtilisateurConnecte moi) {
        return switch (moi.getRole()) {
            case CITOYEN -> "redirect:/demandes";
            case DELEGUE -> "redirect:/delegue";
            case AGENT_REGISTRE -> "redirect:/registre";
            case OFFICIER -> "redirect:/officier";
            case ADMIN -> "redirect:/admin";
        };
    }
}
