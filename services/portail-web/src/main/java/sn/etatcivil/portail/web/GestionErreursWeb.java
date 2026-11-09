package sn.etatcivil.portail.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.clients.ApiException;
import sn.etatcivil.portail.securite.Utilisateur;

import java.net.URI;

/** Toute erreur d'un microservice ramène l'utilisateur sur sa page, avec le message du service. */
@ControllerAdvice
class GestionErreursWeb {

    @ModelAttribute("moi")
    Utilisateur moi(@AuthenticationPrincipal OidcUser utilisateur) {
        return utilisateur == null ? null : Utilisateur.depuis(utilisateur);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GestionErreursWeb.class);

    @ExceptionHandler(ApiException.class)
    String erreur(ApiException e, HttpServletRequest requete, RedirectAttributes flash,
                  @AuthenticationPrincipal OidcUser utilisateur, org.springframework.ui.Model model) {
        log.warn("{} {} : réponse {} d'un service ({})", requete.getMethod(), requete.getRequestURI(), e.statut(), e.getMessage());
        String retour = pagePrecedente(requete, utilisateur);
        if (retour.equals(requete.getRequestURI())) {
            // la page d'arrivée elle-même est en erreur : on l'affiche au lieu de boucler
            model.addAttribute("status", e.indisponible() ? 503 : e.statut());
            model.addAttribute("message", e.getMessage());
            return "error";
        }
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:" + retour;
    }

    static String pagePrecedente(HttpServletRequest requete, OidcUser utilisateur) {
        String referer = requete.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (uri.getHost() == null || uri.getHost().equals(requete.getServerName())) {
                    String chemin = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
                    if (chemin.startsWith("/") && !chemin.startsWith("//") && !chemin.equals(requete.getRequestURI())) return chemin;
                }
            } catch (IllegalArgumentException ignore) {
                // Referer invalide
            }
        }
        return utilisateur == null ? "/" : accueil(Utilisateur.depuis(utilisateur));
    }

    static String accueil(Utilisateur u) {
        return switch (u.role()) {
            case "ADMIN" -> "/admin";
            case "OFFICIER" -> "/officier";
            case "AGENT_REGISTRE" -> "/registre";
            case "DELEGUE" -> "/delegue";
            default -> "/demandes";
        };
    }
}
