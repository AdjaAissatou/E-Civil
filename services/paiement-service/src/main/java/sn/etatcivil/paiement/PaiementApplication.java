package sn.etatcivil.paiement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Paiement en ligne des demandes (PayDunya : Wave, Orange Money, Free Money, carte). */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PaiementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaiementApplication.class, args);
    }
}
