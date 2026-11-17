package sn.etatcivil.registre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Registre national : conserve les actes transcrits et répond aux recherches des autres services. */
@SpringBootApplication
public class RegistreApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistreApplication.class, args);
    }
}
