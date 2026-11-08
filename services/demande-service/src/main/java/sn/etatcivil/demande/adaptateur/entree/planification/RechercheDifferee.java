package sn.etatcivil.demande.adaptateur.entree.planification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.entree.RechercherActes;
import sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException;
import sn.etatcivil.demande.domaine.Acteur;

import java.util.UUID;

/** Retente toutes les 5 minutes la recherche des demandes payées restées en attente (registre injoignable). */
@Component
class RechercheDifferee {

    private static final Logger log = LoggerFactory.getLogger(RechercheDifferee.class);

    private final RechercherActes recherche;

    RechercheDifferee(RechercherActes recherche) {
        this.recherche = recherche;
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "${ecivil.recherche.intervalle:PT5M}")
    void retenter() {
        for (UUID id : recherche.enAttenteDeRecherche()) {
            try {
                recherche.rechercher(id, Acteur.SYSTEME);
            } catch (RegistreIndisponibleException e) {
                log.warn("Registre toujours injoignable, nouvel essai plus tard");
                return;
            } catch (RuntimeException e) {
                log.warn("Recherche différée de la demande {} impossible : {}", id, e.getMessage());
            }
        }
    }
}
