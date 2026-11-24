package sn.etatcivil.registre;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Adaptateurs réels (REST, JPA, outbox) avec une base H2 et des jetons Keycloak simulés. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ActeApiTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    static RequestPostProcessor role(String role) {
        return jwt().jwt(j -> j.subject("u-" + role).claim("email", role.toLowerCase() + "@ecivil.sn")
                        .claim("given_name", "Awa").claim("family_name", "Ndiaye")
                        .claim("realm_access", Map.of("roles", sn.etatcivil.socle.tests.JetonsDeTest.roles(role))))
                .authorities(sn.etatcivil.socle.tests.JetonsDeTest.roles(role).stream().<org.springframework.security.core.GrantedAuthority>map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r)).toList());
    }

    static final String NAISSANCE = """
            {"typeActe":"NAISSANCE","centre":"Centre principal de Dakar Plateau","volume":"12","folio":"34",
             "dateEvenement":"1998-03-14","lieuEvenement":"Hôpital Principal, Dakar","nom":"Diallo",
             "prenom":"Fatou Bintou","sexe":"F","nomPere":"Diallo","prenomPere":"Mamadou","nomMere":"Sow","prenomMere":"Aminata"}
            """;

    MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r) {
        return r.contentType(MediaType.APPLICATION_JSON);
    }

    String transcrire() throws Exception {
        String rep = mvc.perform(json(post("/api/registre/actes")).content(NAISSANCE).with(role("AGENT_REGISTRE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroActe").value("N-1998-000001"))
                .andExpect(jsonPath("$.agentSaisie").value("Awa Ndiaye <agent_registre@ecivil.sn>"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(rep, "$.id");
    }

    @Test
    void controleDesRoles() throws Exception {
        mvc.perform(get("/api/registre/actes/statistiques")).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/api/registre/actes")).content(NAISSANCE).with(role("CITOYEN"))).andExpect(status().isForbidden());
        String id = transcrire();
        mvc.perform(post("/api/registre/actes/" + id + "/validation").with(role("AGENT_REGISTRE"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/registre/actes").with(role("SERVICE"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/registre/actes/" + id).with(role("SERVICE"))).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void validationPublieUnEvenementDansLOutbox() throws Exception {
        String id = transcrire();
        String recherche = """
                {"typeActe":"NAISSANCE","nom":"DIALLO","prenom":"fatou bintou","dateNaissance":"1998-03-14","lieuNaissance":"Dakar"}
                """;
        mvc.perform(json(post("/api/registre/actes/recherche")).content(recherche).with(role("SERVICE")))
                .andExpect(jsonPath("$.resultat").value("NON_TROUVE"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM evenement_sortant", Integer.class)).isZero();

        mvc.perform(post("/api/registre/actes/" + id + "/validation").with(role("OFFICIER"))).andExpect(status().isOk());
        assertThat(jdbc.queryForList("SELECT routage FROM evenement_sortant", String.class)).containsExactly("registre.acte-valide");

        mvc.perform(json(post("/api/registre/actes/recherche")).content(recherche).with(role("SERVICE")))
                .andExpect(jsonPath("$.resultat").value("TROUVE"))
                .andExpect(jsonPath("$.actes[0].numeroActe").value("N-1998-000001"));
        mvc.perform(get("/api/registre/actes/numero/n-1998-000001").with(role("SERVICE"))).andExpect(jsonPath("$.id").value(id));
        mvc.perform(post("/api/registre/actes/" + id + "/validation").with(role("OFFICIER"))).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void erreursEtListe() throws Exception {
        mvc.perform(json(post("/api/registre/actes")).content("{\"typeActe\":\"NAISSANCE\"}").with(role("AGENT_REGISTRE")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.champs.nom").exists());
        mvc.perform(get("/api/registre/actes/numero/X-1").with(role("OFFICIER"))).andExpect(status().isNotFound());
        transcrire();
        mvc.perform(get("/api/registre/actes").param("nom", "diall").param("statut", "SAISI").with(role("OFFICIER")))
                .andExpect(jsonPath("$.contenu.length()").value(1)).andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/api/registre/actes/statistiques").with(role("OFFICIER")))
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.parType.NAISSANCE").value(1));
    }

    @Test
    void mentionsJournalEtCommunes() throws Exception {
        String id = transcrire();
        mvc.perform(post("/api/registre/actes/" + id + "/validation").with(role("OFFICIER"))).andExpect(status().isOk());
        mvc.perform(json(post("/api/registre/actes/" + id + "/mentions"))
                        .content("{\"type\":\"MARIAGE\",\"date\":\"2020-06-06\",\"reference\":\"Acte M-2020-000004\"}")
                        .with(role("CITOYEN"))).andExpect(status().isForbidden());
        mvc.perform(json(post("/api/registre/actes/" + id + "/mentions"))
                        .content("{\"type\":\"MARIAGE\",\"date\":\"2020-06-06\",\"reference\":\"Acte M-2020-000004\"}")
                        .with(role("AGENT_REGISTRE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentions[0].libelleType").value("Mention de mariage"))
                .andExpect(jsonPath("$.mentionsMarginales").value("Mention de mariage du 06/06/2020 (Acte M-2020-000004)"));
        mvc.perform(get("/api/registre/actes/" + id + "/journal").with(role("OFFICIER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("MENTION"))
                .andExpect(jsonPath("$[1].action").value("VALIDATION"))
                .andExpect(jsonPath("$[1].auteur").value("Awa Ndiaye <officier@ecivil.sn>"))
                .andExpect(jsonPath("$[2].action").value("TRANSCRIPTION"));
        mvc.perform(get("/api/registre/communes")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(66))
                .andExpect(jsonPath("$[?(@.nom == 'Touba-Mosquée')].region").value("Diourbel"));
        mvc.perform(get("/api/registre/types-mentions")).andExpect(status().isOk());
    }
}
