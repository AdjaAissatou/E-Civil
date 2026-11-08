// PaymentConfig.java
package EtatCivil.controllers;

import java.util.Properties;

public class PaymentConfig {
    private static Properties props = new Properties();
    
    static {
        try {
            // Chargez la configuration depuis un fichier
            props.load(PaymentConfig.class.getClassLoader().getResourceAsStream("payment.properties"));
        } catch (Exception e) {
            // Valeurs par défaut pour le développement
            props.setProperty("wave.api.key", "votre_cle_wave");
            props.setProperty("wave.api.secret", "votre_secret_wave");
            props.setProperty("wave.merchant.id", "votre_merchant_id");
            props.setProperty("orange.money.merchant.id", "votre_merchant_orange");
            props.setProperty("stripe.api.key", "votre_cle_stripe");
            props.setProperty("paypal.client.id", "votre_client_id_paypal");
            props.setProperty("paypal.client.secret", "votre_secret_paypal");
        }
    }
    
    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}