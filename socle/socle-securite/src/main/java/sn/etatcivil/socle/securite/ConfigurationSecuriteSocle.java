package sn.etatcivil.socle.securite;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@AutoConfiguration(after = OAuth2ClientAutoConfiguration.class)
public class ConfigurationSecuriteSocle {

    @Bean
    @ConditionalOnMissingBean
    RolesKeycloak rolesKeycloak() {
        return new RolesKeycloak();
    }

    /** Uniquement pour les services qui appellent d'autres services (enregistrement « service » configuré). */
    @Bean
    @ConditionalOnProperty("spring.security.oauth2.client.registration.service.client-id")
    @ConditionalOnBean(ClientRegistrationRepository.class)
    JetonServiceInterceptor jetonServiceInterceptor(ClientRegistrationRepository enregistrements,
                                                    OAuth2AuthorizedClientService clients) {
        var gestionnaire = new AuthorizedClientServiceOAuth2AuthorizedClientManager(enregistrements, clients);
        gestionnaire.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
        return new JetonServiceInterceptor(gestionnaire);
    }
}
