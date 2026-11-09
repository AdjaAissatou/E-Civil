package sn.etatcivil.central.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class ConfigurationSecurite {

    @Bean
    SecurityFilterChain filtres(HttpSecurity http, ProprietesApi proprietes) throws Exception {
        String cle = proprietes.apiKey();
        if (cle == null || cle.length() < 32) {
            throw new IllegalStateException(
                    "REGISTRE_API_KEY doit être définie (au moins 32 caractères) avant de démarrer le serveur central");
        }
        return http
                .csrf(c -> c.disable()) // API sans session ni cookie : pas de risque CSRF
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .addFilterBefore(new FiltreCleApi(cle), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers("/api/v1/**").hasRole("PORTAIL")
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    /** Évite que Spring Boot génère un utilisateur par défaut avec un mot de passe affiché dans les logs. */
    @Bean
    UserDetailsService aucunUtilisateur() {
        return new InMemoryUserDetailsManager();
    }
}
