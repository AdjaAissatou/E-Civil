package sn.etatcivil.demande.adaptateur.entree.messagerie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import sn.etatcivil.demande.application.port.entree.ConsulterDemandes;
import sn.etatcivil.demande.application.port.entree.GererMesDemandes;
import sn.etatcivil.demande.application.port.entree.InstruireDemandes;
import sn.etatcivil.demande.application.port.entree.DemandeADeposer;
import sn.etatcivil.demande.application.port.sortie.RegistreCivil;
import sn.etatcivil.demande.application.port.sortie.ServiceDocuments;
import sn.etatcivil.demande.domaine.*;
import sn.etatcivil.evenements.PaiementConfirme;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.EnveloppeEvenement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Réception d'un paiement confirmé : idempotence, enchaînement avec la recherche, publication via l'outbox. */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EcouteurEvenementsTest {

    @Autowired EcouteurEvenements ecouteur;
    @Autowired GererMesDemandes citoyen;
    @Autowired InstruireDemandes instruction;
    @Autowired ConsulterDemandes consultation;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @MockBean RegistreCivil registre;
    @MockBean ServiceDocuments documents;

    @Test
    void unPaiementConfirmeDeclencheLaRechercheUneSeuleFois() {
        Demande d = citoyen.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE,
                null, null, Fixtures.fatou()), Fixtures.FATOU);
        instruction.valider(d.id(), Fixtures.DELEGUE);
        when(registre.rechercher(any(), any())).thenReturn(new RegistreCivil.ResultatRecherche(
                RegistreCivil.Resultat.TROUVE, List.of(Fixtures.acte(true, TypeActe.NAISSANCE))));

        EnveloppeEvenement e = new EnveloppeEvenement(UUID.randomUUID(), Routages.PAIEMENT_CONFIRME, "paiement-service",
                Instant.now(), json.valueToTree(new PaiementConfirme("p1", d.id().toString(), d.reference(), 1000, "tok", Instant.now())));
        ecouteur.recevoir(e);
        ecouteur.recevoir(e); // même message reçu deux fois

        Demande apres = consultation.consulter(d.id(), "x", true);
        assertThat(apres.statut()).isEqualTo(StatutDemande.ACTE_TROUVE);
        verify(registre, times(1)).rechercher(any(), any());
        assertThat(jdbc.queryForList("SELECT contenu FROM evenement_sortant ORDER BY date_creation", String.class))
                .hasSize(4).allMatch(c -> c.contains("\"type\":\"demande.statut\""))
                .anyMatch(c -> c.contains("\"nouveauStatut\":\"ACTE_TROUVE\""));
    }
}
