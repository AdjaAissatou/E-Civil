package sn.etatcivil.habilitation.adaptateur.entree.demarrage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import sn.etatcivil.habilitation.application.port.entree.InitialiserHabilitations;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Lance l'initialisation des habilitations une fois le service démarré, et la retente toutes les 15 secondes
 * tant que Keycloak ne répond pas (au premier démarrage, il peut être encore en train d'importer le realm).
 */
@Component
class InitialisationAuDemarrage {

    private static final Logger LOG = LoggerFactory.getLogger(InitialisationAuDemarrage.class);

    private final InitialiserHabilitations initialisation;
    private final boolean active;
    private final ScheduledExecutorService planificateur = Executors.newSingleThreadScheduledExecutor();

    InitialisationAuDemarrage(InitialiserHabilitations initialisation,
                              @Value("${ecivil.habilitations.initialisation:true}") boolean active) {
        this.initialisation = initialisation;
        this.active = active;
    }

    @EventListener(ApplicationReadyEvent.class)
    void demarrer() {
        if (active) planificateur.execute(this::tenter);
    }

    private void tenter() {
        try {
            initialisation.initialiser();
            LOG.info("Habilitations initialisées dans Keycloak");
            planificateur.shutdown();
        } catch (RuntimeException e) {
            LOG.warn("Initialisation des habilitations impossible pour l'instant ({}), nouvel essai dans 15 s", e.getMessage());
            planificateur.schedule(this::tenter, 15, TimeUnit.SECONDS);
        }
    }
}
