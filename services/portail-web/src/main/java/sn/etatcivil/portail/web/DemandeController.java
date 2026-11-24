package sn.etatcivil.portail.web;

import java.util.ArrayList;
import java.util.List;

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
        List<Vues.Demande> demandes = api.mesDemandes();
        model.addAttribute("demandes", demandes);
        // compteurs par étape, comme le tableau de bord du citoyen de la version 1
        java.util.Map<String, Long> etapes = new java.util.LinkedHashMap<>();
        etapes.put("En examen", compter(demandes, "SOUMISE"));
        etapes.put("À payer", compter(demandes, "A_PAYER"));
        etapes.put("En traitement", compter(demandes, "PAYEE", "ACTE_TROUVE", "ACTE_INTROUVABLE"));
        etapes.put("Délivrées", compter(demandes, "SIGNEE", "LIVREE"));
        etapes.put("Rejetées ou annulées", compter(demandes, "REJETEE", "ANNULEE"));
        model.addAttribute("etapes", etapes);
        return "citoyen/demandes";
    }

    private static long compter(List<Vues.Demande> demandes, String... statuts) {
        List<String> s = List.of(statuts);
        return demandes.stream().filter(d -> s.contains(d.statut())).count();
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
        Vues.ProfilPersonnel profil = profil();
        if (profil != null) {
            // informations de l'inscription : identité, profession et domicile de la personne
            f.setSexe(profil.sexe());
            f.setDateNaissance(profil.dateNaissance());
            f.setLieuNaissance(profil.lieuNaissance());
            f.setProfession(profil.profession());
            f.setResidence(profil.residence());
        }
        return formulaire(model, f, type);
    }

    @PostMapping
    String deposer(@Valid @ModelAttribute("demande") Formulaires.Demande f, BindingResult erreurs, Model model,
                   RedirectAttributes flash) {
        Vues.TypeDocument type = api.document(f.getTypeDocumentId());
        if (erreurs.hasErrors()) return formulaire(model, f, type);
        Vues.Demande d;
        try {
            d = type.verificationIdentite() ? api.deposer(f, f.getTypePiece(), photos(f)) : api.deposer(f);
        } catch (java.io.IOException e) {
            model.addAttribute("erreur", "Les photos n'ont pas pu être lues : envoyez-les de nouveau.");
            return formulaire(model, f, type);
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
        model.addAttribute("motifs", api.motifs(type.id()));
        model.addAttribute("typesMentions", typesMentions(type));
        // retrait à une mission diplomatique : celles qui couvrent le pays de résidence d'abord
        Vues.ProfilPersonnel profil = profil();
        List<Vues.Mission> missions = new ArrayList<>(api.missions(null));
        if (profil != null && profil.diaspora() && profil.paysResidence() != null) {
            String pays = profil.paysResidence();
            missions.sort(java.util.Comparator.comparing((Vues.Mission m) -> !pays.equalsIgnoreCase(m.pays())));
            model.addAttribute("paysResidence", pays);
        }
        model.addAttribute("missions", missions);
        model.addAttribute("diaspora", profil != null && profil.diaspora());
        return "citoyen/nouvelle";
    }

    private static java.util.Map<String, Api.Photo> photos(Formulaires.Demande f) throws java.io.IOException {
        java.util.Map<String, Api.Photo> m = new java.util.LinkedHashMap<>();
        m.put("recto", photo(f.getRecto()));
        m.put("verso", "CNI".equals(f.getTypePiece()) ? photo(f.getVerso()) : null);
        m.put("selfie", photo(f.getSelfie()));
        return m;
    }

    private static Api.Photo photo(org.springframework.web.multipart.MultipartFile f) throws java.io.IOException {
        return f == null || f.isEmpty() ? null : new Api.Photo(f.getOriginalFilename(), f.getBytes(), f.getContentType());
    }

    /** Profil de l'inscription, s'il est disponible (le formulaire fonctionne sans). */
    private Vues.ProfilPersonnel profil() {
        try {
            return api.monProfil();
        } catch (ApiException e) {
            return null;
        }
    }

    private List<Vues.TypeMention> typesMentions(Vues.TypeDocument type) {
        if (!type.demande("MENTION")) return List.of();
        try {
            return api.typesMentions().stream().filter(t -> t.actes().contains(type.typeActe())).toList();
        } catch (ApiException e) {
            return List.of();
        }
    }
}
