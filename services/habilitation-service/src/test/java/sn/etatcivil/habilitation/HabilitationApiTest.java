package sn.etatcivil.habilitation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import sn.etatcivil.habilitation.application.AnnuaireEnMemoire;
import sn.etatcivil.habilitation.application.port.entree.InitialiserHabilitations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** API REST avec un annuaire en mémoire et un vrai journal (H2) : contrôle des permissions et réponses JSON. */
@SpringBootTest
@AutoConfigureMockMvc
class HabilitationApiTest {

    @TestConfiguration
    static class Annuaire {
        @Bean @Primary
        AnnuaireEnMemoire annuaireEnMemoire() {
            AnnuaireEnMemoire a = new AnnuaireEnMemoire();
            a.rolesBruts.addAll(Set.of("CITOYEN", "DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN"));
            a.ajouterCompte("admin@ecivil.sn", true, "ADMIN");
            return a;
        }
    }

    @Autowired MockMvc mvc;
    @Autowired AnnuaireEnMemoire annuaire;
    @Autowired InitialiserHabilitations initialisation;

    static RequestPostProcessor avec(String id, String... permissions) {
        return jwt().jwt(j -> j.subject(id).claim("email", id + "@ecivil.sn").claim("given_name", "Adja").claim("family_name", "Dione")
                        .claim("realm_access", Map.of("roles", List.of(permissions))))
                .authorities(Arrays.stream(permissions).<org.springframework.security.core.GrantedAuthority>map(p -> new SimpleGrantedAuthority("ROLE_" + p)).toList());
    }

    @Test
    void permissionsEtParcoursDAdministration() throws Exception {
        initialisation.initialiser();
        String admin = annuaire.utilisateursAvecRoleDirect("default-roles-ecivil").get(0);
        RequestPostProcessor administrateur = avec(admin, "UTILISATEURS_GERER", "HABILITATIONS_GERER", "JOURNAL_CONSULTER");

        mvc.perform(get("/api/habilitations/utilisateurs")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/habilitations/utilisateurs").with(avec("u", "DOCUMENTS_SIGNER"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/habilitations/synthese").with(avec("u", "STATISTIQUES_CONSULTER"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").value(15)).andExpect(jsonPath("$.profils").value(7));

        String chef = annuaire.idProfil("Chef de centre");
        mvc.perform(post("/api/habilitations/utilisateurs").with(administrateur).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mariama@ecivil.sn\",\"prenom\":\"Mariama\",\"nom\":\"Ndiaye\",\"profils\":[\"" + chef
                                + "\"],\"motDePasse\":\"Provisoire2026\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").exists());
        mvc.perform(get("/api/habilitations/utilisateurs").with(administrateur))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.contenu[1].profils[0].nom").value("Chef de centre"));

        // règle de sûreté : réponse 422 avec un message lisible
        mvc.perform(post("/api/habilitations/utilisateurs/" + admin + "/activation").with(administrateur)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"actif\":false}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Vous ne pouvez pas désactiver votre propre compte"));

        mvc.perform(get("/api/habilitations/roles").with(administrateur))
                .andExpect(jsonPath("$[?(@.code == 'OFFICIER')].profils[*].nom").value(org.hamcrest.Matchers.hasItem("Chef de centre")));
        mvc.perform(get("/api/habilitations/journal").with(administrateur))
                .andExpect(jsonPath("$.contenu[0].type").value("UTILISATEUR_CREE"))
                .andExpect(jsonPath("$.contenu[0].auteur").value("Adja Dione <" + admin + "@ecivil.sn>"))
                .andExpect(jsonPath("$.contenu[1].type").value("INITIALISATION"));
        mvc.perform(get("/api/habilitations/journal").with(avec("u", "UTILISATEURS_GERER"))).andExpect(status().isForbidden());
    }

    @Test
    void lesAgentsVoientLIdentiteDuDemandeur() throws Exception {
        String citoyen = annuaire.ajouterCompte("fatou@example.sn", true);
        mvc.perform(get("/api/habilitations/demandeurs/" + citoyen).with(avec("u", "DEMANDES_DEPOSER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/habilitations/demandeurs/" + citoyen).with(avec("u", "DEMANDES_CONSULTER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fatou@example.sn"))
                .andExpect(jsonPath("$.notifications").doesNotExist());
    }
}
