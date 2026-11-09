package sn.etatcivil.portail.securite;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Connexion déléguée à Keycloak (OpenID Connect). Les rôles du royaume deviennent des autorités Spring ;
 * chaque espace du portail est réservé aux rôles concernés.
 */
@Configuration
class ConfigurationSecurite {

    @Bean
    SecurityFilterChain filtres(HttpSecurity http, ClientRegistrationRepository enregistrements,
                                @Value("${ecivil.keycloak.url-publique}") String keycloak,
                                @Value("${ecivil.url-publique}") String portail) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/documents", "/inscription", "/verification/**", "/css/**", "/js/**", "/images/**",
                                "/favicon_io/**", "/mairie.png", "/error", "/actuator/health/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/delegue/**").hasAnyRole("DELEGUE", "ADMIN")
                        .requestMatchers("/registre/**").hasAnyRole("AGENT_REGISTRE", "OFFICIER", "ADMIN")
                        .requestMatchers("/officier/**").hasAnyRole("OFFICIER", "ADMIN")
                        .requestMatchers("/back-office/**").hasAnyRole("DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(o -> o
                        .loginPage("/oauth2/authorization/keycloak")
                        .authorizationEndpoint(e -> e.authorizationRequestResolver(resolveur(enregistrements, keycloak)))
                        .userInfoEndpoint(u -> u.userAuthoritiesMapper(rolesKeycloak()))
                        .defaultSuccessUrl("/espace", true))
                .logout(l -> l.logoutUrl("/deconnexion").logoutSuccessHandler(deconnexionKeycloak(keycloak, portail)))
                .headers(h -> h
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(c -> c.policyDirectives(
                                "default-src 'self'; script-src 'self' https://cdn.jsdelivr.net; "
                                        + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                        + "font-src 'self' https://cdn.jsdelivr.net; img-src 'self' data:; "
                                        + "form-action 'self' " + keycloak + " https://app.paydunya.com https://paydunya.com; "
                                        + "frame-ancestors 'none'")))
                .build();
    }

    /**
     * Connexion avec PKCE ; « Créer un compte » ouvre directement le formulaire d'inscription de Keycloak
     * (point d'entrée « registrations », qui reconnecte ensuite l'utilisateur sur le portail).
     */
    private static OAuth2AuthorizationRequestResolver resolveur(ClientRegistrationRepository enregistrements, String keycloak) {
        var r = new DefaultOAuth2AuthorizationRequestResolver(enregistrements, "/oauth2/authorization");
        r.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce().andThen(b -> {
            HttpServletRequest requete = requeteCourante();
            if (requete != null && requete.getParameter("inscription") != null) {
                b.authorizationUri(keycloak + "/realms/ecivil/protocol/openid-connect/registrations");
            }
        }));
        return r;
    }

    private static HttpServletRequest requeteCourante() {
        var attributs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        return attributs instanceof org.springframework.web.context.request.ServletRequestAttributes s ? s.getRequest() : null;
    }

    /** Rôles Keycloak (claim realm_access.roles du jeton d'identité) → ROLE_CITOYEN, ROLE_OFFICIER… */
    private static GrantedAuthoritiesMapper rolesKeycloak() {
        return (Collection<? extends GrantedAuthority> autorites) -> {
            Set<GrantedAuthority> resultat = new HashSet<>(autorites);
            for (GrantedAuthority a : autorites) {
                var claims = a instanceof OidcUserAuthority o ? o.getIdToken().getClaims()
                        : a instanceof OAuth2UserAuthority u ? u.getAttributes() : null;
                if (claims != null) {
                    Utilisateur.roles(claims).forEach(r -> resultat.add(new SimpleGrantedAuthority("ROLE_" + r)));
                }
            }
            return resultat;
        };
    }

    /** Déconnexion complète : session du portail puis session Keycloak (RP-initiated logout). */
    private static LogoutSuccessHandler deconnexionKeycloak(String keycloak, String portail) {
        return (requete, reponse, auth) -> {
            var url = UriComponentsBuilder.fromHttpUrl(keycloak + "/realms/ecivil/protocol/openid-connect/logout")
                    .queryParam("client_id", "portail-web")
                    .queryParam("post_logout_redirect_uri", portail + "/");
            if (auth != null && auth.getPrincipal() instanceof OidcUser u) {
                url.queryParam("id_token_hint", u.getIdToken().getTokenValue());
            }
            reponse.sendRedirect(url.encode().toUriString());
        };
    }
}
