package sn.etatcivil.portail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import sn.etatcivil.portail.catalogue.TypeActe;
import sn.etatcivil.portail.catalogue.TypeDocumentRepository;
import sn.etatcivil.portail.demande.*;
import sn.etatcivil.portail.paiement.PayDunyaClient;
import sn.etatcivil.portail.paiement.PaiementRepository;
import sn.etatcivil.portail.paiement.PaiementService;
import sn.etatcivil.portail.paiement.StatutPaiement;
import sn.etatcivil.portail.registre.ActeRegistre;
import sn.etatcivil.portail.registre.RegistreClient;
import sn.etatcivil.portail.registre.ResultatRecherche;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.utilisateur.*;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ParcoursDemandeTest {

    @Autowired MockMvc mvc;
    @Autowired CompteService comptes;
    @Autowired DemandeService demandes;
    @Autowired PaiementService paiements;
    @Autowired PaiementRepository paiementRepository;
    @Autowired TypeDocumentRepository types;
    @MockBean RegistreClient registre;
    @MockBean PayDunyaClient paydunya;

    UtilisateurConnecte citoyen, delegue, officier;

    @BeforeEach
    void comptes() {
        citoyen = compte("fatou@example.sn", Role.CITOYEN);
        delegue = compte("delegue@ecivil.sn", Role.DELEGUE);
        officier = compte("officier@ecivil.sn", Role.OFFICIER);
        when(paydunya.estConfigure()).thenReturn(true);
        when(paydunya.masterKey()).thenReturn("cle-maitre-test");
    }

    UtilisateurConnecte compte(String email, Role role) {
        Inscription f = new Inscription();
        f.setEmail(email); f.setPrenom("Prénom"); f.setNom("Nom"); f.setMotDePasse("MotDePasse123"); f.setRole(role);
        return new UtilisateurConnecte(comptes.creerCompte(f));
    }

    Demande deposer() {
        FormulaireDemande f = new FormulaireDemande();
        f.setTypeDocumentId(types.findByActifTrueOrderByIdAsc().get(0).getId()); // extrait de naissance
        f.setMotifId(1L);
        f.setLienDemandeur(LienDemandeur.MOI_MEME);
        f.setFormat(FormatDocument.NUMERIQUE);
        f.setNom("Diallo"); f.setPrenom("Fatou"); f.setSexe("F");
        f.setDateNaissance(LocalDate.of(1998, 3, 14)); f.setLieuNaissance("Dakar");
        return demandes.creer(f, citoyen);
    }

    static ActeRegistre acte(String statut, TypeActe type) {
        return new ActeRegistre(42L, type, "N-1998-000042", 1998, statut, "Dakar Plateau", null, "1", "2",
                LocalDate.of(1998, 3, 14), "Dakar", "Diallo", "Fatou", "F", LocalDate.of(1998, 3, 14), "Dakar",
                "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null, null, null, null, null,
                "agent", LocalDateTime.now(), LocalDateTime.now());
    }

    void attendreStatut(Demande d, StatutDemande attendu) {
        long limite = System.currentTimeMillis() + 10_000;
        while (demandes.trouver(d.getId()).getStatut() != attendu) {
            if (System.currentTimeMillis() > limite) {
                fail("Statut " + attendu + " non atteint, statut actuel " + demandes.trouver(d.getId()).getStatut());
            }
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Test
    void parcoursCompletJusquALaSignature() {
        Demande d = deposer();
        assertThat(d.getReference()).matches("DEM-\\d{4}-000001");
        assertThat(d.getMontant()).isEqualTo(1000);

        demandes.validerPourPaiement(d.getId(), delegue);
        when(paydunya.creerFacture(eq(1000), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PayDunyaClient.Facture("tok_1", "https://paydunya.com/checkout/tok_1"));
        assertThat(paiements.initier(d.getId(), citoyen)).isEqualTo("https://paydunya.com/checkout/tok_1");

        // Tant que PayDunya ne confirme pas, la demande n'est pas payée
        when(paydunya.confirmer("tok_1")).thenReturn(new PayDunyaClient.EtatFacture("pending", BigDecimal.ZERO, null, null));
        paiements.confirmer("tok_1");
        assertThat(demandes.trouver(d.getId()).getStatut()).isEqualTo(StatutDemande.A_PAYER);

        when(registre.rechercher(any())).thenReturn(new ResultatRecherche(ResultatRecherche.Resultat.TROUVE,
                List.of(acte("VALIDE", TypeActe.NAISSANCE))));
        when(paydunya.confirmer("tok_1")).thenReturn(new PayDunyaClient.EtatFacture("completed",
                new BigDecimal("1000"), "https://paydunya.com/recu", d.getReference()));
        paiements.confirmer("tok_1");
        assertThat(paiementRepository.findByToken("tok_1").orElseThrow().getStatut()).isEqualTo(StatutPaiement.PAYE);

        // La recherche dans le registre démarre d'elle-même après le paiement
        attendreStatut(d, StatutDemande.ACTE_TROUVE);
        assertThat(demandes.trouver(d.getId()).getNumeroActe()).isEqualTo("N-1998-000042");

        when(registre.obtenir(42L)).thenReturn(acte("VALIDE", TypeActe.NAISSANCE));
        demandes.signer(d.getId(), officier);
        Demande signee = demandes.trouver(d.getId());
        assertThat(signee.getStatut()).isEqualTo(StatutDemande.SIGNEE);
        assertThat(signee.getCodeVerification()).matches("[A-Z2-9]{5}-[A-Z2-9]{5}");
        assertThat(signee.getEmpreinte()).hasSize(64);
        assertThat(signee.getContenuSigne()).contains("Mamadou").contains("N-1998-000042");
        assertThat(demandes.historique(d.getId())).extracting(HistoriqueStatut::getNouveauStatut).containsExactly(
                StatutDemande.SOUMISE, StatutDemande.A_PAYER, StatutDemande.PAYEE,
                StatutDemande.ACTE_TROUVE, StatutDemande.SIGNEE);
    }

    @Test
    void refuseUnMontantPayeDifferent() {
        Demande d = deposer();
        demandes.validerPourPaiement(d.getId(), delegue);
        when(paydunya.creerFacture(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PayDunyaClient.Facture("tok_2", "https://paydunya.com/checkout/tok_2"));
        paiements.initier(d.getId(), citoyen);
        when(paydunya.confirmer("tok_2")).thenReturn(new PayDunyaClient.EtatFacture("completed", new BigDecimal("200"), null, null));
        assertThatThrownBy(() -> paiements.confirmer("tok_2")).isInstanceOf(ActionRefuseeException.class);
        assertThat(demandes.trouver(d.getId()).getStatut()).isEqualTo(StatutDemande.A_PAYER);
    }

    @Test
    void notificationIpnAuthentifieeParEmpreinte() throws Exception {
        Demande d = deposer();
        demandes.validerPourPaiement(d.getId(), delegue);
        when(paydunya.creerFacture(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PayDunyaClient.Facture("tok_3", "https://paydunya.com/checkout/tok_3"));
        paiements.initier(d.getId(), citoyen);
        when(paydunya.confirmer("tok_3")).thenReturn(new PayDunyaClient.EtatFacture("completed", new BigDecimal("1000"), null, null));
        when(registre.rechercher(any())).thenReturn(new ResultatRecherche(ResultatRecherche.Resultat.NON_TROUVE, List.of()));

        mvc.perform(post("/paiements/paydunya/ipn").param("data[hash]", "faux").param("data[invoice][token]", "tok_3"))
                .andExpect(status().isBadRequest());
        assertThat(demandes.trouver(d.getId()).getStatut()).isEqualTo(StatutDemande.A_PAYER);

        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512")
                .digest("cle-maitre-test".getBytes(StandardCharsets.UTF_8)));
        mvc.perform(post("/paiements/paydunya/ipn").param("data[hash]", hash).param("data[invoice][token]", "tok_3"))
                .andExpect(status().isOk());
        attendreStatut(d, StatutDemande.ACTE_INTROUVABLE); // payée, puis recherchée automatiquement
    }

    @Test
    void transitionsInterditesEtActeNonValide() {
        Demande d = deposer();
        // Impossible de signer ou de marquer payée une demande qui vient d'être déposée
        assertThatThrownBy(() -> demandes.signer(d.getId(), officier)).isInstanceOf(ActionRefuseeException.class);
        demandes.enregistrerPaiement(d.getId(), "test");
        assertThat(demandes.trouver(d.getId()).getStatut()).isEqualTo(StatutDemande.SOUMISE);

        demandes.validerPourPaiement(d.getId(), delegue);
        when(paydunya.creerFacture(anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new PayDunyaClient.Facture("tok_4", "u"));
        paiements.initier(d.getId(), citoyen);
        when(paydunya.confirmer("tok_4")).thenReturn(new PayDunyaClient.EtatFacture("completed", new BigDecimal("1000"), null, null));
        when(registre.rechercher(any())).thenReturn(new ResultatRecherche(ResultatRecherche.Resultat.NON_TROUVE, List.of()));
        paiements.confirmer("tok_4");
        attendreStatut(d, StatutDemande.ACTE_INTROUVABLE);

        // Un acte non validé ou d'un autre type ne peut pas être rattaché
        when(registre.parNumero("N-1")).thenReturn(acte("SAISI", TypeActe.NAISSANCE));
        assertThatThrownBy(() -> demandes.rattacherParNumero(d.getId(), "N-1", officier))
                .hasMessageContaining("pas encore validé");
        when(registre.parNumero("M-1")).thenReturn(acte("VALIDE", TypeActe.MARIAGE));
        assertThatThrownBy(() -> demandes.rattacherParNumero(d.getId(), "M-1", officier))
                .hasMessageContaining("mariage");
    }

    @Test
    void controleDAccesParRole() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/verification/ABCDE-FGHJK")).andExpect(status().isOk());
        mvc.perform(get("/demandes")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/delegue").with(user(citoyen))).andExpect(status().isForbidden());
        mvc.perform(get("/admin").with(user(delegue))).andExpect(status().isForbidden());
        mvc.perform(get("/officier").with(user(delegue))).andExpect(status().isForbidden());
        mvc.perform(post("/admin/utilisateurs/1/role").param("role", "ADMIN").with(user(citoyen)).with(csrf()))
                .andExpect(status().isForbidden());
        // Sans jeton CSRF, aucune action n'est acceptée
        mvc.perform(post("/delegue/demandes/1/validation").with(user(delegue))).andExpect(status().isForbidden());
        mvc.perform(get("/delegue").with(user(delegue))).andExpect(status().isOk());
    }

    @Test
    void unCitoyenNeVoitQueSesDemandes() throws Exception {
        Demande d = deposer();
        UtilisateurConnecte autre = compte("autre@example.sn", Role.CITOYEN);
        mvc.perform(get("/demandes/" + d.getId()).with(user(citoyen))).andExpect(status().isOk());
        mvc.perform(get("/demandes/" + d.getId()).with(user(autre)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erreur", "Demande introuvable"));
        assertThatThrownBy(() -> paiements.initier(d.getId(), autre)).isInstanceOf(ActionRefuseeException.class);
    }

    @Test
    void inscriptionPubliqueToujoursCitoyen() throws Exception {
        mvc.perform(post("/inscription").with(csrf())
                        .param("prenom", "Awa").param("nom", "Ba").param("email", "awa@example.sn")
                        .param("motDePasse", "MotDePasse123").param("confirmation", "MotDePasse123")
                        .param("role", "ADMIN"))
                .andExpect(redirectedUrl("/connexion"));
        assertThat(comptes.tous()).filteredOn(u -> u.getEmail().equals("awa@example.sn"))
                .singleElement().extracting(Utilisateur::getRole).isEqualTo(Role.CITOYEN);
    }
}
