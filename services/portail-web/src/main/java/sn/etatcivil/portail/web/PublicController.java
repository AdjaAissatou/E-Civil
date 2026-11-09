package sn.etatcivil.portail.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.securite.Utilisateur;

import java.util.Locale;

@Controller
class PublicController {

    private final Api api;

    PublicController(Api api) {
        this.api = api;
    }

    @GetMapping("/")
    String accueil(Model model) {
        // Prix « à partir de » par type d'acte, tirés du vrai catalogue
        java.util.Map<String, Integer> aPartirDe = new java.util.HashMap<>();
        try {
            api.documents().forEach(d -> aPartirDe.merge(d.typeActe(), d.tarif(), Math::min));
        } catch (ApiException e) {
            // catalogue indisponible : les cartes s'affichent sans prix
        }
        model.addAttribute("aPartirDe", aPartirDe);
        model.addAttribute("pageAccueil", true);
        return "accueil";
    }

    @GetMapping("/documents")
    String documents(Model model) {
        model.addAttribute("documents", api.documents());
        return "documents";
    }

    /** Ouvre directement la page d'inscription de Keycloak. */
    @GetMapping("/inscription")
    String inscription() {
        return "redirect:/oauth2/authorization/keycloak?inscription";
    }

    /** Gestion du compte (mot de passe, coordonnées) : console de compte Keycloak. */
    @GetMapping("/compte")
    String compte(@org.springframework.beans.factory.annotation.Value("${ecivil.keycloak.url-publique}") String keycloak) {
        return "redirect:" + keycloak + "/realms/ecivil/account";
    }

    /** Page d'arrivée après connexion, selon le rôle. */
    @GetMapping("/espace")
    String espace(@AuthenticationPrincipal OidcUser u) {
        return "redirect:" + GestionErreursWeb.accueil(Utilisateur.depuis(u));
    }

    @GetMapping("/verification")
    String verification(@RequestParam(required = false) String code) {
        if (code != null && !code.isBlank()) {
            return "redirect:/verification/" + code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9-]", "");
        }
        return "verification";
    }

    @GetMapping("/verification/{code}")
    String verifier(@PathVariable String code, Model model) {
        model.addAttribute("code", code);
        api.verifier(code).ifPresent(v -> model.addAttribute("document", v));
        return "verification";
    }
}
