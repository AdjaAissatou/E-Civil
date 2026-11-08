//// RealPaymentService.java
//package EtatCivil.services;
//
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.io.OutputStream;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.primefaces.shaded.json.JSONObject;
//
//import EtatCivil.controllers.PaymentConfig;
//
//public class RealPaymentService {
//    
//    // Intégration avec Wave
//    public static Map<String, Object> processWavePayment(double amount, String phoneNumber, String reference) {
//        Map<String, Object> result = new HashMap<>();
//        
//        try {
//            // URL de l'API Wave (à vérifier avec la documentation officielle)
//            URL url = new URL("https://api.wave.com/v1/payments");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Authorization", "Bearer " + PaymentConfig.getProperty("wave.api.key"));
//            conn.setDoOutput(true);
//            
//            // Corps de la requête
//           JSONObject requestBody = new JSONObject();
//            requestBody.put("amount", amount);
//            requestBody.put("currency", "XOF");
//            requestBody.put("phone_number", phoneNumber);
//            requestBody.put("merchant_reference", reference);
//            requestBody.put("callback_url", "https://votresite.com/paiement/callback");
//            
//            // Envoyer la requête
//            try(OutputStream os = conn.getOutputStream()) {
//                byte[] input = requestBody.toString().getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//            
//            // Lire la réponse
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                
//                JSONObject jsonResponse = new JSONObject(response.toString());
//                result.put("success", true);
//                result.put("transaction_id", jsonResponse.getString("id"));
//                result.put("status", jsonResponse.getString("status"));
//                
//            } else {
//                result.put("success", false);
//                result.put("error", "Erreur API Wave: " + responseCode);
//            }
//            
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("error", "Exception: " + e.getMessage());
//        }
//        
//        return result;
//    }
//    
//    // Intégration avec Orange Money
//    public static Map<String, Object> processOrangeMoneyPayment(double amount, String phoneNumber, String reference) {
//        Map<String, Object> result = new HashMap<>();
//        
//        try {
//            // URL de l'API Orange Money (à adapter selon la documentation)
//            URL url = new URL("https://api.orange.com/orange-money-webpay/dev/v1/webpayment");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("Authorization", "Bearer " + getOrangeMoneyToken());
//            conn.setDoOutput(true);
//            
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("merchant_key", PaymentConfig.getProperty("orange.money.merchant.id"));
//            requestBody.put("currency", "XOF");
//            requestBody.put("order_id", reference);
//            requestBody.put("amount", amount);
//            requestBody.put("return_url", "https://votresite.com/paiement/success");
//            requestBody.put("cancel_url", "https://votresite.com/paiement/cancel");
//            requestBody.put("notif_url", "https://votresite.com/paiement/notification");
//            
//            try(OutputStream os = conn.getOutputStream()) {
//                byte[] input = requestBody.toString().getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//            
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                
//                JSONObject jsonResponse = new JSONObject(response.toString());
//                result.put("success", true);
//                result.put("payment_url", jsonResponse.getString("payment_url"));
//                
//            } else {
//                result.put("success", false);
//                result.put("error", "Erreur API Orange Money: " + responseCode);
//            }
//            
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("error", "Exception: " + e.getMessage());
//        }
//        
//        return result;
//    }
//    
//    // Intégration avec Stripe (cartes bancaires)
//    public static Map<String, Object> processStripePayment(double amount, String token, String description) {
//        Map<String, Object> result = new HashMap<>();
//        
//        try {
//            URL url = new URL("https://api.stripe.com/v1/charges");
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Authorization", "Bearer " + PaymentConfig.getProperty("stripe.api.key"));
//            conn.setDoOutput(true);
//            
//            // Convertir en centimes pour Stripe
//            int amountInCents = (int) (amount * 100);
//            
//            String formData = "amount=" + amountInCents + 
//                            "&currency=xof" +
//                            "&source=" + token +
//                            "&description=" + description;
//            
//            try(OutputStream os = conn.getOutputStream()) {
//                byte[] input = formData.getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//            
//            int responseCode = conn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                
//                JSONObject jsonResponse = new JSONObject(response.toString());
//                result.put("success", true);
//                result.put("charge_id", jsonResponse.getString("id"));
//                result.put("status", jsonResponse.getString("status"));
//                
//            } else {
//                result.put("success", false);
//                result.put("error", "Erreur API Stripe: " + responseCode);
//            }
//            
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("error", "Exception: " + e.getMessage());
//        }
//        
//        return result;
//    }
//    
//    private static String getOrangeMoneyToken() {
//        // Implémentez l'obtention du token OAuth pour Orange Money
//        // Cette méthode devrait gérer l'authentification OAuth 2.0
//        return "token_temporaire";
//    }
//}