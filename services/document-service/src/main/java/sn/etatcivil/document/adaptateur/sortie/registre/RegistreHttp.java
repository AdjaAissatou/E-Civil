package sn.etatcivil.document.adaptateur.sortie.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import sn.etatcivil.document.application.port.sortie.RegistreActes;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.RegleMetierException;
import sn.etatcivil.socle.securite.JetonServiceInterceptor;

import java.time.Duration;
import java.util.UUID;

/** Relit l'acte dans registre-service, avec le jeton de service de document-service. */
@Component
class RegistreHttp implements RegistreActes {

    private final RestClient http;

    RegistreHttp(RestClient.Builder builder, JetonServiceInterceptor jeton,
                 @Value("${ecivil.services.registre:http://localhost:8083}") String url) {
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(5));
        fabrique.setReadTimeout(Duration.ofSeconds(15));
        this.http = builder.baseUrl(url + "/api/registre/actes").requestFactory(fabrique).requestInterceptor(jeton).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ActeJson(String typeActe, String numeroActe, int annee, String statut, String centre, String commune,
                    String volume, String folio, java.time.LocalDate dateEvenement, String lieuEvenement, String nom,
                    String prenom, String sexe, java.time.LocalDate dateNaissance, String lieuNaissance, String nomPere,
                    String prenomPere, String nomMere, String prenomMere, String nomConjoint, String prenomConjoint,
                    java.time.LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint, String heureDeces,
                    String causeDeces, String temoin1, String temoin2, String mentionsMarginales, String observations) {
        ContenuActe versDomaine() {
            return new ContenuActe(typeActe, numeroActe, annee, statut, centre, commune, volume, folio, dateEvenement,
                    lieuEvenement, nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                    prenomMere, nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint, heureDeces,
                    causeDeces, temoin1, temoin2, mentionsMarginales, observations);
        }
    }

    @Override
    public ContenuActe contenu(UUID acteId) {
        try {
            return http.get().uri("/{id}", acteId).retrieve().body(ActeJson.class).versDomaine();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RegleMetierException("L'acte n'existe pas dans le registre national");
        }
    }
}
