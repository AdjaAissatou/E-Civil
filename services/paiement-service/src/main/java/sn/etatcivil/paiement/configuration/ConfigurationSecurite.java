package sn.etatcivil.paiement.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import sn.etatcivil.socle.securite.RolesKeycloak;

/** Serveur de ressources OAuth2 : chaque requête porte un jeton Keycloak ; les rôles sont vérifiés par méthode. */
@Configuration
@EnableMethodSecurity
class ConfigurationSecurite {

    @Bean
    SecurityFilterChain filtres(HttpSecurity http, RolesKeycloak roles) throws Exception {
        return http
                .csrf(c -> c.disable()) // API sans session ni cookie
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health/**", "/error").permitAll()
                        // notification serveur à serveur de PayDunya : authentifiée par sa signature
                        .requestMatchers(HttpMethod.POST, "/api/paiements/paydunya/ipn").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paiements/disponibilite").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(roles)))
                .build();
    }
}
