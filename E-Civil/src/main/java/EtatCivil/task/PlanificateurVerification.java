package EtatCivil.task;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.services.GestionStatutService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PlanificateurVerification {
    private static final Logger logger = Logger.getLogger(PlanificateurVerification.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final GestionStatutService gestionStatutService;

    public PlanificateurVerification() {
        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        this.gestionStatutService = new GestionStatutService(demandeDAO);
    }

    public void demarrer() {
        // Vérifier toutes les 2 minutes
        scheduler.scheduleAtFixedRate(this::verifierRecherches, 0, 2, TimeUnit.MINUTES);
        logger.info("🔔 Planificateur de vérification démarré (toutes les 2 minutes)");
    }

    public void arreter() {
        scheduler.shutdown();
        logger.info("🔔 Planificateur de vérification arrêté");
    }

    private void verifierRecherches() {
        try {
            logger.info("🔍 Vérification périodique des recherches en cours...");
            gestionStatutService.verifierRecherchesEnCours();
        } catch (Exception e) {
            logger.severe("❌ Erreur lors de la vérification périodique: " + e.getMessage());
        }
    }
}