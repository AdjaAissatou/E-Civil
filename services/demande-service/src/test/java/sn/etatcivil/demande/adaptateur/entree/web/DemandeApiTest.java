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

import static org.assertj.core.api.Assertions.assertThat;
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
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @MockBean RegistreCivil registre;
    @MockBean ServiceDocuments documents;

    static RequestPostProcessor utilisateur(String id, String role) {
        return jwt().jwt(j -> j.subject(id).claim("email", id + "@example.sn").claim("given_name", "Prénom")
                        .claim("family_name", id).claim("realm_access", Map.of("roles", sn.etatcivil.socle.tests.JetonsDeTest.roles(role))))
                .authorities(sn.etatcivil.socle.tests.JetonsDeTest.roles(role).stream().<org.springframework.security.core.GrantedAuthority>map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r)).toList());
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
        mvc.perform(get("/api/catalogue/documents")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(46))
                .andExpect(jsonPath("$[?(@.code == 'COPIE_NAISSANCE')].modele").value("COPIE_INTEGRALE"))
                .andExpect(jsonPath("$[?(@.code == 'TRANSCRIPTION_MARIAGE')].origine").value("TRANSCRIPTION"))
                .andExpect(jsonPath("$[?(@.code == 'CERTIFICAT_CELIBAT')].typeActe").value("NAISSANCE"))
                .andExpect(jsonPath("$[?(@.code == 'COPIE_RECONNAISSANCE')].typeActe").value("RECONNAISSANCE"));
        mvc.perform(get("/api/catalogue/motifs")).andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[10].autre").value(true));
        mvc.perform(get("/api/demandes/mes-demandes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/demandes").with(FATOU)).andExpect(status().isForbidden());
        mvc.perform(put("/api/catalogue/documents/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tarif\":1500,\"delaiJours\":2,\"actif\":true}").with(DELEGUE)).andExpect(status().isForbidden());
        mvc.perform(put("/api/catalogue/documents/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tarif\":1500,\"delaiJours\":2,\"actif\":true}").with(utilisateur("admin", "ADMIN")))
                .andExpect(jsonPath("$.tarif").value(1500));
    }

    @Test
    void naturesVarietesEtAdministrationDuCatalogue() throws Exception {
        var admin = utilisateur("admin", "ADMIN");
        mvc.perform(get("/api/catalogue/natures")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(15))
                .andExpect(jsonPath("$[0].code").value("NAISSANCE"));
        String catalogue = mvc.perform(get("/api/catalogue/documents")).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        Integer original = JsonPath.<List<Integer>>read(catalogue, "$[?(@.code == 'ACTE_NAISSANCE')].id").get(0);
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'COPIE_NAISSANCE')].copieDeId")).containsExactly(original);
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'ACTE_NAISSANCE')].estOriginal")).containsExactly(true);
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'EXTRAIT_NAISSANCE')].dureeValiditeJours")).containsExactly(90);
        // un document administratif vaut 3 mois ; le livret de famille n'expire pas
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.modele != 'LIVRET_FAMILLE')].dureeValiditeJours")).containsOnly(90);
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'LIVRET_FAMILLE')].dureeValiditeJours")).containsOnly((Object) null);
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'CERTIFICAT_CELIBAT')].libelleNature")).containsExactly("Célibat");
        assertThat(JsonPath.<List<Object>>read(catalogue, "$[?(@.code == 'CERTIFICAT_COUTUME')].exigeActe")).containsExactly(false);
        Integer domicile = JsonPath.<List<Integer>>read(catalogue, "$[?(@.code == 'CERTIFICAT_DOMICILE_ELECTIONS')].id").get(0);
        mvc.perform(get("/api/catalogue/motifs").param("document", domicile.toString()))
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[?(@.libelle == 'Inscription sur les listes électorales')].piecesRequises").exists());

        String nature = mvc.perform(post("/api/catalogue/natures").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"adoption\",\"libelle\":\"Adoption\",\"ordre\":160,\"active\":true}").with(admin))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("ADOPTION"))
                .andReturn().getResponse().getContentAsString();
        Integer natureId = JsonPath.read(nature, "$.id");
        String variete = """
                {"code":"copie adoption","libelle":"Copie de jugement d'adoption","natureId":%d,"typeActe":"NAISSANCE",
                 "modele":"%s","champs":["PARENTS","PRECISIONS"],"tarif":1000,"delaiJours":3,"dureeValiditeJours":90,
                 "piecesAFournir":"Pièce d'identité","actif":true}""";
        mvc.perform(post("/api/catalogue/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(variete.formatted(natureId, "LIVRET_FAMILLE")).with(admin))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("mariage")));
        mvc.perform(post("/api/catalogue/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(variete.formatted(natureId, "COPIE_INTEGRALE")).with(DELEGUE))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/catalogue/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(variete.formatted(natureId, "COPIE_INTEGRALE")).with(admin))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("COPIE_ADOPTION"))
                .andExpect(jsonPath("$.libelleNature").value("Adoption"))
                .andExpect(jsonPath("$.champs.length()").value(2));
    }

    @Test
    void referentielDeLaDiaspora() throws Exception {
        mvc.perform(get("/api/catalogue/diaspora")).andExpect(status().isOk())
                .andExpect(jsonPath("$.pays.length()").value(14))
                .andExpect(jsonPath("$.typesMission.length()").value(4))
                .andExpect(jsonPath("$.zones.length()").value(8));
        String referentiel = mvc.perform(get("/api/catalogue/diaspora")).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        Integer belgique = JsonPath.<List<Integer>>read(referentiel, "$.pays[?(@.codeIso == 'BEL')].id").get(0);
        mvc.perform(get("/api/catalogue/diaspora/missions").param("pays", belgique.toString()))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nom").value("Ambassade du Sénégal en France"));
        mvc.perform(get("/api/catalogue/diaspora/missions")).andExpect(jsonPath("$.length()").value(5));
        mvc.perform(post("/api/catalogue/diaspora/pays").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Maroc\",\"codeIso\":\"mar\",\"continent\":\"Afrique\",\"capitale\":\"Rabat\",\"zone\":\"Afrique du Nord\"}")
                        .with(utilisateur("admin", "ADMIN")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codeIso").value("MAR"));
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
        when(registre.rechercher(any(), any(), any())).thenThrow(
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

    @Test
    void lesPreferencesDeNotificationAccompagnentLesEvenements() throws Exception {
        mvc.perform(put("/api/demandes/preferences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"notifications\":false}").with(FATOU)).andExpect(status().isOk());
        deposer();
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM evenement_sortant WHERE contenu LIKE '%\"notifier\":false%'", Long.class)).isEqualTo(1L);
    }

    @Autowired sn.etatcivil.demande.application.port.entree.ConsulterPiecesIdentite piecesIdentite;

    static byte[] photo(int largeur, int hauteur) throws Exception {
        var image = new java.awt.image.BufferedImage(largeur, hauteur, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var sortie = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", sortie);
        return sortie.toByteArray();
    }

    @Test
    void uneCopieLitteraleExigeLaPieceDIdentiteEtLeSelfie() throws Exception {
        String depot = DEPOT.replace("\"typeDocumentId\":1", "\"typeDocumentId\":2");
        mvc.perform(post("/api/demandes").contentType(MediaType.APPLICATION_JSON).content(depot).with(FATOU))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("pièce d'identité")));
        var demande = new org.springframework.mock.web.MockMultipartFile("demande", "", "application/json", depot.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        var recto = new org.springframework.mock.web.MockMultipartFile("recto", "recto.png", "image/png", photo(800, 500));
        var verso = new org.springframework.mock.web.MockMultipartFile("verso", "verso.png", "image/png", photo(800, 500));
        var faux = new org.springframework.mock.web.MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "<html>".getBytes());
        mvc.perform(multipart("/api/demandes").file(demande).file(recto).file(verso).file(faux).param("typePiece", "CNI").with(FATOU))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Selfie : le fichier n'est pas une photo"));
        var minuscule = new org.springframework.mock.web.MockMultipartFile("selfie", "selfie.png", "image/png", photo(100, 80));
        mvc.perform(multipart("/api/demandes").file(demande).file(recto).file(verso).file(minuscule).param("typePiece", "CNI").with(FATOU))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("trop petite")));
        var selfie = new org.springframework.mock.web.MockMultipartFile("selfie", "selfie.png", "image/png", photo(640, 480));
        String rep = mvc.perform(multipart("/api/demandes").file(demande).file(recto).file(verso).file(selfie).param("typePiece", "CNI").with(FATOU))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.verificationIdentite").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(rep, "$.id");

        String pieces = mvc.perform(get("/api/demandes/" + id + "/pieces").with(FATOU)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)).andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/demandes/" + id + "/pieces").with(MOUSSA)).andExpect(status().isNotFound());
        String selfieId = JsonPath.<List<String>>read(pieces, "$[?(@.nature == 'SELFIE')].id").get(0);
        // la photo a été réencodée en JPEG (métadonnées retirées) ; seuls les agents qui vérifient l'identité la voient
        byte[] jpeg = mvc.perform(get("/api/demandes/" + id + "/pieces/" + selfieId).with(DELEGUE))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "private, no-store"))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(jpeg[0] & 0xFF).isEqualTo(0xFF);
        assertThat(jpeg[1] & 0xFF).isEqualTo(0xD8);
        mvc.perform(get("/api/demandes/" + id + "/pieces/" + selfieId).with(AGENT)).andExpect(status().isForbidden());
        mvc.perform(get("/api/demandes/" + id + "/pieces/" + selfieId).with(FATOU)).andExpect(status().isForbidden());

        mvc.perform(post("/api/demandes/" + id + "/validation").with(DELEGUE)).andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/demandes/" + id + "/validation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identiteVerifiee\":true}").with(DELEGUE)).andExpect(status().isOk());

        // 30 jours après la clôture, les photos sont effacées ; il en reste la trace
        jdbc.update("UPDATE demande SET statut = 'ANNULEE', date_modification = DATEADD('DAY', -31, CURRENT_TIMESTAMP) WHERE id = ?",
                java.util.UUID.fromString(id));
        assertThat(piecesIdentite.purger()).isEqualTo(3);
        mvc.perform(get("/api/demandes/" + id + "/pieces/" + selfieId).with(DELEGUE)).andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/demandes/" + id + "/pieces").with(FATOU)).andExpect(jsonPath("$[0].datePurge").isNotEmpty());
    }
}
