package sn.etatcivil.portail.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sn.etatcivil.portail.registre.RegistreIndisponibleException;
import sn.etatcivil.portail.securite.UtilisateurConnecte;

import java.net.URI;

/**
 * Une action refusée renvoie l'utilisateur sur la page d'où il vient, avec un message clair.
 * Aucun détail technique (exception, SQL…) n'est jamais affiché.
 */
@ControllerAdvice
public class GestionErreursWeb {

    private static final Logger log = LoggerFactory.getLogger(GestionErreursWeb.class);

    @ModelAttribute("moi")
    UtilisateurConnecte utilisateurConnecte(@AuthenticationPrincipal UtilisateurConnecte moi) {
        return moi;
    }

    @ExceptionHandler(ActionRefuseeException.class)
    String refus(ActionRefuseeException e, HttpServletRequest requete, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:" + pagePrecedente(requete);
    }

    @ExceptionHandler(RegistreIndisponibleException.class)
    String registre(RegistreIndisponibleException e, HttpServletRequest requete, RedirectAttributes flash) {
        log.warn("Registre central indisponible : {}", e.getMessage());
        flash.addFlashAttribute("erreur", "Le registre central ne répond pas pour le moment. Réessayez dans quelques minutes.");
        return "redirect:" + pagePrecedente(requete);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    String conflit(HttpServletRequest requete, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", "Cette demande vient d'être modifiée par quelqu'un d'autre. Vérifiez son état puis recommencez.");
        return "redirect:" + pagePrecedente(requete);
    }

    /** Seuls les chemins internes au portail sont acceptés (pas de redirection vers un site tiers). */
    static String pagePrecedente(HttpServletRequest requete) {
        String referer = requete.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (uri.getHost() == null || uri.getHost().equals(requete.getServerName())) {
                    String chemin = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
                    if (chemin.startsWith("/") && !chemin.startsWith("//")) return chemin;
                }
            } catch (IllegalArgumentException ignore) {
                // Referer invalide : on retombe sur l'espace personnel
            }
        }
        return accueilDuRole();
    }

    /** Page d'accueil directe du rôle connecté (un message flash ne survit qu'à une seule redirection). */
    private static String accueilDuRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UtilisateurConnecte moi)) return "/";
        return switch (moi.getRole()) {
            case CITOYEN -> "/demandes";
            case DELEGUE -> "/delegue";
            case AGENT_REGISTRE -> "/registre";
            case OFFICIER -> "/officier";
            case ADMIN -> "/admin";
        };
    }
}
