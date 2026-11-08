package sn.etatcivil.portail.demande;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sn.etatcivil.portail.registre.RegistreIndisponibleException;

/**
 * Lance la recherche dès qu'une demande est payée, puis retente toutes les 5 minutes
 * celles restées « Payée » parce que le registre central était injoignable.
 */
@Component
class RechercheAutomatique {

    private static final Logger log = LoggerFactory.getLogger(RechercheAutomatique.class);

    private final DemandeService service;

    RechercheAutomatique(DemandeService service) {
        this.service = service;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void apresPaiement(ChangementStatut e) {
        if (e.nouveau() == StatutDemande.PAYEE) rechercher(e.demandeId());
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT5M")
    public void retenter() {
        for (Long id : service.enAttenteDeRecherche()) {
            if (!rechercher(id)) return; // registre toujours injoignable : inutile d'insister
        }
    }

    private boolean rechercher(Long id) {
        try {
            service.rechercherDansRegistre(id, DemandeService.SYSTEME);
            return true;
        } catch (RegistreIndisponibleException ex) {
            log.warn("Registre central injoignable, la demande {} sera recherchée plus tard", id);
            return false;
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.warn("Demande {} modifiée en parallèle (ex. relance manuelle) : recherche automatique ignorée", id);
            return true;
        } catch (RuntimeException ex) {
            log.error("Échec de la recherche pour la demande {}", id, ex);
            return true;
        }
    }
}
