package sn.etatcivil.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import sn.etatcivil.evenements.DemandeStatutChange;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.notification.application.port.sortie.EnvoiEmail;
import sn.etatcivil.socle.messagerie.EnveloppeEvenement;
import sn.etatcivil.socle.messagerie.TraitementIdempotent;
import sn.etatcivil.notification.application.port.entree.NotifierDemandeur;
import sn.etatcivil.notification.domaine.ChangementDemande;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/** Un événement reçu deux fois ne produit qu'un seul email. */
@SpringBootTest
class NotificationTest {

    @Autowired TraitementIdempotent idempotence;
    @Autowired NotifierDemandeur notification;
    @Autowired ObjectMapper json;
    @MockBean EnvoiEmail email;

    @Test
    void idempotence() {
        var evt = new DemandeStatutChange("d1", "DEM-2026-000001", "SOUMISE", "A_PAYER", "u", "fatou@example.sn",
                "Fatou", "Extrait", "Fatou Diallo", 1000, "NUMERIQUE", null, Instant.now());
        EnveloppeEvenement e = new EnveloppeEvenement(UUID.randomUUID(), Routages.DEMANDE_STATUT, "demande-service",
                Instant.now(), json.valueToTree(evt));
        for (int i = 0; i < 2; i++) {
            idempotence.traiter(e, DemandeStatutChange.class, d -> notification.notifier(new ChangementDemande(d.demandeId(),
                    d.reference(), d.nouveauStatut(), d.emailDemandeur(), d.prenomDemandeur(), d.libelleDocument(),
                    d.personneConcernee(), d.montant(), d.format(), d.motifRejet())));
        }
        verify(email, times(1)).envoyer(argThat(m -> m.destinataire().equals("fatou@example.sn")
                && m.corps().contains("http://localhost:8080/demandes/d1")));
    }
}
