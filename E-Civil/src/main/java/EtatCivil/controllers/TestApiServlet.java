//package EtatCivil.controllers;
//
//import EtatCivil.dao.RenseignementDAO;
//import EtatCivil.modeles.Renseignement;
//import EtatCivil.services.ServeurCentralClient;
//import EtatCivil.services.ServeurCentralClient.ApiStatistiques;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//
//@WebServlet("/test-api")
//public class TestApiServlet extends HttpServlet {
//    
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        response.setContentType("text/plain;charset=UTF-8");
//        ServeurCentralClient client = new ServeurCentralClient();
//        
//        try {
//            response.getWriter().println("=== TEST CONNEXION API SERVEUR CENTRAL ===\n");
//            
//            // Test 0: Connexion basique
//            response.getWriter().println("🔌 Test de connexion basique...");
//            boolean connexionOk = client.testConnexion();
//            if (connexionOk) {
//                response.getWriter().println("✅ Serveur accessible");
//            } else {
//                response.getWriter().println("❌ Serveur non accessible");
//            }
//            
//            // Test 1: Statistiques
//            response.getWriter().println("\n📊 Test des statistiques...");
//            ApiStatistiques stats = client.getStatistiques();
//            response.getWriter().println("✅ Statistiques récupérées:");
//            response.getWriter().println("   - Total: " + stats.getTotalDemandes());
//            response.getWriter().println("   - En attente: " + stats.getEnAttente());
//            response.getWriter().println("   - Complètes: " + stats.getCompletes());
//            response.getWriter().println("   - Non trouvées: " + stats.getNonTrouves());
//            
//
//            
//            response.getWriter().println("\n🎉 TESTS TERMINÉS !");
//            
//        } catch (Exception e) {
//            response.getWriter().println("❌ ERREUR LORS DU TEST API");
//            response.getWriter().println("Message: " + e.getMessage());
//            response.getWriter().println("\n🔧 Vérifiez que:");
//            response.getWriter().println("   1. Le ServeurCentral Spring Boot est démarré sur le port 8081");
//            response.getWriter().println("   2. L'URL dans ApiConfig.java est: http://localhost:8081");
//            response.getWriter().println("   4. Les endpoints API existent dans le contrôleur Spring");
//            
//            e.printStackTrace(response.getWriter());
//        }
//    }
//}