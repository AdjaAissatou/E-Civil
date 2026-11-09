package sn.etatcivil.demande;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Dépôt, instruction et suivi des demandes de documents d'état civil. */
@SpringBootApplication
public class DemandeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemandeApplication.class, args);
    }
}
