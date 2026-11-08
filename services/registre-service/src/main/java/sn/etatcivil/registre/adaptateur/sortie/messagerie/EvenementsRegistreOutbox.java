package sn.etatcivil.registre.adaptateur.sortie.messagerie;

import org.springframework.stereotype.Component;
import sn.etatcivil.evenements.ActeValide;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.registre.application.port.sortie.EvenementsRegistre;
import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.socle.messagerie.PublicateurOutbox;

import java.time.Instant;

@Component
class EvenementsRegistreOutbox implements EvenementsRegistre {

    private final PublicateurOutbox outbox;

    EvenementsRegistreOutbox(PublicateurOutbox outbox) {
        this.outbox = outbox;
    }

    @Override
    public void acteValide(Acte acte) {
        outbox.publier(Routages.ACTE_VALIDE,
                new ActeValide(acte.id().toString(), acte.numeroActe(), acte.typeActe().name(), Instant.now()));
    }
}
