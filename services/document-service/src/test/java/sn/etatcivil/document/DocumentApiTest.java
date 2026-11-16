package sn.etatcivil.document;

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
import sn.etatcivil.document.application.port.sortie.RegistreActes;
import sn.etatcivil.document.domaine.DocumentDelivreTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentApiTest {

    @Autowired MockMvc mvc;
    @MockBean RegistreActes registre;

    static RequestPostProcessor qui(String id, String role) {
        return jwt().jwt(j -> j.subject(id).claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void delivranceIdempotentePdfEtVerification() throws Exception {
        when(registre.contenu(any())).thenReturn(DocumentDelivreTest.acte("VALIDE"));
        UUID demande = UUID.randomUUID();
        String commande = """
                {"demandeId":"%s","reference":"DEM-2026-000001","demandeurId":"fatou","libelleDocument":"Extrait d'acte de naissance",
                 "modele":"EXTRAIT","acteId":"%s","format":"NUMERIQUE","signataire":"Mariama Sow"}
                """.formatted(demande, UUID.randomUUID());

        mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(commande).with(qui("fatou", "CITOYEN")))
                .andExpect(status().isForbidden());
        String code = com.jayway.jsonpath.JsonPath.read(mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON)
                .content(commande).with(qui("demande-service", "SERVICE"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.codeVerification");
        mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(commande).with(qui("demande-service", "SERVICE")))
                .andExpect(jsonPath("$.codeVerification").value(code));
        verify(registre, times(1)).contenu(any()); // le second appel n'a rien refait

        byte[] pdf = mvc.perform(get("/api/documents/demandes/" + demande + "/pdf").with(qui("fatou", "CITOYEN")))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        mvc.perform(get("/api/documents/demandes/" + demande + "/pdf").with(qui("moussa", "CITOYEN"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/documents/demandes/" + demande + "/pdf").with(qui("off", "OFFICIER"))).andExpect(status().isOk());

        mvc.perform(get("/api/verification/" + code.toLowerCase())).andExpect(status().isOk())
                .andExpect(jsonPath("$.acte.numeroActe").value("N-1998-000001"))
                .andExpect(jsonPath("$.signataire").value("Mariama Sow"));
        mvc.perform(get("/api/verification/FAUX0-CODE0")).andExpect(status().isNotFound());
    }

    String delivrer(String modele, String libelle, String motif, String constat) throws Exception {
        String commande = """
                {"demandeId":"%s","reference":"DEM-2026-000002","demandeurId":"fatou","libelleDocument":"%s",
                 "modele":"%s","acteId":"%s","format":"NUMERIQUE","signataire":"Mariama Sow","motif":%s,"constat":%s}
                """.formatted(UUID.randomUUID(), libelle, modele, UUID.randomUUID(),
                motif == null ? "null" : "\"" + motif + "\"", constat == null ? "null" : "\"" + constat + "\"");
        return com.jayway.jsonpath.JsonPath.read(mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON)
                .content(commande).with(qui("demande-service", "SERVICE"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.codeVerification");
    }

    @Test
    void certificatEtLivretDeFamille() throws Exception {
        when(registre.contenu(any())).thenReturn(DocumentDelivreTest.acte("VALIDE"));
        String certificat = delivrer("CERTIFICAT_CELIBAT", "Certificat de célibat", "Mariage",
                "Aucun acte de mariage n'est inscrit au registre national au nom de cette personne.");
        mvc.perform(get("/api/verification/" + certificat)).andExpect(jsonPath("$.complements.modele").value("CERTIFICAT_CELIBAT"))
                .andExpect(jsonPath("$.complements.constat").value(org.hamcrest.Matchers.startsWith("Aucun acte de mariage")));

        when(registre.contenu(any())).thenReturn(DocumentDelivreTest.mariage());
        when(registre.enfants(any())).thenReturn(List.of(
                new sn.etatcivil.document.domaine.Complements.Enfant("N-2002-000001", "Awa", "Mbaye", "F",
                        java.time.LocalDate.of(2002, 5, 30), "Kaolack")));
        String livret = delivrer("LIVRET_FAMILLE", "Duplicata de livret de famille", null, null);
        mvc.perform(get("/api/verification/" + livret)).andExpect(jsonPath("$.complements.enfants[0].prenom").value("Awa"));
    }
}
