package sn.etatcivil.habilitation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Utilisateurs, profils, rôles et permissions : administration des habilitations, appliquée dans Keycloak. */
@SpringBootApplication
public class HabilitationApplication {
    public static void main(String[] args) {
        SpringApplication.run(HabilitationApplication.class, args);
    }
}
