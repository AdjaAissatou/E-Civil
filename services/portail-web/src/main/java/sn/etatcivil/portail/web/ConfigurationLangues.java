package sn.etatcivil.portail.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Langue du portail : français (par défaut), wolof ou anglais. Elle se choisit dans le pied de page (?lang=wo) ou
 * dans « Mon compte », et se retient dans un cookie. Les textes viennent de messages_*.properties.
 */
@Configuration
class ConfigurationLangues implements WebMvcConfigurer {

    static final List<String> LANGUES = List.of("fr", "wo", "en");

    @Bean
    LocaleResolver localeResolver() {
        CookieLocaleResolver r = new CookieLocaleResolver("langue");
        r.setDefaultLocale(Locale.FRENCH);
        r.setCookieMaxAge(Duration.ofDays(365));
        return r;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor i = new LocaleChangeInterceptor() {
            @Override
            public boolean preHandle(jakarta.servlet.http.HttpServletRequest requete,
                                     jakarta.servlet.http.HttpServletResponse reponse, Object handler)
                    throws jakarta.servlet.ServletException {
                String lang = requete.getParameter(getParamName());
                return lang == null || LANGUES.contains(lang) ? super.preHandle(requete, reponse, handler) : true;
            }
        };
        i.setParamName("lang");
        registry.addInterceptor(i);
    }
}
