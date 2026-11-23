package sn.etatcivil.paiement.adaptateur.sortie.messagerie;

import org.springframework.stereotype.Component;
import sn.etatcivil.evenements.PaiementConfirme;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.paiement.application.port.sortie.EvenementsPaiement;
import sn.etatcivil.paiement.domaine.DemandePayable;
import sn.etatcivil.paiement.domaine.Paiement;
import sn.etatcivil.socle.messagerie.PublicateurOutbox;

@Component
class EvenementsPaiementOutbox implements EvenementsPaiement {

    private final PublicateurOutbox outbox;

    EvenementsPaiementOutbox(PublicateurOutbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void paiementConfirme(Paiement p, DemandePayable d) {
        outbox.publier(Routages.PAIEMENT_CONFIRME, new PaiementConfirme(p.id().toString(), p.demandeId().toString(),
                d.reference(), p.montant(), p.token(), p.dateConfirmation()));
    }
}
