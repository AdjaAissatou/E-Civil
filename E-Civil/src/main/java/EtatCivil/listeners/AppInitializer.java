package EtatCivil.listeners;

import EtatCivil.task.PlanificateurVerification;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class AppInitializer implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(AppInitializer.class.getName());
    private PlanificateurVerification planificateur;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("🚀 Initialisation de l'application État Civil");
        
        // Démarrer le planificateur de vérification
        planificateur = new PlanificateurVerification();
        planificateur.demarrer();
        
        logger.info("✅ Planificateur de vérification démarré");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (planificateur != null) {
            planificateur.arreter();
            logger.info("🔴 Planificateur de vérification arrêté");
        }
    }
}