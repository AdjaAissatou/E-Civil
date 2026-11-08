package sn.etatcivil.portail.web;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.demande.Demande;
import sn.etatcivil.portail.demande.DemandeService;
import sn.etatcivil.portail.demande.StatutDemande;
import sn.etatcivil.portail.paiement.PaiementService;
import sn.etatcivil.portail.registre.ActeRegistre;
import sn.etatcivil.portail.registre.RegistreClient;
import sn.etatcivil.portail.registre.RegistreIndisponibleException;
import sn.etatcivil.portail.securite.UtilisateurConnecte;

import java.util.List;

import static sn.etatcivil.portail.demande.StatutDemande.*;

/**
 * Espaces des agents. Les files d'attente sont propres à chaque rôle ; la fiche d'une demande
 * est commune et n'affiche que les actions autorisées au rôle connecté.
 */
@Controller
public class BackOfficeController {

    private final DemandeService demandes;
    private final PaiementService paiements;
    private final RegistreClient registre;

    public BackOfficeController(DemandeService demandes, PaiementService paiements, RegistreClient registre) {
        this.demandes = demandes;
        this.paiements = paiements;
        this.registre = registre;
    }

    // ------------------------------------------------------------------ files d'attente

    @GetMapping("/delegue")
    String delegue(@RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Demandes à examiner",
                "Vérifiez la cohérence de chaque demande avant de l'ouvrir au paiement.",
                List.of(SOUMISE), page);
    }

    @GetMapping("/delegue/livraisons")
    String livraisons(@RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Documents papier à expédier",
                "Documents signés demandés en version papier.", List.of(SIGNEE), page);
    }

    @GetMapping("/registre")
    String registreFile(@RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "Actes non retrouvés",
                "Retrouvez l'acte dans les registres papier, transcrivez-le puis relancez la recherche "
                        + "ou rattachez-le par son numéro.", List.of(ACTE_INTROUVABLE, PAYEE), page);
    }

    @GetMapping("/officier")
    String officier(Model model) {
        model.addAttribute("aSigner", demandes.parStatuts(List.of(ACTE_TROUVE), PageRequest.of(0, 50)).getContent());
        try {
            model.addAttribute("aValider", registre.lister(null, "SAISI", null, 0).content());
        } catch (RegistreIndisponibleException e) {
            model.addAttribute("registreIndisponible", true);
            model.addAttribute("aValider", List.of());
        }
        return "officier/accueil";
    }

    @GetMapping("/back-office/demandes")
    String toutes(@RequestParam(required = false) StatutDemande statut, @RequestParam(required = false) String reference,
                  @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("titre", "Toutes les demandes");
        model.addAttribute("page", demandes.rechercher(statut, reference, PageRequest.of(page, 25)));
        model.addAttribute("filtres", true);
        model.addAttribute("statuts", StatutDemande.values());
        model.addAttribute("statut", statut);
        model.addAttribute("reference", reference);
        return "agent/demandes";
    }

    @GetMapping("/back-office/demandes/{id}")
    String fiche(@PathVariable Long id, Model model) {
        Demande d = demandes.trouver(id);
        model.addAttribute("d", d);
        model.addAttribute("historique", demandes.historique(id));
        model.addAttribute("paiements", paiements.deLaDemande(id));
        if (d.getActeCentralId() != null && d.getStatut() == ACTE_TROUVE) {
            try {
                ActeRegistre acte = registre.obtenir(d.getActeCentralId());
                model.addAttribute("acte", acte);
            } catch (RegistreIndisponibleException e) {
                model.addAttribute("registreIndisponible", true);
            }
        }
        return "agent/demande";
    }

    // ------------------------------------------------------------------ délégué

    @PostMapping("/delegue/demandes/{id}/validation")
    String valider(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.validerPourPaiement(id, moi);
        flash.addFlashAttribute("succes", "Demande validée : le demandeur est invité à payer en ligne.");
        return "redirect:/delegue";
    }

    @PostMapping("/delegue/demandes/{id}/livraison")
    String livrer(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.marquerLivree(id, moi);
        flash.addFlashAttribute("succes", "Document marqué comme expédié.");
        return "redirect:/delegue/livraisons";
    }

    /** Rejet possible par le délégué (avant paiement) ou l'agent du registre (acte inexistant). */
    @PostMapping("/back-office/demandes/{id}/rejet")
    String rejeter(@PathVariable Long id, @RequestParam String motif,
                   @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        Demande d = demandes.trouver(id);
        boolean autorise = switch (moi.getRole()) {
            case DELEGUE -> d.getStatut() == SOUMISE || d.getStatut() == A_PAYER;
            case AGENT_REGISTRE, OFFICIER -> d.getStatut() == ACTE_INTROUVABLE;
            case ADMIN -> true;
            case CITOYEN -> false;
        };
        if (!autorise) throw new ActionRefuseeException("Votre rôle ne permet pas de rejeter cette demande à ce stade");
        demandes.rejeter(id, motif, moi);
        flash.addFlashAttribute("succes", "Demande rejetée, le demandeur en est informé.");
        return "redirect:/back-office/demandes/" + id;
    }

    // ------------------------------------------------------------------ registre

    @PostMapping("/registre/demandes/{id}/recherche")
    String relancer(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.rechercherDansRegistre(id, moi.signature());
        Demande d = demandes.trouver(id);
        if (d.getStatut() == ACTE_TROUVE) {
            flash.addFlashAttribute("succes", "Acte " + d.getNumeroActe() + " retrouvé : la demande part en signature.");
        } else {
            flash.addFlashAttribute("erreur", d.getNoteRecherche());
        }
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/registre/demandes/{id}/rattachement")
    String rattacher(@PathVariable Long id, @RequestParam String numeroActe,
                     @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.rattacherParNumero(id, numeroActe, moi);
        flash.addFlashAttribute("succes", "Acte rattaché : la demande part en signature.");
        return "redirect:/back-office/demandes/" + id;
    }

    // ------------------------------------------------------------------ officier

    @PostMapping("/officier/demandes/{id}/signature")
    String signer(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.signer(id, moi);
        flash.addFlashAttribute("succes", "Document signé. Code de vérification : "
                + demandes.trouver(id).getCodeVerification());
        return "redirect:/officier";
    }

    @PostMapping("/officier/demandes/{id}/contestation")
    String contester(@PathVariable Long id, @RequestParam String raison,
                     @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.contesterRattachement(id, raison, moi);
        flash.addFlashAttribute("succes", "La demande est renvoyée à l'agent du registre.");
        return "redirect:/officier";
    }

    @PostMapping("/officier/actes/{id}/validation")
    String validerActe(@PathVariable Long id, RedirectAttributes flash) {
        ActeRegistre a = registre.valider(id);
        flash.addFlashAttribute("succes", "Acte " + a.numeroActe() + " validé dans le registre central.");
        return "redirect:/officier";
    }

    private String file(Model model, String titre, String aide, List<StatutDemande> statuts, int page) {
        model.addAttribute("titre", titre);
        model.addAttribute("aide", aide);
        model.addAttribute("page", demandes.parStatuts(statuts, PageRequest.of(page, 25)));
        return "agent/demandes";
    }
}
