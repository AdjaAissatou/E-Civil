package sn.etatcivil.socle.securite;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

/**
 * Ajoute à chaque appel sortant le jeton du service lui-même, obtenu auprès de Keycloak
 * par « client credentials » (enregistrement « service ») et mis en cache jusqu'à expiration.
 */
public class JetonServiceInterceptor implements ClientHttpRequestInterceptor {

    public static final String ENREGISTREMENT = "service";

    private final OAuth2AuthorizedClientManager gestionnaire;

    public JetonServiceInterceptor(OAuth2AuthorizedClientManager gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest requete, byte[] corps, ClientHttpRequestExecution execution)
            throws IOException {
        OAuth2AuthorizedClient client = gestionnaire.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(ENREGISTREMENT).principal(ENREGISTREMENT).build());
        if (client == null) throw new IllegalStateException("Impossible d'obtenir un jeton de service auprès de Keycloak");
        requete.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
        return execution.execute(requete, corps);
    }
}
