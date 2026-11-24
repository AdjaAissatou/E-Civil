package sn.etatcivil.demande.adaptateur.entree.planification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.entree.ConsulterPiecesIdentite;

/** Chaque nuit, efface les photos d'identité des demandes closes depuis plus de 30 jours (minimisation des données). */
@Component
class PurgePiecesIdentite {

    private static final Logger log = LoggerFactory.getLogger(PurgePiecesIdentite.class);

    private final ConsulterPiecesIdentite pieces;

    PurgePiecesIdentite(ConsulterPiecesIdentite pieces) {
        this.pieces = pieces;
    }

    @Scheduled(cron = "${ecivil.pieces.purge:0 30 2 * * *}", zone = "Africa/Dakar")
    void purger() {
        int n = pieces.purger();
        if (n > 0) log.info("{} photo(s) d'identité effacée(s) après la clôture de leur demande", n);
    }
}
