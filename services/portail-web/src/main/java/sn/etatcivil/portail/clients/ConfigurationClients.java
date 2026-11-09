package sn.etatcivil.portail.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

/**
 * Client HTTP vers la passerelle. Chaque appel porte le jeton d'accès Keycloak de l'utilisateur connecté
 * (renouvelé automatiquement grâce au jeton de rafraîchissement) ; les pages publiques appellent sans jeton.
 */
@Configuration
class ConfigurationClients {

    @Bean
    OAuth2AuthorizedClientManager gestionnaireJetons(ClientRegistrationRepository enregistrements,
                                                     OAuth2AuthorizedClientRepository clients) {
        var g = new DefaultOAuth2AuthorizedClientManager(enregistrements, clients);
        g.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode().refreshToken().build());
        return g;
    }

    @Bean
    RestClient clientHttpApi(RestClient.Builder builder, OAuth2AuthorizedClientManager jetons, ObjectMapper json,
                   @Value("${ecivil.api-url}") String url) {
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(5));
        fabrique.setReadTimeout(Duration.ofSeconds(30));
        return builder.baseUrl(url).requestFactory(fabrique)
                .requestInterceptor(jetonUtilisateur(jetons))
                .defaultStatusHandler(HttpStatusCode::isError, (req, rep) -> {
                    String detail = null;
                    try {
                        JsonNode corps = json.readTree(rep.getBody());
                        detail = corps.path("detail").asText(null);
                        if (corps.has("champs")) {
                            StringBuilder s = new StringBuilder(detail == null ? "Données invalides" : detail);
                            corps.path("champs").fields().forEachRemaining(c -> s.append(" — ").append(c.getKey())
                                    .append(" : ").append(c.getValue().asText()));
                            detail = s.toString();
                        }
                    } catch (Exception ignore) {
                        // corps vide ou non JSON
                    }
                    int code = rep.getStatusCode().value();
                    throw new ApiException(code, detail != null ? detail : switch (code) {
                        case 401, 403 -> "Accès refusé";
                        case 404 -> "Élément introuvable";
                        default -> "Un service de l'état civil ne répond pas pour le moment. Réessayez dans quelques minutes.";
                    });
                })
                .build();
    }

    private static ClientHttpRequestInterceptor jetonUtilisateur(OAuth2AuthorizedClientManager jetons) {
        return (requete, corps, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof OAuth2AuthenticationToken jeton
                    && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributs) {
                var client = jetons.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId(jeton.getAuthorizedClientRegistrationId())
                        .principal(auth)
                        .attribute(HttpServletRequest.class.getName(), attributs.getRequest())
                        .attribute(HttpServletResponse.class.getName(), attributs.getResponse())
                        .build());
                if (client != null) {
                    requete.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
                }
            }
            return execution.execute(requete, corps);
        };
    }
}
