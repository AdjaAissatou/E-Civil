package sn.etatcivil.portail.web;

import sn.etatcivil.portail.i18n.Textes;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Espaces des agents : files d'attente par rôle et fiche commune d'une demande. */
@Controller
class BackOfficeController {

    static final Map<String, String> STATUTS = new LinkedHashMap<>();
    static {
        STATUTS.put("SOUMISE", "Soumise");
        STATUTS.put("A_PAYER", "À payer");
        STATUTS.put("PAYEE", "Payée");
        STATUTS.put("ACTE_TROUVE", "Acte retrouvé");
        STATUTS.put("ACTE_INTROUVABLE", "Acte non retrouvé");
        STATUTS.put("SIGNEE", "Signée");
        STATUTS.put("LIVREE", "Livrée");
        STATUTS.put("REJETEE", "Rejetée");
        STATUTS.put("ANNULEE", "Annulée");
    }

    private final Api api;
    private final RenseignementController renseignement;

    BackOfficeController(Api api, RenseignementController renseignement) {
        this.api = api;
        this.renseignement = renseignement;
    }

    // ---------------------------------------------------------------- files d'attente

    /** Filtres communs des files (comme les écrans du délégué et de l'officier de la version 1). */
    @ModelAttribute("filtre")
    Api.FiltreDemandes filtre(@RequestParam(required = false) String reference,
                              @RequestParam(required = false) Long typeDocument,
                              @RequestParam(required = false) String format,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au) {
        return new Api.FiltreDemandes(reference, typeDocument, format == null || format.isBlank() ? null : format, du, au);
    }

    @GetMapping("/delegue")
    String delegue(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "file.delegue.titre", "file.delegue.aide",
                List.of("SOUMISE"), f, page, List.of("SOUMISE", "A_PAYER", "SIGNEE", "LIVREE", "REJETEE"));
    }

    @GetMapping("/delegue/livraisons")
    String livraisons(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        return file(model, "file.livraisons.titre", "file.livraisons.aide", List.of("SIGNEE"), f, page, List.of("SIGNEE", "LIVREE"));
    }

    @GetMapping("/registre")
    String registre(@ModelAttribute("filtre") Api.FiltreDemandes f, @RequestParam(defaultValue = "0") int page, Model model) {
        try {
            model.addAttribute("statsRenseignement", api.statistiquesRenseignement());
            Map<UUID, Vues.ResumeRenseignement> dossiers = new java.util.HashMap<>();
            api.renseignementsEnCours().forEach(r -> dossiers.put(r.demandeId(), r));
            model.addAttribute("dossiers", dossiers);
        } catch (ApiException e) {
            // résumés secondaires : la file reste affichée
        }
        return file(model, "file.registre.titre", "file.registre.aide",
                List.of("ACTE_INTROUVABLE", "PAYEE"), f, page, List.of("PAYEE", "ACTE_INTROUVABLE", "ACTE_TROUVE"));
    }

    @GetMapping("/officier")
    String officier(@ModelAttribute("filtre") Api.FiltreDemandes f, Model model) {
        model.addAttribute("aSigner", api.demandes(List.of("ACTE_TROUVE"), f, 0).contenu());
        compteurs(model, f, List.of("ACTE_TROUVE", "SIGNEE", "REJETEE"));
        model.addAttribute("documents", documentsDuCatalogue());
        try {
            model.addAttribute("aValider", api.actes(null, "SAISI", null, 0).contenu());
        } catch (ApiException e) {
            model.addAttribute("registreIndisponible", true);
            model.addAttribute("aValider", List.of());
        }
        return "officier/accueil";
    }

    @GetMapping("/back-office/demandes")
    String toutes(@RequestParam(required = false) String statut, @ModelAttribute("filtre") Api.FiltreDemandes f,
                  @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("titre", Textes.t("message.toutes_les_demandes"));
        model.addAttribute("page", api.demandes(statut == null || statut.isBlank() ? null : List.of(statut), f, page));
        model.addAttribute("tousStatuts", true);
        model.addAttribute("statuts", STATUTS);
        model.addAttribute("statut", statut);
        compteurs(model, f, List.copyOf(STATUTS.keySet()));
        model.addAttribute("documents", documentsDuCatalogue());
        return "agent/demandes";
    }

    @GetMapping("/back-office/demandes/{id}")
    String fiche(@PathVariable UUID id, @RequestParam(required = false) Integer annee,
                 org.springframework.security.core.Authentication auth, Model model) {
        Vues.Demande d = api.demande(id);
        renseignement.completerFiche(d, annee, auth, model);
        api.demandeur(d.demandeurId()).ifPresent(p -> model.addAttribute("demandeur", p));
        model.addAttribute("d", d);
        model.addAttribute("paiements", api.paiements(id));
        model.addAttribute("pieces", d.verificationIdentite() ? api.piecesIdentite(id) : List.of());
        if (d.acteId() != null && "ACTE_TROUVE".equals(d.statut())) {
            try {
                model.addAttribute("acte", api.acte(d.acteId()));
            } catch (ApiException e) {
                model.addAttribute("registreIndisponible", true);
            }
        }
        return "agent/demande";
    }

    // ---------------------------------------------------------------- actions

    /** Photo d'identité, servie sans cache ; demande-service vérifie que l'agent a le droit de la voir. */
    @GetMapping("/back-office/demandes/{id}/pieces/{pieceId}")
    org.springframework.http.ResponseEntity<byte[]> photo(@PathVariable UUID id, @PathVariable UUID pieceId) {
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(api.photoIdentite(id, pieceId));
    }

    @PostMapping("/delegue/demandes/{id}/validation")
    String valider(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean identiteVerifiee, RedirectAttributes flash) {
        api.action(id, "validation", Map.of("identiteVerifiee", String.valueOf(identiteVerifiee)));
        flash.addFlashAttribute("succes", Textes.t("message.demande_validee_le_demandeur_est_invite"));
        return "redirect:/delegue";
    }

    @PostMapping("/delegue/demandes/{id}/livraison")
    String livrer(@PathVariable UUID id, RedirectAttributes flash) {
        api.action(id, "livraison", null);
        flash.addFlashAttribute("succes", Textes.t("message.document_marque_comme_expedie"));
        return "redirect:/delegue/livraisons";
    }

    @PostMapping("/back-office/demandes/{id}/rejet")
    String rejeter(@PathVariable UUID id, @RequestParam String motif, RedirectAttributes flash) {
        api.action(id, "rejet", Map.of("motif", motif));
        flash.addFlashAttribute("succes", Textes.t("message.demande_rejetee_le_demandeur_en_est"));
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/registre/demandes/{id}/recherche")
    String relancer(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Demande d = api.actionAvecResultat(id, "recherche", null);
        if ("ACTE_TROUVE".equals(d.statut())) {
            flash.addFlashAttribute("succes", Textes.t("message.acte_retrouve_la_demande_part_en", d.numeroActe()));
        } else {
            flash.addFlashAttribute("erreur", Textes.t("message.texte", d.noteRecherche()));
        }
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/registre/demandes/{id}/rattachement")
    String rattacher(@PathVariable UUID id, @RequestParam String numeroActe, RedirectAttributes flash) {
        api.actionAvecResultat(id, "rattachement", Map.of("numeroActe", numeroActe));
        flash.addFlashAttribute("succes", Textes.t("message.acte_rattache_la_demande_part_en"));
        return "redirect:/back-office/demandes/" + id;
    }

    @PostMapping("/officier/demandes/{id}/signature")
    String signer(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Demande d = api.actionAvecResultat(id, "signature", null);
        flash.addFlashAttribute("succes", Textes.t("message.document_signe_code_de_verification", d.codeVerification()));
        return "redirect:/officier";
    }

    @PostMapping("/officier/demandes/{id}/contestation")
    String contester(@PathVariable UUID id, @RequestParam String raison, RedirectAttributes flash) {
        api.action(id, "contestation", Map.of("raison", raison));
        flash.addFlashAttribute("succes", Textes.t("message.la_demande_est_renvoyee_a_l"));
        return "redirect:/officier";
    }

    @PostMapping("/officier/actes/{id}/validation")
    String validerActe(@PathVariable UUID id, RedirectAttributes flash) {
        Vues.Acte a = api.validerActe(id);
        flash.addFlashAttribute("succes", Textes.t("message.acte_valide_dans_le_registre_national", a.numeroActe()));
        return "redirect:/officier";
    }

    private String file(Model model, String titre, String aide, List<String> statuts, Api.FiltreDemandes f, int page,
                        List<String> compteurs) {
        model.addAttribute("titre", Textes.t("message.texte_2", titre));
        model.addAttribute("aide", aide);
        model.addAttribute("page", api.demandes(statuts, f, page));
        compteurs(model, f, compteurs);
        model.addAttribute("documents", documentsDuCatalogue());
        return "agent/demandes";
    }

    /** Nombre de demandes par statut pour les filtres en cours (cartes en tête de file). */
    private void compteurs(Model model, Api.FiltreDemandes f, List<String> statuts) {
        Map<String, Map.Entry<String, Long>> c = new LinkedHashMap<>();
        try {
            Map<String, Long> n = api.statistiquesDemandes(f);
            statuts.forEach(s -> c.put(s, Map.entry(STATUTS.get(s), n.getOrDefault(s, 0L))));
        } catch (ApiException e) {
            // compteurs secondaires : la file reste affichée
        }
        model.addAttribute("compteurs", c);
    }

    private List<Vues.TypeDocument> documentsDuCatalogue() {
        try {
            return api.documents().stream().sorted(java.util.Comparator.comparing(Vues.TypeDocument::libelle)).toList();
        } catch (ApiException e) {
            return List.of();
        }
    }
}
