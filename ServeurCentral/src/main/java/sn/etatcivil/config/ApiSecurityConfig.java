//package sn.etatcivil.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ApiSecurityConfig {
//    
//    @Value("${app.api.key}")
//    private String apiKey;
//    
//    @Bean
//    public String apiKey() {
//        System.out.println("🔑 Clé API chargée: " + maskApiKey(apiKey));
//        return apiKey;
//    }
//    
//    @Bean
//    public ApiKeyFilter apiKeyFilter() {
//        return new ApiKeyFilter(apiKey);
//    }
//    
//    private String maskApiKey(String key) {
//        if (key == null || key.length() <= 8) {
//            return "***";
//        }
//        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
//    }
//}