package sn.etatcivil.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Délivrance des documents signés, génération des PDF et vérification publique d'authenticité. */
@SpringBootApplication
public class DocumentApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentApplication.class, args);
    }
}
