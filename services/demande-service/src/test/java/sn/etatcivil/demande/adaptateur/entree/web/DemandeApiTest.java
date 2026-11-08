package sn.etatcivil.demande.adaptateur.entree.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import sn.etatcivil.demande.application.port.sortie.RegistreCivil;
import sn.etatcivil.demande.application.port.sortie.ServiceDocuments;
import sn.etatcivil.demande.domaine.Fixtures;
import sn.etatcivil.demande.domaine.TypeActe;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** API REST avec les vrais adaptateurs (JPA, outbox) ; seuls les services distants sont simulés. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DemandeApiTest {

    @Autowired MockMvc mvc;
    @MockBean RegistreCivil registre;
    @MockBean ServiceDocuments documents;

    static RequestPostProcessor utilisateur(String id, String role) {
        return jwt().jwt(j -> j.subject(id).claim("email", id + "@example.sn").claim("given_name", "Prénom")
                        .claim("family_name", id).claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    static final RequestPostProcessor FATOU = utilisateur("fatou", "CITOYEN");
    static final RequestPostProcessor MOUSSA = utilisateur("moussa", "CITOYEN");
    static final RequestPostProcessor DELEGUE = utilisateur("awa", "DELEGUE");
    static final RequestPostProcessor AGENT = utilisateur("ibrahima", "AGENT_REGISTRE");
    static final RequestPostProcessor OFFICIER = utilisateur("mariama", "OFFICIER");

    static final String DEPOT = """
            {"typeDocumentId":1,"motifId":3,"lien":"MOI_MEME","format":"NUMERIQUE","nom":"Diallo","prenom":"Fatou",
             "sexe":"F","dateNaissance":"1998-03-14","lieuNaissance":"Dakar","nomMere":"Sow","prenomMere":"Aminata"}
            """;

    String deposer() throws Exception {
        String rep = mvc.perform(post("/api/demandes").contentType(MediaType.APPLICATION_JSON).content(DEPOT).with(FATOU))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.matchesPattern("DEM-\\d{4}-000001")))
                .andExpect(jsonPath("$.montant").value(1000))
                .andExpect(jsonPath("$.statut").value("SOUMISE"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(rep, "$.id");
    }

    @Test
    void catalogueEtSecurite() throws Exception {
        mvc.perform(get("/api/catalogue/documents")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/catalogue/motifs")).andExpect(jsonPath("$[8].autre").value(true));
        mvc.perform(get("/api/demandes/mes-demandes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/demandes").with(FATOU)).andExpect(status().isForbidden());
        mvc.perform(put("/api/catalogue/documents/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tarif\":1500,\"delaiJours\":2,\"actif\":true}").with(DELEGUE)).andExpect(status().isForbidden());
        mvc.perform(put("/api/catalogue/documents/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarif\":1500,\"delaiJours\":2,\"actif\":true}").with(utilisateur("admin", "ADMIN")))
                .andExpect(jsonPath("$.tarif").value(1500));
    }

    @Test
    void parcoursEtCloisonnement() throws Exception {
        String id = deposer();
        mvc.perform(get("/api/demandes/" + id).with(MOUSSA)).andExpect(status().isNotFound());
        mvc.perform(get("/api/demandes/mes-demandes").with(MOUSSA)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/demandes/mes-demandes").with(FATOU)).andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(post("/api/demandes/" + id + "/validation").with(AGENT)).andExpect(status().isForbidden());
        mvc.perform(post("/api/demandes/" + id + "/validation").with(DELEGUE)).andExpect(status().isOk());
        mvc.perform(post("/api/demandes/" + id + "/validation").with(DELEGUE)).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Impossible de passer une demande « À payer » à « À payer »"));
        mvc.perform(get("/api/demandes").param("statut", "A_PAYER").with(DELEGUE))
                .andExpect(jsonPath("$.total").value(1));
        mvc.perform(post("/api/demandes/" + id + "/rejet").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motif\":\"Pièces manquantes\"}").with(AGENT))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/demandes/" + id + "/rejet").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motif\":\"Pièces manquantes\"}").with(DELEGUE))
                .andExpect(status().isOk());
        mvc.perform(get("/api/demandes/" + id).with(FATOU))
                .andExpect(jsonPath("$.statut").value("REJETEE"))
                .andExpect(jsonPath("$.motifRejet").value("Pièces manquantes"))
                .andExpect(jsonPath("$.historique.length()").value(3))
                .andExpect(jsonPath("$.historique[1].auteur").value("Prénom awa (Délégué)"));
    }

    @Test
    void registreIndisponibleDonne503() throws Exception {
        String id = deposer();
        mvc.perform(post("/api/demandes/" + id + "/validation").with(DELEGUE));
        // la demande n'est pas payée : la recherche est refusée avant tout appel au registre
        mvc.perform(post("/api/demandes/" + id + "/recherche").with(AGENT)).andExpect(status().isUnprocessableEntity());
        when(registre.rechercher(any(), any())).thenThrow(
                new sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException("panne", null));
        when(registre.parNumero(any())).thenReturn(java.util.Optional.of(Fixtures.acte(true, TypeActe.NAISSANCE)));
        mvc.perform(post("/api/demandes/" + id + "/signature").with(OFFICIER)).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/demandes/" + id + "/signature").with(FATOU)).andExpect(status().isForbidden());
    }

    @Test
    void validationDesDonnees() throws Exception {
        mvc.perform(post("/api/demandes").contentType(MediaType.APPLICATION_JSON).content("{\"typeDocumentId\":1}").with(FATOU))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.champs.nom").exists());
        mvc.perform(post("/api/demandes").contentType(MediaType.APPLICATION_JSON)
                        .content(DEPOT.replace("\"lieuNaissance\":\"Dakar\",", "")).with(FATOU))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("La date et le lieu de naissance sont obligatoires"));
    }
}
