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

    /** Connexion avec un rôle de base : le jeton porte le rôle et ses permissions, comme ceux de Keycloak. */
    static org.springframework.test.web.servlet.request.RequestPostProcessor utilisateur(String role) {
        List<String> roles = sn.etatcivil.socle.tests.JetonsDeTest.roles(role);
        OidcIdToken jeton = new OidcIdToken("jeton", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("sub", "u-" + role, "email", role.toLowerCase() + "@example.sn", "given_name", "Awa",
                        "family_name", "Ndiaye", "realm_access", Map.of("roles", roles)));
        return oidcLogin().oidcUser(new DefaultOidcUser(roles.stream()
                .<org.springframework.security.core.GrantedAuthority>map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList(), jeton));
    }

    @Test
    void pagesPubliquesEtControleDesRoles() throws Exception {
        when(api.documents()).thenReturn(List.of(
                new Vues.TypeDocument(1L, "EXTRAIT_NAISSANCE", "Extrait d'acte de naissance",
                        "desc", "NAISSANCE", false, false, "EXTRAIT", null, 1000, 2, true),
                new Vues.TypeDocument(2L, "CERTIFICAT_CELIBAT", "Certificat de célibat",
                        "desc", "NAISSANCE", false, false, "CERTIFICAT_CELIBAT", null, 500, 1, true),
                new Vues.TypeDocument(3L, "TRANSCRIPTION_MARIAGE", "Transcription d'acte de mariage étranger",
                        "desc", "MARIAGE", true, true, "COPIE_INTEGRALE", "TRANSCRIPTION", 500, 1, true)));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString(">500<")));
        mvc.perform(get("/documents")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-famille=\"CERTIFICAT\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(">Transcription<")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("accent-certificat")));
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
                "ORDINAIRE", null, "Agent", t, t));
        mvc.perform(get("/back-office/demandes/" + id).with(utilisateur("OFFICIER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Validé par l&#39;officier")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Signer et délivrer")));
    }

    @Test
    void formulairesSAffichent() throws Exception {
        when(api.document(1L)).thenReturn(new Vues.TypeDocument(1L, "EXTRAIT_MARIAGE", "Extrait d'acte de mariage",
                "desc", "MARIAGE", true, false, "EXTRAIT", null, 1500, 3, true));
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

    @Test
    void formulairesDesNouveauxDocuments() throws Exception {
        when(api.motifs()).thenReturn(List.of(new Vues.Motif(1L, "Retraite", false), new Vues.Motif(9L, "Autre", true)));
        when(api.document(2L)).thenReturn(new Vues.TypeDocument(2L, "CERTIFICAT_VIE", "Certificat de vie",
                "desc", "NAISSANCE", false, false, "CERTIFICAT_VIE", null, 500, 1, true));
        mvc.perform(get("/demandes/nouvelle").param("type", "2").with(utilisateur("CITOYEN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Présentez-vous au centre")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Personne concernée par le certificat")));
        when(api.document(3L)).thenReturn(new Vues.TypeDocument(3L, "COPIE_RECONNAISSANCE", "Copie d'acte de reconnaissance",
                "desc", "RECONNAISSANCE", false, true, "COPIE_INTEGRALE", null, 500, 1, true));
        mvc.perform(get("/demandes/nouvelle").param("type", "3").with(utilisateur("CITOYEN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Enfant reconnu")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Date de la reconnaissance")));
        mvc.perform(get("/registre/actes/nouveau").param("type", "RECONNAISSANCE").with(utilisateur("AGENT_REGISTRE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"TRANSCRIPTION\"")));
    }

    @Test
    void laVerificationAfficheLeConstatEtLesEnfants() throws Exception {
        Instant t = Instant.now();
        java.time.LocalDate d = java.time.LocalDate.of(2000, 8, 12);
        Vues.Acte mariage = new Vues.Acte(java.util.UUID.randomUUID(), "MARIAGE", "M-2000-000001", 2000, "VALIDE",
                "Kaolack", null, null, null, d, "Kaolack", "Mbaye", "Babacar", "M", null, null, null, null, null, null,
                "Diouf", "Seynabou", null, null, null, null, null, null, null, null, "ORDINAIRE", null, "Agent", t, t);
        when(api.verifier("ABCDE-23456")).thenReturn(java.util.Optional.of(new Vues.Verification("Duplicata de livret de famille",
                mariage, new Vues.Complements("LIVRET_FAMILLE", null, null,
                        List.of(new Vues.Enfant("N-2002-000001", "Awa", "Mbaye", "F", d, "Kaolack"))),
                "Mariama Ndiaye", t, "ABCDE-23456", "abc")));
        mvc.perform(get("/verification/ABCDE-23456")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Awa Mbaye (N-2002-000001)")));
    }

    @Test
    void administrationDesHabilitations() throws Exception {
        Instant t = Instant.now();
        var permissions = List.of(
                new Vues.Permission("DEMANDES_DEPOSER", "Déposer des demandes", "desc", "avantage", "interdiction", "Citoyen"),
                new Vues.Permission("UTILISATEURS_GERER", "Gérer les utilisateurs", "desc", null, null, "Administration"));
        var profilResume = new Vues.ProfilResume("g1", "Administrateur");
        var roles = List.of(
                new Vues.Role("ADMIN", "Administrateur", null, java.util.Set.of("UTILISATEURS_GERER"), true, List.of(profilResume)),
                new Vues.Role("CITOYEN", "Citoyen", null, java.util.Set.of("DEMANDES_DEPOSER"), true, List.of()));
        var compte = new Vues.Compte("u1", "awa@ecivil.sn", "Awa", "Ndiaye", "Awa Ndiaye", true, t, List.of(profilResume));
        var operation = new Vues.Operation(t, "Adja Dione <admin@ecivil.sn>", "PROFILS_MODIFIES", "Profils du compte modifiés",
                "awa@ecivil.sn", "+ Administrateur");
        when(api.syntheseHabilitations()).thenReturn(new Vues.Synthese(12, 6, 5, 15, List.of(operation)));
        when(api.statistiquesDemandes()).thenReturn(Map.of("SOUMISE", 3L));
        when(api.permissions()).thenReturn(permissions);
        when(api.roles()).thenReturn(roles);
        when(api.role("ADMIN")).thenReturn(roles.get(0));
        var profil = new Vues.Profil("g1", "Administrateur", "Administration de la plateforme", java.util.Set.of("ADMIN"), false, 1,
                List.of(compte));
        when(api.profils()).thenReturn(List.of(profil));
        when(api.profil("g1")).thenReturn(profil);
        when(api.comptes(null, 0)).thenReturn(new Vues.Page<>(List.of(compte), 0, 20, 1, 1));
        when(api.compte("u1")).thenReturn(new Vues.FicheCompte(compte, List.of(roles.get(0)), List.of(permissions.get(1)),
                List.of(operation)));
        when(api.journal(null, 0)).thenReturn(new Vues.Page<>(List.of(operation), 0, 30, 1, 1));

        var contient = (java.util.function.Function<String, org.springframework.test.web.servlet.ResultMatcher>)
                x -> content().string(org.hamcrest.Matchers.containsString(x));
        mvc.perform(get("/admin").with(utilisateur("ADMIN"))).andExpect(status().isOk())
                .andExpect(contient.apply("Activité récente")).andExpect(contient.apply("Profils du compte modifiés"));
        mvc.perform(get("/admin/utilisateurs").with(utilisateur("ADMIN"))).andExpect(status().isOk())
                .andExpect(contient.apply("awa@ecivil.sn"));
        mvc.perform(get("/admin/utilisateurs/nouveau").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/utilisateurs/u1").with(utilisateur("ADMIN"))).andExpect(status().isOk())
                .andExpect(contient.apply("Gérer les utilisateurs")).andExpect(contient.apply("checked"));
        mvc.perform(get("/admin/profils").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/profils/g1").with(utilisateur("ADMIN"))).andExpect(status().isOk())
                .andExpect(contient.apply("Awa Ndiaye"));
        mvc.perform(get("/admin/profils/nouveau").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/roles").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/roles/ADMIN").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/roles/nouveau").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/permissions").with(utilisateur("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.matchesRegex(
                        "(?s).*value=\"ADMIN:UTILISATEURS_GERER\"[^>]*checked.*")))
                .andExpect(content().string(org.hamcrest.Matchers.matchesRegex(
                        "(?s).*value=\"ADMIN:DEMANDES_DEPOSER\"[^>]*aria-label=\"[^\"]*\">.*")));
        mvc.perform(get("/admin/permissions/DEMANDES_DEPOSER").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/journal").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        mvc.perform(get("/admin/documents").with(utilisateur("ADMIN"))).andExpect(status().isOk());
        when(api.referentielsCatalogue()).thenReturn(new Vues.ReferentielsCatalogue(
                List.of(new Vues.Libelle("EXTRAIT", "Extrait")), List.of(new Vues.Libelle("PARENTS", "Filiation")),
                List.of(new Vues.Libelle("NAISSANCE", "Naissance")), List.of()));
        when(api.diaspora()).thenReturn(new Vues.ReferentielDiaspora(List.of(new Vues.Pays(1L, "France", "FRA", "Europe", "Paris", "Europe")),
                List.of("Consulat"), List.of("Europe")));
        for (String page : List.of("/admin/documents/nouveau", "/admin/natures", "/admin/motifs", "/admin/missions")) {
            mvc.perform(get(page).with(utilisateur("ADMIN"))).andExpect(status().isOk());
        }
        mvc.perform(get("/admin/natures").with(utilisateur("DELEGUE"))).andExpect(status().isForbidden());

        // la matrice envoie, pour chaque rôle, les permissions cochées (un rôle tout décoché part vide)
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/admin/permissions/matrice")
                        .param("case", "ADMIN:UTILISATEURS_GERER", "ADMIN:DEMANDES_DEPOSER")
                        .with(utilisateur("ADMIN")).with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(redirectedUrl("/admin/permissions"));
        org.mockito.Mockito.verify(api).modifierMatrice(Map.of(
                "ADMIN", java.util.Set.of("UTILISATEURS_GERER", "DEMANDES_DEPOSER"), "CITOYEN", java.util.Set.of()));

        // chaque espace exige sa permission
        mvc.perform(get("/admin/utilisateurs").with(utilisateur("OFFICIER"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/roles").with(utilisateur("DELEGUE"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/journal").with(utilisateur("CITOYEN"))).andExpect(status().isForbidden());
        mvc.perform(get("/demandes").with(utilisateur("DELEGUE"))).andExpect(status().isForbidden());
        mvc.perform(get("/espace").with(utilisateur("ADMIN"))).andExpect(redirectedUrl("/admin"));
    }

    @Test
    void ecranDuChargeDeRenseignement() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID();
        Instant t = Instant.now();
        java.time.LocalDate naissance = java.time.LocalDate.of(1987, 2, 3);
        when(api.demande(id)).thenReturn(new Vues.Demande(id, "DEM-2026-000002", "ACTE_INTROUVABLE", "Acte non retrouvé", "",
                false, "u-1", "Awa Sy", "awa@example.sn", 1L, "Extrait d'acte de naissance", "NAISSANCE", false, 1000,
                "Dossier administratif", "Moi-même", "NUMERIQUE", null, null, "Sy", "Awa", "F", naissance, "Kaolack",
                null, null, "Ba", "Coumba", null, null, null, null, null, "Aucun acte validé", null, null,
                null, null, t, t, List.of()));
        when(api.paiements(id)).thenReturn(List.of());
        var registre = new Vues.RegistrePapier(5L, 9L, "Centre principal de Kaolack", "NAISSANCE", 1987, "GREFFE",
                "Exemplaire du greffe", null, "Greffe", "ABIME", "Abîmé", true, false, null,
                "Naissances 1987, Centre principal de Kaolack, exemplaire du greffe");
        when(api.registresPapier(null, "NAISSANCE", 1987)).thenReturn(List.of(registre));
        when(api.renseignement(id)).thenReturn(java.util.Optional.of(new Vues.Renseignement(id, "EN_COURS", "En cours", true,
                "u-AGENT_REGISTRE", "Awa Ndiaye", t, t, null, null, null, List.of(new Vues.Consultation(4L,
                "Naissances 1987, Centre principal de Kaolack, exemplaire du centre", "REGISTRE_INDISPONIBLE",
                "Registre manquant ou indisponible", null, "Détruit en 2009", "Awa Ndiaye (Agent du registre)", t)))));
        when(api.demandeur("u-1")).thenReturn(java.util.Optional.of(new Vues.ProfilPersonnel("Awa", "Sy", "awa@example.sn",
                "+221 77 000 00 00", "F", naissance, "Kaolack", "Commerçante", "Médina", "Kaolack", 2, "1018201883088",
                "RESIDENT", null, true, "fr")));
        mvc.perform(get("/back-office/demandes/" + id).with(utilisateur("AGENT_REGISTRE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1 018 2018 83088")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ne correspond pas au sexe déclaré")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Détruit en 2009")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Naissances 1987, Centre principal de Kaolack, exemplaire du greffe")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Retirer l&#39;urgence")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("acte non trouvé (la demande est rejetée)")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Prendre en charge"))));

        // aucun registre pour une autre année : la fiche invite à enregistrer les registres du centre
        when(api.registresPapier(null, "NAISSANCE", 1950)).thenReturn(List.of());
        mvc.perform(get("/back-office/demandes/" + id).param("annee", "1950").with(utilisateur("AGENT_REGISTRE")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Aucun registre enregistré pour ce type")));

        var centre = new Vues.CentreEtatCivil(9L, "Centre principal de Kaolack", "PRINCIPAL", "Centre principal", "Kaolack",
                null, null, null, true);
        when(api.centres(null, null)).thenReturn(List.of(centre));
        when(api.centres(null, "PRINCIPAL")).thenReturn(List.of(centre));
        when(api.centre(9L)).thenReturn(centre);
        when(api.registresPapier(9L, null, null)).thenReturn(List.of(registre));
        mvc.perform(get("/registre/centres").with(utilisateur("AGENT_REGISTRE"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nouveau centre")));
        mvc.perform(get("/registre/centres/9").with(utilisateur("AGENT_REGISTRE"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Exemplaire du greffe")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Abîmé")));
        mvc.perform(get("/registre/centres").with(utilisateur("DELEGUE"))).andExpect(status().isForbidden());
    }
}
