package sn.etatcivil.portail.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Textes du portail dans la langue de la requête en cours (messages_fr, messages_wo, messages_en). Accès statique
 * pour les messages produits en Java (confirmations, libellés calculés) ; les pages utilisent #{…}.
 */
@Component
public class Textes {

    private static MessageSource source;

    Textes(MessageSource messageSource) {
        source = messageSource;
    }

    /** Texte de la clé, avec ses paramètres {0}, {1}… ; à défaut, la clé elle-même. */
    public static String t(String cle, Object... parametres) {
        if (source == null) return cle;
        return source.getMessage(cle, parametres.length == 0 ? null : parametres, cle, LocaleContextHolder.getLocale());
    }
}
