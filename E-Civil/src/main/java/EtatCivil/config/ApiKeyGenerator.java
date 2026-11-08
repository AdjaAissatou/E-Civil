//package EtatCivil.config;
//
//import java.security.SecureRandom;
//import java.util.Base64;
//
//public class ApiKeyGenerator {
//    
//    public static String generateSecureApiKey() {
//        SecureRandom random = new SecureRandom();
//        byte[] bytes = new byte[32]; // 256 bits
//        random.nextBytes(bytes);
//        return "sk-ecivil-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
//    }
//    
//    public static void main(String[] args) {
//        String apiKey = generateSecureApiKey();
//        System.out.println("🔑 Clé API générée: " + apiKey);
//        System.out.println("📋 À copier dans application.properties et ApiConfig.java");
//    }
//}