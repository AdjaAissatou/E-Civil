package sn.etatcivil.paiement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import sn.etatcivil.paiement.application.port.entree.SuivreDemandes;
import sn.etatcivil.paiement.application.port.sortie.PasserellePaiement;
import sn.etatcivil.paiement.domaine.EtatFacture;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaiementApiTest {

    @Autowired MockMvc mvc;
    @Autowired SuivreDemandes suivi;
    @Autowired JdbcTemplate jdbc;
    @MockBean PasserellePaiement passerelle;

    static RequestPostProcessor citoyen(String id) {
        return jwt().jwt(j -> j.subject(id).claim("realm_access", Map.of("roles", List.of("CITOYEN", "offline_access"))))
                .authorities(new SimpleGrantedAuthority("ROLE_CITOYEN"));
    }

    @Test
    void paiementPuisNotificationIpnPublieUnEvenement() throws Exception {
        UUID id = UUID.randomUUID();
        suivi.statutChange(id, "DEM-2026-000001", "fatou", "Extrait", 1000, "A_PAYER");
        when(passerelle.configuree()).thenReturn(true);
        when(passerelle.creerFacture(eq(1000), anyString(), eq("DEM-2026-000001"), any()))
                .thenReturn(new PasserellePaiement.Facture("tok", "https://paydunya.com/checkout/tok"));

        mvc.perform(post("/api/paiements/demandes/" + id).with(citoyen("moussa"))).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/paiements/demandes/" + id)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/paiements/demandes/" + id).with(citoyen("fatou")))
                .andExpect(jsonPath("$.urlPaiement").value("https://paydunya.com/checkout/tok"));

        when(passerelle.consulter("tok")).thenReturn(new EtatFacture(EtatFacture.Etat.PAYEE, 1000, "recu"));
        when(passerelle.notificationAuthentique("bonne")).thenReturn(true);
        mvc.perform(post("/api/paiements/paydunya/ipn").param("data[hash]", "mauvaise").param("data[invoice][token]", "tok"))
                .andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM evenement_sortant", Integer.class)).isZero();
        mvc.perform(post("/api/paiements/paydunya/ipn").param("data[hash]", "bonne").param("data[invoice][token]", "tok"))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForList("SELECT routage FROM evenement_sortant", String.class)).containsExactly("paiement.confirme");

        mvc.perform(get("/api/paiements/demandes/" + id).with(citoyen("fatou"))).andExpect(jsonPath("$[0].statut").value("PAYE"));
        mvc.perform(get("/api/paiements/demandes/" + id).with(citoyen("moussa"))).andExpect(jsonPath("$.length()").value(0));
    }
}
