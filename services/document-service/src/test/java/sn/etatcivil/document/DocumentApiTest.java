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
                 "copieIntegrale":false,"acteId":"%s","format":"NUMERIQUE","signataire":"Mariama Sow"}
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
}
