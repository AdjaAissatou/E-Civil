package sn.etatcivil.demande.adaptateur.sortie.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException;
import sn.etatcivil.demande.application.port.sortie.ServiceDocuments;
import sn.etatcivil.demande.domaine.RegleMetierException;
import sn.etatcivil.socle.securite.JetonServiceInterceptor;

import java.time.Duration;

@Component
class DocumentsHttp implements ServiceDocuments {

    private final RestClient http;

    DocumentsHttp(RestClient.Builder builder, JetonServiceInterceptor jeton,
                  @Value("${ecivil.services.document:http://localhost:8084}") String url) {
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(5));
        fabrique.setReadTimeout(Duration.ofSeconds(20));
        this.http = builder.baseUrl(url + "/api/documents").requestFactory(fabrique).requestInterceptor(jeton).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentJson(String codeVerification) {}

    @Override
    public String delivrer(DocumentADelivrer document) {
        try {
            DocumentJson r = http.post().contentType(MediaType.APPLICATION_JSON).body(document).retrieve().body(DocumentJson.class);
            return r.codeVerification();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 422) {
                ProblemDetail p = e.getResponseBodyAs(ProblemDetail.class);
                throw new RegleMetierException(p != null && p.getDetail() != null ? p.getDetail() : "Document refusé");
            }
            throw new RegistreIndisponibleException("Le service des documents a refusé la requête", e);
        } catch (RestClientException | IllegalStateException e) {
            throw new RegistreIndisponibleException("Le service des documents ne répond pas", e);
        }
    }
}
