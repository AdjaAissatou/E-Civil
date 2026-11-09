package sn.etatcivil.portail.web;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.catalogue.MotifRepository;
import sn.etatcivil.portail.catalogue.TypeDocument;
import sn.etatcivil.portail.catalogue.TypeDocumentRepository;
import sn.etatcivil.portail.demande.*;
import sn.etatcivil.portail.document.ActePdfService;
import sn.etatcivil.portail.paiement.PaiementService;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.utilisateur.CompteService;
import sn.etatcivil.portail.utilisateur.Utilisateur;

/** Espace citoyen : dépôt, suivi, paiement et téléchargement des documents. */
@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandes;
    private final TypeDocumentRepository typesDocument;
    private final MotifRepository motifs;
    private final CompteService comptes;
    private final PaiementService paiements;
    private final ActePdfService pdf;

    public DemandeController(DemandeService demandes, TypeDocumentRepository typesDocument, MotifRepository motifs,
                             CompteService comptes, PaiementService paiements, ActePdfService pdf) {
        this.demandes = demandes;
        this.typesDocument = typesDocument;
        this.motifs = motifs;
        this.comptes = comptes;
        this.paiements = paiements;
        this.pdf = pdf;
    }

    @GetMapping
    String mesDemandes(@AuthenticationPrincipal UtilisateurConnecte moi, Model model) {
        model.addAttribute("demandes", demandes.demandesDe(moi));
        return "citoyen/demandes";
    }

    @GetMapping("/nouvelle")
    String nouvelle(@RequestParam("type") Long typeId, @AuthenticationPrincipal UtilisateurConnecte moi, Model model) {
        TypeDocument type = typeDisponible(typeId);
        FormulaireDemande f = new FormulaireDemande();
        f.setTypeDocumentId(type.getId());
        f.setFormat(FormatDocument.NUMERIQUE);
        f.setLienDemandeur(LienDemandeur.MOI_MEME);
        // Pré-remplissage avec le profil : le citoyen demande le plus souvent son propre acte
        Utilisateur u = comptes.trouver(moi.getId());
        f.setNom(u.getNom());
        f.setPrenom(u.getPrenom());
        f.setSexe(u.getSexe());
        f.setDateNaissance(u.getDateNaissance());
        f.setLieuNaissance(u.getLieuNaissance());
        f.setAdresseLivraison(u.getAdresse());
        return formulaire(model, f, type);
    }

    @PostMapping
    String deposer(@Valid @ModelAttribute("demande") FormulaireDemande f, BindingResult erreurs,
                   @AuthenticationPrincipal UtilisateurConnecte moi, Model model, RedirectAttributes flash) {
        TypeDocument type = typeDisponible(f.getTypeDocumentId());
        if (erreurs.hasErrors()) return formulaire(model, f, type);
        Demande d;
        try {
            d = demandes.creer(f, moi);
        } catch (ActionRefuseeException e) {
            model.addAttribute("erreur", e.getMessage());
            return formulaire(model, f, type);
        }
        flash.addFlashAttribute("succes", "Demande " + d.getReference() + " enregistrée. Vous serez prévenu(e) par email "
                + "dès qu'elle aura été examinée.");
        return "redirect:/demandes/" + d.getId();
    }

    @GetMapping("/{id}")
    String detail(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, Model model) {
        Demande d = demandes.demandeDe(id, moi);
        if (moi.getRole().estAgent() && !d.appartientA(moi.getId())) return "redirect:/back-office/demandes/" + id;
        model.addAttribute("d", d);
        model.addAttribute("historique", demandes.historique(id));
        model.addAttribute("paiements", paiements.deLaDemande(id));
        model.addAttribute("paiementDisponible", paiements.estDisponible());
        return "citoyen/demande";
    }

    @PostMapping("/{id}/annulation")
    String annuler(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi, RedirectAttributes flash) {
        demandes.annuler(id, moi);
        flash.addFlashAttribute("succes", "Demande annulée");
        return "redirect:/demandes/" + id;
    }

    @GetMapping("/{id}/document.pdf")
    ResponseEntity<byte[]> document(@PathVariable Long id, @AuthenticationPrincipal UtilisateurConnecte moi) {
        Demande d = demandes.demandeDe(id, moi);
        if (d.getStatut() != StatutDemande.SIGNEE && d.getStatut() != StatutDemande.LIVREE) {
            throw new ActionRefuseeException("Le document n'est pas encore disponible");
        }
        if (!moi.getRole().estAgent() && d.getFormat() != FormatDocument.NUMERIQUE) {
            throw new ActionRefuseeException("Ce document a été demandé en version papier");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(d.getReference() + ".pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(pdf.generer(d));
    }

    private String formulaire(Model model, FormulaireDemande f, TypeDocument type) {
        model.addAttribute("demande", f);
        model.addAttribute("type", type);
        model.addAttribute("motifs", motifs.findAllByOrderByOrdreAsc());
        model.addAttribute("liens", LienDemandeur.values());
        model.addAttribute("formats", FormatDocument.values());
        return "citoyen/nouvelle";
    }

    private TypeDocument typeDisponible(Long id) {
        return typesDocument.findById(id).filter(TypeDocument::isActif)
                .orElseThrow(() -> new ActionRefuseeException("Ce document n'est pas disponible"));
    }
}
