package EtatCivil.services;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.modeles.DemandeDocument;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GestionStatutService {
    private static final Logger logger = Logger.getLogger(GestionStatutService.class.getName());
    private final DemandeDocumentDAO demandeDAO;
    private final ServeurCentralClient serveurCentralClient;

    public GestionStatutService(DemandeDocumentDAO demandeDAO) {
        this.demandeDAO = demandeDAO;
        this.serveurCentralClient = new ServeurCentralClient();
    }

    /**
     * Mettre à jour le statut d'une demande et déclencher les actions appropriées
     */
    public boolean mettreAJourStatut(int idDemande, String nouveauStatut, String commentaire) {
        try {
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            if (demande == null) {
                logger.warning("❌ Demande non trouvée: " + idDemande);
                return false;
            }

            String ancienStatut = demande.getStatus();
            demande.setStatus(nouveauStatut);
            
            // Mettre à jour en base de données
            boolean success = demandeDAO.updateStatus(idDemande, nouveauStatut);
            
            if (success) {
                logger.info("✅ Statut mis à jour: " + idDemande + " [" + ancienStatut + " → " + nouveauStatut + "]");
                
                // Ajouter à l'historique des statuts
                try {
                    demandeDAO.ajouterHistoriqueStatut(idDemande, nouveauStatut, commentaire);
                } catch (SQLException e) {
                    logger.warning("⚠️ Impossible d'ajouter à l'historique: " + e.getMessage());
                }
                
                // Déclencher les actions spécifiques selon le nouveau statut
                declencherActionsStatut(demande, nouveauStatut, ancienStatut);
                
                return true;
            }
            
        } catch (Exception e) {
            logger.severe("❌ ERREUR mise à jour statut: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Déclencher les actions spécifiques selon le changement de statut
     */
    private void declencherActionsStatut(DemandeDocument demande, String nouveauStatut, String ancienStatut) {
        switch (nouveauStatut.toLowerCase()) {
            case "recherché":
                if (!"recherché".equalsIgnoreCase(ancienStatut)) {
                    // Lancer AUTOMATIQUEMENT la recherche dans le serveur central
                    lancerRechercheServeurCentral(demande);
                }
                break;
                
            case "en cours":
                // "en cours" signifie que le paiement est requis
                logger.info("💰 Paiement requis pour demande: " + demande.getIdDemande());
                // Pas d'action automatique - attendre le paiement
                break;
                
            case "terminée":
                // Actions pour le statut "terminée"
                logger.info("✅ Demande terminée: " + demande.getIdDemande());
                break;
                
            case "rejetée":
                // Actions pour le statut "rejetée"
                logger.info("❌ Demande rejetée: " + demande.getIdDemande());
                break;
                
            default:
                logger.info("📝 Changement de statut standard: " + ancienStatut + " → " + nouveauStatut);
        }
    }

    /**
     * Lancer la recherche dans le serveur central
     */
    private void lancerRechercheServeurCentral(DemandeDocument demande) {
        try {
            logger.info("🚀 Lancement recherche serveur central pour demande: " + demande.getIdDemande());
            
            // Récupérer les variétés supplémentaires pour enrichir la recherche
            Map<String, String> varietes = demandeDAO.getVarietesByDemandeId(demande.getIdDemande());
            if (varietes != null && !varietes.isEmpty()) {
                demande.setInformationsSupplementaires(new java.util.HashMap<>(varietes));
            }
            
            boolean rechercheLancee = serveurCentralClient.lancerRechercheActe(demande);
            
            if (rechercheLancee) {
                logger.info("✅ Recherche lancée avec succès pour demande: " + demande.getIdDemande());
                
                // Optionnel: Marquer que la recherche est lancée
                // demande.setRechercheLancee(true);
                
            } else {
                logger.warning("⚠️ Échec du lancement de la recherche pour demande: " + demande.getIdDemande());
                // Optionnel: Revenir au statut précédent ou mettre en erreur
            }
            
        } catch (Exception e) {
            logger.severe("❌ ERREUR lors du lancement de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifier périodiquement le statut des recherches en cours
     */
    public void verifierRecherchesEnCours() {
        try {
            // Récupérer toutes les demandes en statut "recherché"
            List<DemandeDocument> demandesEnRecherche = demandeDAO.getDemandesByStatus("recherché");
            
            for (DemandeDocument demande : demandesEnRecherche) {
                Map<String, Object> statutRecherche = serveurCentralClient.verifierStatutRecherche(
                    (long) demande.getIdDemande());
                
                String statut = (String) statutRecherche.get("statut");
                if ("trouvé".equals(statut)) {
                    mettreAJourStatut(demande.getIdDemande(), "terminée", 
                        "Document trouvé et généré avec succès");
                } else if ("non_trouvé".equals(statut)) {
                    mettreAJourStatut(demande.getIdDemande(), "rejetée", 
                        "Document non trouvé dans les registres du serveur central");
                }
                // Si "en_cours", on laisse le statut "recherché"
            }
            
        } catch (Exception e) {
            logger.severe("❌ ERREUR vérification recherches en cours: " + e.getMessage());
        }
    }

    /**
     * Méthode spécifique pour après paiement - change le statut vers "recherché"
     */
    public boolean apresPaiement(int idDemande) {
        return mettreAJourStatut(idDemande, "recherché", 
            "Paiement effectué - Lancement automatique de la recherche");
    }

    /**
     * Méthode pour lancer manuellement la recherche
     */
    public boolean lancerRechercheManuelle(int idDemande) {
        return mettreAJourStatut(idDemande, "recherché", 
            "Lancement manuel de la recherche dans le serveur central");
    }
}