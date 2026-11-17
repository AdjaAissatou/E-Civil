package sn.etatcivil.notification.adaptateur.entree.messagerie;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sn.etatcivil.evenements.DemandeStatutChange;
import sn.etatcivil.notification.application.port.entree.NotifierDemandeur;
import sn.etatcivil.notification.domaine.ChangementDemande;
import sn.etatcivil.socle.messagerie.EnveloppeEvenement;
import sn.etatcivil.socle.messagerie.TraitementIdempotent;

/** Un même événement reçu deux fois ne déclenche qu'un seul email. */
@Component
class EcouteurDemandes {

    private final TraitementIdempotent idempotence;
    private final NotifierDemandeur notification;

    EcouteurDemandes(TraitementIdempotent idempotence, NotifierDemandeur notification) {
        this.idempotence = idempotence;
        this.notification = notification;
    }

    @RabbitListener(queues = "notification-service")
    void recevoir(EnveloppeEvenement e) {
        idempotence.traiter(e, DemandeStatutChange.class, d -> notification.notifier(new ChangementDemande(d.demandeId(),
                d.reference(), d.nouveauStatut(), d.emailDemandeur(), d.prenomDemandeur(), d.libelleDocument(),
                d.personneConcernee(), d.montant(), d.format(), d.motifRejet())));
    }
}
