//package EtatCivil.dao;
//
//import EtatCivil.config.ApiConfig;
//import EtatCivil.modeles.Renseignement;
//import EtatCivil.services.ServeurCentralClient;
//import EtatCivil.services.ServeurCentralClient.ApiStatistiques;
//
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//public class RenseignementDAO {
//    private final ServeurCentralClient apiClient;
//    private final RestTemplate restTemplate;
//
//    public RenseignementDAO() {
//        this.restTemplate = new RestTemplate();
//
//        this.apiClient = new ServeurCentralClient();
//    }
//
//    
//
//    
// // Méthode pour créer un nouvel acte via API
//    public boolean creerNouvelActe(Renseignement renseignement) {
//        try {
//            System.out.println("🔄 DAO: Création nouvel acte via API");
//            boolean success = apiClient.creerNouvelActe(renseignement);
//            System.out.println("✅ DAO: Création acte via API - " + (success ? "Succès" : "Échec"));
//            return success;
//        } catch (Exception e) {
//            System.err.println("❌ ERREUR DAO API création: " + e.getMessage());
//            throw new RuntimeException("Erreur lors de la création d'acte via API", e);
//        }
//    }
//
//    // Les autres méthodes peuvent être supprimées car non utilisées
//    
////    // Mettre à jour un renseignement via API
////    public boolean mettreAJourRenseignement(Renseignement renseignement) {
////        try {
////            System.out.println("🔄 DAO: Mise à jour renseignement via API - ID: " + renseignement.getIdRenseignement());
////            boolean success = apiClient.mettreAJourRenseignement(renseignement);
////            System.out.println("✅ DAO: Mise à jour via API - " + (success ? "Succès" : "Échec"));
////            return success;
////        } catch (Exception e) {
////            System.err.println("❌ ERREUR DAO API mise à jour: " + e.getMessage());
////            throw new RuntimeException("Erreur lors de la mise à jour via API", e);
////        }
////    }
//    
//    // Récupérer les statistiques via API
//    public ApiStatistiques getStatistiques() {
//        try {
//            String url = ApiConfig.getBaseUrl() + ApiConfig.getApiPath() + "/statistiques";
//            System.out.println("📡 Appel API Statistiques: " + url);
//            
//            ResponseEntity<ApiStatistiques> response = restTemplate.getForEntity(url, ApiStatistiques.class);
//            System.out.println("📡 Réponse statistiques - Status: " + response.getStatusCode());
//            
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                return response.getBody();
//            } else {
//                System.err.println("❌ Erreur API: Statut " + response.getStatusCode());
//                return createDefaultStats();
//            }
//        } catch (Exception e) {
//            System.err.println("❌ ERREUR API statistiques: " + e.getMessage());
//            return createDefaultStats();
//        }
//    }
//    
//    // Méthodes utilitaires pour la compatibilité avec l'ancien code
//    public int getTotalRenseignements() {
//        return getStatistiques().getTotalDemandes();
//    }
//    
//    public int getRenseignementsByStatutCount(String statut) {
//        ApiStatistiques stats = getStatistiques();
//        switch(statut) {
//            case "en_attente": return stats.getEnAttente();
//            case "complete": return stats.getCompletes();
//            case "non_trouve": return stats.getNonTrouves();
//            default: return 0;
//        }
//    }
//    
//    // Alias pour la compatibilité
//    public int getTotalDemandesARenseigner() {
//        return getTotalRenseignements();
//    }
//    
//    public int getDemandesByStatutCount(String statut) {
//        return getRenseignementsByStatutCount(statut);
//    }
//    
//    // Statistiques par défaut en cas d'erreur API
//    private ApiStatistiques createDefaultStats() {
//        ApiStatistiques defaultStats = new ApiStatistiques();
//        defaultStats.setTotalDemandes(0);
//        defaultStats.setEnAttente(0);
//        defaultStats.setCompletes(0);
//        defaultStats.setNonTrouves(0);
//        return defaultStats;
//    }
//    
//    public void close() {
//        // Nothing to close for API client
//    }
//}