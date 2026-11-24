package sn.etatcivil.portail.web;

import sn.etatcivil.portail.i18n.Textes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.Vues;

import java.util.*;

/**
 * Administration du catalogue : natures (familles), variétés de documents (toutes leurs caractéristiques),
 * motifs avec leurs pièces à fournir, missions diplomatiques et pays de la diaspora.
 */
@Controller
@RequestMapping("/admin")
class CatalogueAdminController {

    private final Api api;

    CatalogueAdminController(Api api) {
        this.api = api;
    }

    // ------------------------------------------------------------------ variétés

    @GetMapping("/documents/nouveau")
    String nouveau(Model model) {
        return formulaire(model, null);
    }

    @GetMapping("/documents/{id}/modifier")
    String modifier(@PathVariable Long id, Model model) {
        Vues.TypeDocument t = api.tousLesDocuments().stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        return formulaire(model, t);
    }

    @PostMapping("/documents/enregistrement")
    String enregistrer(@RequestParam(required = false) Long id, @RequestParam(required = false) String code,
                       @RequestParam String libelle, @RequestParam(required = false) String description,
                       @RequestParam Long natureId, @RequestParam String typeActe, @RequestParam String modele,
                       @RequestParam(required = false) String origine, @RequestParam(required = false) String typeMention,
                       @RequestParam(name = "champ", required = false) List<String> champs,
                       @RequestParam int tarif, @RequestParam int delaiJours,
                       @RequestParam(required = false) Integer dureeValiditeJours, @RequestParam(required = false) String motsCles,
                       @RequestParam(required = false) String piecesAFournir,
                       @RequestParam(defaultValue = "false") boolean estOriginal, @RequestParam(required = false) Long copieDeId,
                       @RequestParam(defaultValue = "false") boolean actif,
                       @RequestParam(defaultValue = "false") boolean verificationIdentite, RedirectAttributes flash) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("verificationIdentite", verificationIdentite);
        corps.put("code", code);
        corps.put("libelle", libelle);
        corps.put("description", description);
        corps.put("natureId", natureId);
        corps.put("typeActe", typeActe);
        corps.put("modele", modele);
        corps.put("origine", vide(origine));
        corps.put("typeMention", vide(typeMention));
        corps.put("champs", champs == null ? List.of() : champs);
        corps.put("tarif", tarif);
        corps.put("delaiJours", delaiJours);
        corps.put("dureeValiditeJours", dureeValiditeJours);
        corps.put("motsCles", motsCles);
        corps.put("piecesAFournir", piecesAFournir);
        corps.put("estOriginal", estOriginal);
        corps.put("copieDeId", copieDeId);
        corps.put("actif", actif);
        Vues.TypeDocument t = id == null ? api.creerDocument(corps) : api.modifierDocumentComplet(id, corps);
        flash.addFlashAttribute("succes", Textes.t(id == null ? "message.document_cree" : "message.document_enregistre", t.libelle()));
        return "redirect:/admin/documents/" + t.id() + "/modifier";
    }

    private String formulaire(Model model, Vues.TypeDocument t) {
        model.addAttribute("document", t);
        model.addAttribute("natures", api.natures(false));
        model.addAttribute("referentiels", api.referentielsCatalogue());
        model.addAttribute("originaux", api.tousLesDocuments().stream().filter(Vues.TypeDocument::estOriginal).toList());
        try {
            model.addAttribute("typesMentions", api.typesMentions());
        } catch (sn.etatcivil.portail.clients.ApiException e) {
            model.addAttribute("typesMentions", List.of());
        }
        if (t != null) {
            model.addAttribute("motifsPropres", api.tousLesMotifs().stream().filter(m -> t.id().equals(m.typeDocumentId())).toList());
        }
        return "admin/document";
    }

    // ------------------------------------------------------------------ natures

    @GetMapping("/natures")
    String natures(Model model) {
        List<Vues.Nature> natures = api.natures(false);
        Map<Long, Long> nombre = new HashMap<>();
        api.tousLesDocuments().forEach(t -> { if (t.natureId() != null) nombre.merge(t.natureId(), 1L, Long::sum); });
        model.addAttribute("natures", natures);
        model.addAttribute("nombre", nombre);
        return "admin/natures";
    }

    @PostMapping("/natures")
    String enregistrerNature(@RequestParam(required = false) Long id, @RequestParam(required = false) String code,
                             @RequestParam String libelle, @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "0") int ordre, @RequestParam(defaultValue = "false") boolean active,
                             RedirectAttributes flash) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("code", code);
        corps.put("libelle", libelle);
        corps.put("description", description);
        corps.put("ordre", ordre);
        corps.put("active", id == null || active);
        api.enregistrerNature(id, corps);
        flash.addFlashAttribute("succes", Textes.t("message.nature_enregistree", libelle.trim()));
        return "redirect:/admin/natures";
    }

    // ------------------------------------------------------------------ motifs

    @GetMapping("/motifs")
    String motifs(Model model) {
        model.addAttribute("motifs", api.tousLesMotifs());
        Map<Long, String> libelles = new LinkedHashMap<>();
        api.tousLesDocuments().stream().sorted(Comparator.comparing(Vues.TypeDocument::libelle))
                .forEach(t -> libelles.put(t.id(), t.libelle()));
        model.addAttribute("documents", libelles);
        return "admin/motifs";
    }

    @PostMapping("/motifs")
    String enregistrerMotif(@RequestParam(required = false) Long id, @RequestParam String libelle,
                            @RequestParam(defaultValue = "50") int ordre, @RequestParam(required = false) String piecesRequises,
                            @RequestParam(required = false) Long typeDocumentId, RedirectAttributes flash) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("libelle", libelle);
        corps.put("ordre", ordre);
        corps.put("piecesRequises", piecesRequises);
        corps.put("typeDocumentId", typeDocumentId);
        api.enregistrerMotif(id, corps);
        flash.addFlashAttribute("succes", Textes.t("message.motif_enregistre", libelle.trim()));
        return "redirect:/admin/motifs";
    }

    // ------------------------------------------------------------------ missions et pays

    @GetMapping("/missions")
    String missions(@RequestParam(required = false) Long modifier, Model model) {
        List<Vues.Mission> missions = api.toutesLesMissions();
        model.addAttribute("missions", missions);
        model.addAttribute("referentiel", api.diaspora());
        model.addAttribute("mission", modifier == null ? null : missions.stream().filter(m -> m.id().equals(modifier)).findFirst().orElse(null));
        return "admin/missions";
    }

    @PostMapping("/missions")
    String enregistrerMission(@RequestParam(required = false) Long id, @RequestParam String type, @RequestParam String nom,
                              @RequestParam Long paysId, @RequestParam String ville, @RequestParam(required = false) String codePostal,
                              @RequestParam(required = false) String competence, @RequestParam String adresse,
                              @RequestParam(required = false) String telephone, @RequestParam(required = false) String email,
                              @RequestParam(name = "paysCouvert", required = false) List<Long> paysCouverts,
                              @RequestParam(defaultValue = "false") boolean active, RedirectAttributes flash) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("type", type);
        corps.put("nom", nom);
        corps.put("paysId", paysId);
        corps.put("ville", ville);
        corps.put("codePostal", codePostal);
        corps.put("competence", competence);
        corps.put("adresse", adresse);
        corps.put("telephone", telephone);
        corps.put("email", email);
        corps.put("paysCouverts", paysCouverts == null ? List.of() : paysCouverts);
        corps.put("active", id == null || active);
        api.enregistrerMission(id, corps);
        flash.addFlashAttribute("succes", Textes.t("message.mission_enregistree", nom.trim()));
        return "redirect:/admin/missions";
    }

    @PostMapping("/missions/pays")
    String ajouterPays(@RequestParam String nom, @RequestParam String codeIso, @RequestParam(required = false) String continent,
                       @RequestParam(required = false) String capitale, @RequestParam(required = false) String zone,
                       RedirectAttributes flash) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("nom", nom);
        corps.put("codeIso", codeIso);
        corps.put("continent", continent);
        corps.put("capitale", capitale);
        corps.put("zone", vide(zone));
        api.ajouterPays(corps);
        flash.addFlashAttribute("succes", Textes.t("message.pays_ajoute", nom.trim()));
        return "redirect:/admin/missions";
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
