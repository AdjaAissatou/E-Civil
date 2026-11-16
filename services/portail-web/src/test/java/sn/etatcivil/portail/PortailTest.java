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
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString(">1 000<")));
        mvc.perform(get("/documents")).andExpect(status().isOk());
        mvc.perform(get("/verification")).andExpect(status().isOk());
        for (String ressource : List.of("/css/app.css", "/js/app.js", "/fonts/jakarta.woff2", "/images/mairie-hall-portrait.webp")) {
            mvc.perform(get(ressource)).andExpect(status().isOk());
        }
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
    void ficheDeControleDeLOfficierSAffiche() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID(), acteId = java.util.UUID.randomUUID();
        Instant t = Instant.now();
        java.time.LocalDate naissance = java.time.LocalDate.of(1995, 7, 22);
        when(api.demande(id)).thenReturn(new Vues.Demande(id, "DEM-2026-000001", "ACTE_TROUVE", "Acte retrouvé", "",
                false, "u-1", "Ousmane Sarr", "ousmane@example.sn", 1L, "Extrait d'acte de naissance", "NAISSANCE", false, 1000,
                "Dossier administratif", "Moi-même", "NUMERIQUE", null, null, "Sarr", "Ousmane", "M", naissance, "Thiès",
                "Sarr", "Abdoulaye", "Faye", "Khady", null, null, null, acteId, "N-1995-000001", null, null, null,
                null, null, t, t, List.of()));
        when(api.paiements(id)).thenReturn(List.of());
        when(api.acte(acteId)).thenReturn(new Vues.Acte(acteId, "NAISSANCE", "N-1995-000001", 1995, "VALIDE",
                "Centre principal de Thiès", "Thiès", "4", "112", naissance, "Thiès", "Sarr", "Ousmane", "M", naissance, "Thiès",
                "Sarr", "Abdoulaye", "Faye", "Khady", null, null, null, null, null, null, null, null, null, null,
                "Agent", t, t));
        mvc.perform(get("/back-office/demandes/" + id).with(utilisateur("OFFICIER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Validé par l&#39;officier")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Signer et délivrer")));
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
