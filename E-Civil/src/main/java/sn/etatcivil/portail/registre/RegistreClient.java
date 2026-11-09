package sn.etatcivil.portail.registre;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import sn.etatcivil.portail.catalogue.TypeActe;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.function.Supplier;

/** Accès au ServeurCentral (registre national) via son API REST v1. */
@Component
public class RegistreClient {

    private final RestClient http;
    private final ObjectMapper json;

    public RegistreClient(RestClient.Builder builder, ProprietesPortail proprietes, ObjectMapper json) {
        this.json = json;
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(5));
        fabrique.setReadTimeout(Duration.ofSeconds(15));
        String cle = proprietes.registre().apiKey() == null ? "" : proprietes.registre().apiKey();
        this.http = builder
                .baseUrl(proprietes.registre().url() + "/api/v1/actes")
                .requestFactory(fabrique)
                .defaultHeader("X-API-Key", cle)
                .defaultStatusHandler(HttpStatusCode::isError, (req, rep) -> traiterErreur(rep))
                .build();
    }

    public ResultatRecherche rechercher(CriteresRecherche criteres) {
        return appeler(() -> http.post().uri("/recherche").contentType(MediaType.APPLICATION_JSON)
                .body(criteres).retrieve().body(ResultatRecherche.class));
    }

    public ActeRegistre obtenir(Long id) {
        return appeler(() -> http.get().uri("/{id}", id).retrieve().body(ActeRegistre.class));
    }

    public ActeRegistre parNumero(String numero) {
        return appeler(() -> http.get().uri("/numero/{numero}", numero.trim()).retrieve().body(ActeRegistre.class));
    }

    public ActeRegistre creer(SaisieActe saisie) {
        return appeler(() -> http.post().contentType(MediaType.APPLICATION_JSON)
                .body(saisie).retrieve().body(ActeRegistre.class));
    }

    public ActeRegistre modifier(Long id, SaisieActe saisie) {
        return appeler(() -> http.put().uri("/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .body(saisie).retrieve().body(ActeRegistre.class));
    }

    public ActeRegistre valider(Long id) {
        return appeler(() -> http.post().uri("/{id}/validation", id).retrieve().body(ActeRegistre.class));
    }

    public PageActes lister(TypeActe type, String statut, String nom, int page) {
        return appeler(() -> http.get().uri(u -> filtres(u, type, statut, nom, page))
                .retrieve().body(PageActes.class));
    }

    public StatistiquesRegistre statistiques() {
        return appeler(() -> http.get().uri("/statistiques").retrieve().body(StatistiquesRegistre.class));
    }

    private static URI filtres(UriBuilder u, TypeActe type, String statut, String nom, int page) {
        if (type != null) u.queryParam("type", type);
        if (statut != null && !statut.isBlank()) u.queryParam("statut", statut);
        if (nom != null && !nom.isBlank()) u.queryParam("nom", nom);
        return u.queryParam("page", page).queryParam("size", 20).build();
    }

    private <T> T appeler(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (ResourceAccessException e) {
            throw new RegistreIndisponibleException("Le registre central est injoignable", e);
        }
    }

    private void traiterErreur(ClientHttpResponse reponse) throws IOException {
        HttpStatusCode code = reponse.getStatusCode();
        if (code.value() == HttpStatus.NOT_FOUND.value()) {
            throw new ActionRefuseeException("Aucun acte correspondant dans le registre central");
        }
        if (code.is4xxClientError() && code.value() != 401 && code.value() != 403) {
            throw new RefusRegistreException(detail(reponse));
        }
        throw new RegistreIndisponibleException("Le registre central a répondu " + code.value(), null);
    }

    /** Extrait le message d'erreur RFC 7807 renvoyé par le registre (et le détail des champs invalides). */
    private String detail(ClientHttpResponse reponse) {
        try {
            JsonNode corps = json.readTree(reponse.getBody());
            StringBuilder message = new StringBuilder(corps.path("detail").asText("Saisie refusée par le registre"));
            corps.path("champs").fields().forEachRemaining(c ->
                    message.append(" — ").append(c.getKey()).append(" : ").append(c.getValue().asText()));
            return message.toString();
        } catch (IOException | RuntimeException e) {
            return "Saisie refusée par le registre";
        }
    }
}
