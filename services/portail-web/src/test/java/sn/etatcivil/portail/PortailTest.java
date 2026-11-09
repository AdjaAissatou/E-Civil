package sn.etatcivil.portail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import sn.etatcivil.portail.clients.Api;
import sn.etatcivil.portail.clients.Vues;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Le portail démarre, protège ses espaces par rôle et affiche ses pages (microservices simulés). */
@SpringBootTest(properties = {"PORTAIL_CLIENT_SECRET=test"})
@AutoConfigureMockMvc
class PortailTest {

    @Autowired MockMvc mvc;
    @MockBean Api api;

    static org.springframework.test.web.servlet.request.RequestPostProcessor utilisateur(String role) {
        OidcIdToken jeton = new OidcIdToken("jeton", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("sub", "u-" + role, "email", role.toLowerCase() + "@example.sn", "given_name", "Awa",
                        "family_name", "Ndiaye", "realm_access", Map.of("roles", List.of(role))));
        return oidcLogin().oidcUser(new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_" + role)), jeton));
    }

    @Test
    void pagesPubliquesEtControleDesRoles() throws Exception {
        when(api.documents()).thenReturn(List.of(new Vues.TypeDocument(1L, "EXTRAIT_NAISSANCE", "Extrait d'acte de naissance",
                "desc", "NAISSANCE", false, false, 1000, 2, true)));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("À partir de 1.000 FCFA")));
        mvc.perform(get("/documents")).andExpect(status().isOk());
        mvc.perform(get("/verification")).andExpect(status().isOk());
        mvc.perform(get("/demandes")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/keycloak"));
        mvc.perform(get("/inscription")).andExpect(redirectedUrl("/oauth2/authorization/keycloak?inscription"));

        when(api.mesDemandes()).thenReturn(List.of());
        mvc.perform(get("/demandes").with(utilisateur("CITOYEN"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Awa Ndiaye")));
        mvc.perform(get("/delegue").with(utilisateur("CITOYEN"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin").with(utilisateur("OFFICIER"))).andExpect(status().isForbidden());
        mvc.perform(get("/espace").with(utilisateur("OFFICIER"))).andExpect(redirectedUrl("/officier"));
    }

    @Test
    void formulairesSAffichent() throws Exception {
        when(api.document(1L)).thenReturn(new Vues.TypeDocument(1L, "EXTRAIT_MARIAGE", "Extrait d'acte de mariage",
                "desc", "MARIAGE", true, false, 1500, 3, true));
        when(api.motifs()).thenReturn(List.of(new Vues.Motif(1L, "Mariage", false), new Vues.Motif(9L, "Autre", true)));
        mvc.perform(get("/demandes/nouvelle").param("type", "1").with(utilisateur("CITOYEN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Second conjoint")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"Ndiaye\"")));
        for (String type : List.of("NAISSANCE", "MARIAGE", "DECES")) {
            mvc.perform(get("/registre/actes/nouveau").param("type", type).with(utilisateur("AGENT_REGISTRE")))
                    .andExpect(status().isOk());
        }
    }
}
