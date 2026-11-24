package sn.etatcivil.portail.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.clients.Vues;

import java.time.LocalDate;
import java.util.Locale;

/**
 * « Mon compte » : informations personnelles de l'inscription, lieu de résidence (Sénégal ou diaspora),
 * préférences de notification et de langue. Le mot de passe se change dans l'espace sécurisé de Keycloak.
 */
@Controller
@RequestMapping("/compte")
class CompteController {

    private final Api api;
    private final LocaleResolver langues;

    CompteController(Api api, LocaleResolver langues) {
        this.api = api;
        this.langues = langues;
    }

    @GetMapping
    String compte(Model model) {
        model.addAttribute("profil", api.monProfil());
        model.addAttribute("pays", api.diaspora().pays());
        return "compte";
    }

    @PostMapping
    String enregistrer(@RequestParam String prenom, @RequestParam String nom,
                       @RequestParam(required = false) String telephone, @RequestParam(required = false) String sexe,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateNaissance,
                       @RequestParam(required = false) String lieuNaissance, @RequestParam(required = false) String profession,
                       @RequestParam(required = false) String residence, @RequestParam(required = false) String localite,
                       @RequestParam(required = false) Integer nombreEnfants, @RequestParam(required = false) String numeroCni,
                       @RequestParam(defaultValue = "RESIDENT") String typeUtilisateur,
                       @RequestParam(required = false) String paysResidence,
                       @RequestParam(defaultValue = "false") boolean notifications,
                       @RequestParam(defaultValue = "fr") String langue,
                       HttpServletRequest requete, HttpServletResponse reponse, RedirectAttributes flash) {
        Vues.ProfilPersonnel p = api.modifierMonProfil(new Vues.ProfilPersonnel(prenom, nom, null, telephone,
                vide(sexe), dateNaissance, lieuNaissance, profession, residence, localite, nombreEnfants, numeroCni,
                typeUtilisateur, paysResidence, notifications, langue));
        try {
            api.preferencesNotifications(p.notifications());
        } catch (ApiException e) {
            if (e.indisponible()) {
                flash.addFlashAttribute("erreur", "Profil enregistré, mais la préférence de notification n'a pas pu être "
                        + "transmise : réessayez dans quelques minutes.");
                return "redirect:/compte";
            }
            throw e;
        }
        langues.setLocale(requete, reponse, Locale.forLanguageTag(p.langue()));
        flash.addFlashAttribute("succes", "Vos informations sont enregistrées. Votre nom à jour s'affichera à votre prochaine connexion.");
        return "redirect:/compte";
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
