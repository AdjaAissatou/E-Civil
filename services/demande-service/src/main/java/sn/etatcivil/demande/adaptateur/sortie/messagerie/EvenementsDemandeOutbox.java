package sn.etatcivil.demande.adaptateur.sortie.messagerie;

import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.EvenementsDemande;
import sn.etatcivil.demande.domaine.ChangementStatut;
import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.evenements.DemandeStatutChange;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.PublicateurOutbox;

@Component
class EvenementsDemandeOutbox implements EvenementsDemande {

    private final PublicateurOutbox outbox;

    EvenementsDemandeOutbox(PublicateurOutbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void statutChange(Demande d, ChangementStatut c) {
        outbox.publier(Routages.DEMANDE_STATUT, new DemandeStatutChange(
                d.id().toString(), d.reference(), c.ancien() == null ? null : c.ancien().name(), c.nouveau().name(),
                d.demandeur().id(), d.demandeur().email(), d.demandeur().prenom(), d.libelleDocument(),
                d.personne().nomComplet(), d.montant(), d.format().name(), d.motifRejet(), c.date()));
    }
}
