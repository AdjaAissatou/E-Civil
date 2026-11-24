package sn.etatcivil.portail.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;
import sn.etatcivil.portail.i18n.Textes;

import java.time.LocalDate;
import java.util.*;

/**
 * Espace du chargé de renseignement : dossier de recherche d'une demande dont l'acte manque au registre national
 * (registres papier consultés, conclusion), centres d'état civil et registres papier qu'ils tiennent.
 */
@Controller
class RenseignementController {

    static final List<String> TYPES_CENTRE = List.of("PRINCIPAL", "SECONDAIRE", "CONSULAIRE");
    static final List<String> TYPES_ACTE = List.of("NAISSANCE", "MARIAGE", "DIVORCE", "DECES", "RECONNAISSANCE");
    static final List<String> EXEMPLAIRES = List.of("CENTRE", "GREFFE");
    static final List<String> ETATS = List.of("BON", "ABIME", "INCOMPLET", "MANQUANT");
    static final List<String> RESULTATS = List.of("ABSENT", "ACTE_PRESENT", "PAGE_ILLISIBLE", "REGISTRE_INDISPONIBLE");

    private final Api api;

    RenseignementController(Api api) {
        this.api = api;
    }

    static boolean peut(Authentication a, String permission) {
        return a != null && a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_" + permission));
    }

    /**
     * Ajoute à la fiche d'une demande son dossier de renseignement et les registres papier à consulter : ceux du type
     * d'acte, pour l'année de l'événement et les deux années voisines (ou l'année choisie par l'agent).
     */
    void completerFiche(Vues.Demande d, Integer annee, Authentication a, Model model) {
        if (!peut(a, "DEMANDES_RECHERCHER") && !peut(a, "DEMANDES_SUPERVISER")) return;
        try {
            api.renseignement(d.id()).ifPresent(r -> model.addAttribute("renseignement", r));
        } catch (ApiException e) {
            model.addAttribute("renseignementIndisponible", true);
            return;
        }
        boolean enAttente = "PAYEE".equals(d.statut()) || "ACTE_INTROUVABLE".equals(d.statut());
        if (!enAttente || !peut(a, "DEMANDES_RECHERCHER")) return;
        LocalDate date = "NAISSANCE".equals(d.typeActe()) ? d.dateNaissance() : d.dateEvenement(); // inconnue : tous les registres
        Integer centrale = annee != null ? annee : date == null ? null : date.getYear();
        List<Vues.RegistrePapier> registres = new ArrayList<>();
        try {
            if (centrale == null) {
                registres.addAll(api.registresPapier(null, d.typeActe(), null));
            } else if (annee != null) {
                registres.addAll(api.registresPapier(null, d.typeActe(), annee));
            } else {
                for (int y = centrale - 1; y <= centrale + 1; y++) registres.addAll(api.registresPapier(null, d.typeActe(), y));
            }
        } catch (ApiException e) {
            model.addAttribute("registresIndisponibles", true);
        }
        model.addAttribute("anneeRecherchee", centrale);
        model.addAttribute("registresSuggeres", registres);
        model.addAttribute("resultats", RESULTATS);
    }

    // ---------------------------------------------------------------- dossier de renseignement

    @PostMapping("/registre/demandes/{id}/renseignement/prise-en-charge")
    String prendreEnCharge(@PathVariable UUID id, RedirectAttributes flash) {
        api.actionRenseignement(id, "prise-en-charge", null);
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.pris_en_charge"));
        return fiche(id);
    }

    @PostMapping("/registre/demandes/{id}/renseignement/priorite")
    String priorite(@PathVariable UUID id, @RequestParam boolean urgent, RedirectAttributes flash) {
        api.actionRenseignement(id, "priorite", Map.of("urgent", urgent));
        flash.addFlashAttribute("succes", Textes.t(urgent ? "renseignement.message.urgent" : "renseignement.message.normal"));
        return fiche(id);
    }

    @PostMapping("/registre/demandes/{id}/renseignement/consultations")
    String consigner(@PathVariable UUID id, @RequestParam Long registreId, @RequestParam String resultat,
                     @RequestParam(required = false) String folio, @RequestParam(required = false) String note,
                     RedirectAttributes flash) {
        Vues.RegistrePapier registre = api.registrePapier(registreId);
        Map<String, Object> corps = new HashMap<>();
        corps.put("registreId", registreId);
        corps.put("registre", registre.libelle());
        corps.put("resultat", resultat);
        corps.put("folio", folio);
        corps.put("note", note);
        api.actionRenseignement(id, "consultations", corps);
        flash.addFlashAttribute("succes", Textes.t("ACTE_PRESENT".equals(resultat)
                ? "renseignement.message.acte_present" : "renseignement.message.consultation"));
        return fiche(id);
    }

    @PostMapping("/registre/demandes/{id}/renseignement/conclusion")
    String conclure(@PathVariable UUID id, @RequestParam String motif, RedirectAttributes flash) {
        api.actionRenseignement(id, "conclusion", Map.of("motif", motif));
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.non_trouve"));
        return fiche(id);
    }

    private static String fiche(UUID id) {
        return "redirect:/back-office/demandes/" + id;
    }

    // ---------------------------------------------------------------- centres et registres papier

    @GetMapping("/registre/centres")
    String centres(@RequestParam(required = false) String recherche, @RequestParam(required = false) String type, Model model) {
        model.addAttribute("centres", api.centres(recherche, type));
        model.addAttribute("principaux", api.centres(null, "PRINCIPAL"));
        model.addAttribute("recherche", recherche);
        model.addAttribute("type", type);
        model.addAttribute("typesCentre", TYPES_CENTRE);
        model.addAttribute("communes", api.communes());
        return "registre/centres";
    }

    @PostMapping("/registre/centres")
    String creerCentre(@RequestParam Map<String, String> p, RedirectAttributes flash) {
        Vues.CentreEtatCivil c = api.enregistrerCentre(null, centre(p));
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.centre_enregistre", c.nom()));
        return "redirect:/registre/centres/" + c.id();
    }

    @GetMapping("/registre/centres/{id}")
    String centre(@PathVariable long id, Model model) {
        model.addAttribute("centre", api.centre(id));
        model.addAttribute("registres", api.registresPapier(id, null, null));
        model.addAttribute("principaux", api.centres(null, "PRINCIPAL"));
        model.addAttribute("typesCentre", TYPES_CENTRE);
        model.addAttribute("typesActe", TYPES_ACTE);
        model.addAttribute("exemplaires", EXEMPLAIRES);
        model.addAttribute("etats", ETATS);
        model.addAttribute("communes", api.communes());
        return "registre/centre";
    }

    @PostMapping("/registre/centres/{id}")
    String modifierCentre(@PathVariable long id, @RequestParam Map<String, String> p, RedirectAttributes flash) {
        Vues.CentreEtatCivil c = api.enregistrerCentre(id, centre(p));
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.centre_enregistre", c.nom()));
        return "redirect:/registre/centres/" + id;
    }

    @PostMapping("/registre/centres/{id}/registres")
    String ajouterRegistre(@PathVariable long id, @RequestParam Map<String, String> p, RedirectAttributes flash) {
        Vues.RegistrePapier r = api.enregistrerRegistrePapier(null, registre(id, p));
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.registre_enregistre", r.libelle()));
        return "redirect:/registre/centres/" + id;
    }

    @PostMapping("/registre/registres/{id}")
    String modifierRegistre(@PathVariable long id, @RequestParam Map<String, String> p, RedirectAttributes flash) {
        Vues.RegistrePapier avant = api.registrePapier(id);
        Map<String, Object> corps = registre(avant.centreId(), p);
        corps.putIfAbsent("typeActe", avant.typeActe());
        Vues.RegistrePapier r = api.enregistrerRegistrePapier(id, corps);
        flash.addFlashAttribute("succes", Textes.t("renseignement.message.registre_enregistre", r.libelle()));
        return "redirect:/registre/centres/" + avant.centreId();
    }

    private static Map<String, Object> centre(Map<String, String> p) {
        Map<String, Object> c = new HashMap<>();
        c.put("nom", p.get("nom"));
        c.put("type", vide(p.get("type")) ? null : p.get("type"));
        c.put("commune", p.get("commune"));
        c.put("pays", p.get("pays"));
        c.put("rattachementId", vide(p.get("rattachementId")) ? null : Long.valueOf(p.get("rattachementId")));
        c.put("actif", !p.containsKey("actifPresent") || p.containsKey("actif"));
        return c;
    }

    private static Map<String, Object> registre(long centreId, Map<String, String> p) {
        Map<String, Object> r = new HashMap<>();
        r.put("centreId", centreId);
        if (!vide(p.get("typeActe"))) r.put("typeActe", p.get("typeActe"));
        r.put("annee", entier(p.get("annee")));
        r.put("exemplaire", vide(p.get("exemplaire")) ? null : p.get("exemplaire"));
        r.put("volume", p.get("volume"));
        r.put("lieuConservation", p.get("lieuConservation"));
        r.put("etat", vide(p.get("etat")) ? null : p.get("etat"));
        r.put("numerise", p.containsKey("numerise"));
        r.put("observations", p.get("observations"));
        return r;
    }

    /** Nombre saisi, ou {@code null} s'il est absent ou illisible (le service explique alors ce qui est attendu). */
    private static Integer entier(String s) {
        try {
            return vide(s) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
