package sn.etatcivil.portail.securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Chaque espace est réservé à un rôle : la vérification est faite ici pour toutes les URL,
 * et non plus page par page comme dans l'ancienne version.
 */
@Configuration
@EnableMethodSecurity
public class ConfigurationSecurite {

    @Bean
    PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain filtres(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/documents", "/inscription", "/connexion", "/verification/**",
                                "/css/**", "/images/**", "/favicon_io/**", "/mairie.png", "/error",
                                "/actuator/health").permitAll()
                        // Notification serveur à serveur de PayDunya : authentifiée par son empreinte (hash)
                        .requestMatchers("/paiements/paydunya/ipn").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/delegue/**").hasAnyRole("DELEGUE", "ADMIN")
                        .requestMatchers("/registre/**").hasAnyRole("AGENT_REGISTRE", "OFFICIER", "ADMIN")
                        .requestMatchers("/officier/**").hasAnyRole("OFFICIER", "ADMIN")
                        .requestMatchers("/back-office/**").hasAnyRole("DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN")
                        .requestMatchers("/demandes/**", "/paiements/**", "/profil").authenticated()
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/connexion")
                        .usernameParameter("email")
                        .passwordParameter("motDePasse")
                        .defaultSuccessUrl("/espace", true)
                        .failureUrl("/connexion?erreur")
                        .permitAll())
                .logout(l -> l.logoutUrl("/deconnexion").logoutSuccessUrl("/?deconnecte").permitAll())
                .csrf(c -> c.ignoringRequestMatchers("/paiements/paydunya/ipn"))
                .headers(h -> h
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .contentSecurityPolicy(c -> c.policyDirectives(
                                "default-src 'self'; script-src 'self' https://cdn.jsdelivr.net; "
                                        + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                        + "font-src 'self' https://cdn.jsdelivr.net; img-src 'self' data:; "
                                        + "form-action 'self' https://app.paydunya.com https://paydunya.com; "
                                        + "frame-ancestors 'none'")))
                .build();
    }
}
