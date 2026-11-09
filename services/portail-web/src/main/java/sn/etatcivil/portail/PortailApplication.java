package sn.etatcivil.portail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Interface web des citoyens et des agents. Le portail ne possède aucune donnée : il affiche les pages
 * et appelle les microservices (via la passerelle) avec le jeton Keycloak de l'utilisateur connecté.
 */
@SpringBootApplication
public class PortailApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortailApplication.class, args);
    }
}
