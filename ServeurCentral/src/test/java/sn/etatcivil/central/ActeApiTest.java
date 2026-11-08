package sn.etatcivil.central;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ActeApiTest {

    static final String CLE = "cle-de-test-0123456789-abcdefghijklmnop";

    @Autowired
    MockMvc mvc;

    static final String NAISSANCE = """
            {"typeActe":"NAISSANCE","centre":"Centre principal de Dakar Plateau","commune":"Dakar Plateau",
             "volume":"12","folio":"34","dateEvenement":"1998-03-14","lieuEvenement":"Hôpital Principal, Dakar",
             "nom":"Diallo","prenom":"Fatou Bintou","sexe":"F",
             "nomPere":"Diallo","prenomPere":"Mamadou","nomMere":"Sow","prenomMere":"Aminata",
             "agent":"agent.registre@etatcivil.sn"}
            """;

    MockHttpServletRequestBuilder avecCle(MockHttpServletRequestBuilder r) {
        return r.header("X-API-Key", CLE).contentType(MediaType.APPLICATION_JSON);
    }

    long creer(String json) throws Exception {
        String rep = mvc.perform(avecCle(post("/api/v1/actes")).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(rep, "$.id")).longValue();
    }

    @Test
    void refuseSansCleOuAvecMauvaiseCle() throws Exception {
        mvc.perform(get("/api/v1/actes/statistiques")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/actes/statistiques").header("X-API-Key", "mauvaise"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void numeroteSansDoublonEtDeduitLaNaissance() throws Exception {
        long id = creer(NAISSANCE);
        mvc.perform(avecCle(get("/api/v1/actes/" + id)))
                .andExpect(jsonPath("$.numeroActe").value("N-1998-000001"))
                .andExpect(jsonPath("$.statut").value("SAISI"))
                .andExpect(jsonPath("$.dateNaissance").value("1998-03-14"))
                .andExpect(jsonPath("$.lieuNaissance").value("Hôpital Principal, Dakar"));
        long id2 = creer(NAISSANCE.replace("Fatou Bintou", "Awa"));
        mvc.perform(avecCle(get("/api/v1/actes/" + id2)))
                .andExpect(jsonPath("$.numeroActe").value("N-1998-000002"));
    }

    @Test
    void neRetrouveQueLesActesValidesEtIgnoreAccentsEtCasse() throws Exception {
        long id = creer(NAISSANCE);
        String recherche = """
                {"typeActe":"NAISSANCE","nom":"DIALLO","prenom":"fatou  bintou","dateNaissance":"1998-03-14",
                 "nomMere":"Sow"}
                """;
        mvc.perform(avecCle(post("/api/v1/actes/recherche")).content(recherche))
                .andExpect(jsonPath("$.resultat").value("NON_TROUVE"));

        mvc.perform(avecCle(post("/api/v1/actes/" + id + "/validation"))).andExpect(status().isOk());

        mvc.perform(avecCle(post("/api/v1/actes/recherche")).content(recherche))
                .andExpect(jsonPath("$.resultat").value("TROUVE"))
                .andExpect(jsonPath("$.actes[0].numeroActe").value("N-1998-000001"));

        // Un critère fourni qui contredit l'acte l'écarte
        mvc.perform(avecCle(post("/api/v1/actes/recherche"))
                        .content(recherche.replace("1998-03-14", "1999-03-14")))
                .andExpect(jsonPath("$.resultat").value("NON_TROUVE"));
    }

    @Test
    void retrouveUnMariageParLeConjoint() throws Exception {
        long id = creer("""
                {"typeActe":"MARIAGE","centre":"Centre de Thiès","dateEvenement":"2015-07-04","lieuEvenement":"Thiès",
                 "nom":"Ndiaye","prenom":"Ousmane","sexe":"M","nomConjoint":"Fall","prenomConjoint":"Mariama",
                 "dateNaissanceConjoint":"1990-01-02","agent":"agent"}
                """);
        mvc.perform(avecCle(post("/api/v1/actes/" + id + "/validation"))).andExpect(status().isOk());
        mvc.perform(avecCle(post("/api/v1/actes/recherche")).content("""
                        {"typeActe":"MARIAGE","nom":"Fall","prenom":"Mariama","dateNaissance":"1990-01-02",
                         "nomConjoint":"Ndiaye","prenomConjoint":"Ousmane"}
                        """))
                .andExpect(jsonPath("$.resultat").value("TROUVE"));
    }

    @Test
    void appliqueLesReglesMetier() throws Exception {
        mvc.perform(avecCle(post("/api/v1/actes")).content("""
                        {"typeActe":"MARIAGE","centre":"C","dateEvenement":"2015-07-04","lieuEvenement":"Thiès",
                         "nom":"Ndiaye","prenom":"Ousmane","agent":"agent"}
                        """))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(avecCle(post("/api/v1/actes")).content("{\"typeActe\":\"NAISSANCE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.champs.nom").exists());
        mvc.perform(avecCle(post("/api/v1/actes")).content("{\"typeActe\":\"INCONNU\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unActeValideNAccepteQueDesMentions() throws Exception {
        long id = creer(NAISSANCE);
        mvc.perform(avecCle(post("/api/v1/actes/" + id + "/validation")));
        String modif = NAISSANCE.replace("\"Diallo\",\"prenom\"", "\"Ba\",\"prenom\"")
                .replace("\"agent\"", "\"mentionsMarginales\":\"Marié le 04/07/2015 à Thiès\",\"agent\"");
        mvc.perform(avecCle(put("/api/v1/actes/" + id)).content(modif))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Diallo"))
                .andExpect(jsonPath("$.mentionsMarginales").value("Marié le 04/07/2015 à Thiès"));
        mvc.perform(avecCle(post("/api/v1/actes/" + id + "/validation")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listeEtStatistiques() throws Exception {
        creer(NAISSANCE);
        mvc.perform(avecCle(get("/api/v1/actes").param("nom", "diall")))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
        mvc.perform(avecCle(get("/api/v1/actes/statistiques")))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.saisis").value(1))
                .andExpect(jsonPath("$.parType.NAISSANCE").value(1));
    }
}
