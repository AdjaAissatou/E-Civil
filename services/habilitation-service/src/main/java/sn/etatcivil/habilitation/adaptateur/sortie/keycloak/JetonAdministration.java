package sn.etatcivil.habilitation.adaptateur.sortie.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Jeton d'administration Keycloak (compte administrateur du realm « master »), mis en cache jusqu'à
 * 30 secondes avant son expiration. Les identifiants viennent de l'environnement (.env), jamais du code.
 */
class JetonAdministration {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Reponse(@JsonProperty("access_token") String jeton, @JsonProperty("expires_in") long duree) {}

    private final RestClient http;
    private final String utilisateur;
    private final String motDePasse;
    private String jeton;
    private Instant expiration = Instant.EPOCH;

    JetonAdministration(RestClient http, String utilisateur, String motDePasse) {
        this.http = http;
        this.utilisateur = utilisateur;
        this.motDePasse = motDePasse;
    }

    synchronized String valeur() {
        if (jeton == null || Instant.now().isAfter(expiration)) {
            var formulaire = new LinkedMultiValueMap<String, String>();
            formulaire.add("grant_type", "password");
            formulaire.add("client_id", "admin-cli");
            formulaire.add("username", utilisateur);
            formulaire.add("password", motDePasse);
            Reponse r = http.post().uri("/realms/master/protocol/openid-connect/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(formulaire)
                    .retrieve().body(Reponse.class);
            if (r == null || r.jeton() == null) throw new IllegalStateException("Keycloak n'a pas délivré de jeton d'administration");
            jeton = r.jeton();
            expiration = Instant.now().plusSeconds(Math.max(10, r.duree() - 30));
        }
        return jeton;
    }

    synchronized void oublier() {
        jeton = null;
    }
}
