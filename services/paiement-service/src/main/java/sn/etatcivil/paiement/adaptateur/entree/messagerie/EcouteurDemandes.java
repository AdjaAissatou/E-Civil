package sn.etatcivil.paiement.adaptateur.entree.messagerie;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sn.etatcivil.evenements.DemandeStatutChange;
import sn.etatcivil.paiement.application.port.entree.SuivreDemandes;
import sn.etatcivil.socle.messagerie.EnveloppeEvenement;
import sn.etatcivil.socle.messagerie.TraitementIdempotent;

import java.util.UUID;

@Component
class EcouteurDemandes {

    private final TraitementIdempotent idempotence;
    private final SuivreDemandes suivi;

    EcouteurDemandes(TraitementIdempotent idempotence, SuivreDemandes suivi) {
        this.idempotence = idempotence;
        this.suivi = suivi;
    }

    @RabbitListener(queues = "paiement-service")
    void recevoir(EnveloppeEvenement e) {
        idempotence.traiter(e, DemandeStatutChange.class, d -> suivi.statutChange(UUID.fromString(d.demandeId()),
                d.reference(), d.demandeurId(), d.libelleDocument(), d.montant(), d.nouveauStatut()));
    }
}
