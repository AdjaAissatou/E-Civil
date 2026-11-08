
//package EtatCivil.config;
//
//public class ApiConfig {
//    
//    public static String getBaseUrl() {
//        return "http://localhost:8080/serveur-central";
//    }
//    
//    // Pour l'instant, on utilise les endpoints publics
//    public static String getApiPath() {
//        return "/api/actes";
//    }
//    
//    // On n'utilise plus l'authentification pour le moment
//    public static String getAuthHeader() {
//        return null; // Ou une chaîne vide
//    }
// // Méthode pour vérifier la configuration
//    public static void logConfiguration() {
//        System.out.println("🔧 Configuration API:");
//        System.out.println("   📍 URL Base: " + getBaseUrl());
//        System.out.println("   🛣️  API Path: " + getApiPath());
//    }
//}
package EtatCivil.config;

public class ApiConfig {
    
//    public static String getBaseUrl() {
//        // Si le serveur central est sur un port différent
//        return System.getenv().getOrDefault("ECIVIL_API_BASE_URL", "http://localhost:8080");
//    }
    public static String getBaseUrl() {
        // CORRECTION : Changer 8081 en 8080
        return "http://localhost:8081/serveur-central";
    }
    public static String getApiPath() {
        return "/api/actes";
    }
    
    public static String getFullApiUrl(String endpoint) {
        return getBaseUrl() + getApiPath() + endpoint;
    }
    
    public static void logConfiguration() {
        System.out.println("🔧 Configuration API:");
        System.out.println("   📍 URL Base: " + getBaseUrl());
        System.out.println("   🛣️  API Path: " + getApiPath());
        System.out.println("   🌍 Mode: " + (getBaseUrl().contains("8081") ? "Serveur externe" : "Embedded"));
    }
}