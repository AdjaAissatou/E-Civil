package sn.etatcivil.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée unique : le navigateur, PayDunya et les administrations ne connaissent que cette adresse.
 * Les routes vers chaque microservice sont déclarées dans application.yml.
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
