package sn.etatcivil.demande.adaptateur.sortie.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import sn.etatcivil.demande.application.port.sortie.RegistreCivil;
import sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException;
import sn.etatcivil.demande.domaine.ActeRetrouve;
import sn.etatcivil.demande.domaine.OrigineActe;
import sn.etatcivil.demande.domaine.PersonneConcernee;
import sn.etatcivil.demande.domaine.SituationCivile;
import sn.etatcivil.demande.domaine.TypeActe;
import sn.etatcivil.socle.securite.JetonServiceInterceptor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Appelle registre-service avec le jeton de service de demande-service (client credentials Keycloak). */
@Component
class RegistreHttp implements RegistreCivil {

    private final RestClient http;

    RegistreHttp(RestClient.Builder builder, JetonServiceInterceptor jeton,
                 @Value("${ecivil.services.registre:http://localhost:8083}") String url) {
        var fabrique = new SimpleClientHttpRequestFactory();
        fabrique.setConnectTimeout(Duration.ofSeconds(5));
        fabrique.setReadTimeout(Duration.ofSeconds(15));
        this.http = builder.baseUrl(url + "/api/registre/actes").requestFactory(fabrique).requestInterceptor(jeton).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ActeJson(UUID id, String numeroActe, TypeActe typeActe, String statut) {
        ActeRetrouve versDomaine() { return new ActeRetrouve(id, numeroActe, typeActe, "VALIDE".equals(statut)); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ResultatJson(Resultat resultat, List<ActeJson> actes) {}

    record CriteresJson(TypeActe typeActe, String nom, String prenom, LocalDate dateNaissance, String lieuNaissance,
                        String nomPere, String prenomPere, String nomMere, String prenomMere,
                        String nomConjoint, String prenomConjoint, LocalDate dateEvenement, OrigineActe origine) {}

    record PersonneJson(String nom, String prenom, LocalDate dateNaissance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MariageJson(ActeJson acte, String nomConjoint, String prenomConjoint, boolean dissous) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SituationJson(List<MariageJson> mariages, List<ActeJson> deces) {}

    @Override
    public ResultatRecherche rechercher(TypeActe type, PersonneConcernee p, OrigineActe origine) {
        CriteresJson c = new CriteresJson(type, p.nom(), p.prenom(), p.dateNaissance(), p.lieuNaissance(), p.nomPere(),
                p.prenomPere(), p.nomMere(), p.prenomMere(), p.nomConjoint(), p.prenomConjoint(), p.dateEvenement(), origine);
        ResultatJson r = appeler(() -> http.post().uri("/recherche").contentType(MediaType.APPLICATION_JSON).body(c)
                .retrieve().body(ResultatJson.class));
        return new ResultatRecherche(r.resultat(), r.actes().stream().map(ActeJson::versDomaine).toList());
    }

    @Override
    public SituationCivile situation(String nom, String prenom, LocalDate dateNaissance) {
        SituationJson s = appeler(() -> http.post().uri("/situation").contentType(MediaType.APPLICATION_JSON)
                .body(new PersonneJson(nom, prenom, dateNaissance)).retrieve().body(SituationJson.class));
        return new SituationCivile(
                s.mariages().stream().map(m -> new SituationCivile.Mariage(m.acte().numeroActe(),
                        (m.prenomConjoint() + " " + m.nomConjoint()).trim(), m.dissous())).toList(),
                s.deces().stream().map(ActeJson::numeroActe).toList());
    }

    @Override
    public Optional<ActeRetrouve> parNumero(String numero) {
        return chercher(() -> http.get().uri("/numero/{n}", numero).retrieve().body(ActeJson.class));
    }

    @Override
    public Optional<ActeRetrouve> parId(UUID id) {
        return chercher(() -> http.get().uri("/{id}", id).retrieve().body(ActeJson.class));
    }

    private Optional<ActeRetrouve> chercher(Supplier<ActeJson> appel) {
        try {
            return Optional.ofNullable(appeler(appel)).map(ActeJson::versDomaine);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private static <T> T appeler(Supplier<T> appel) {
        try {
            return appel.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw e;
        } catch (RestClientException | IllegalStateException e) {
            throw new RegistreIndisponibleException("Le registre national ne répond pas", e);
        }
    }
}
