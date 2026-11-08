package sn.etatcivil.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "sn.etatcivil.repositories") // ← AJOUT IMPORTANT
@EntityScan(basePackages = "sn.etatcivil.Modeles") // ✅ pour que Spring gère ActeRegistre

@ComponentScan(basePackages = {
    "sn.etatcivil.api", 
    "sn.etatcivil.controllers", 
    "sn.etatcivil.services"
})
public class ServeurCentralApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ServeurCentralApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ServeurCentralApplication.class, args);
        System.out.println("🚀 ===========================================");
        System.out.println("🚀 SERVEUR CENTRAL DÉMARRÉ AVEC PERSISTANCE");
        System.out.println("🚀 Port: 8081");
        System.out.println("🚀 Contexte: /ServeurCentral");
        System.out.println("🚀 Base de données: serveur-central-etat-civil");
        System.out.println("🚀 URL Base: http://localhost:8081/ServeurCentral");
        System.out.println("🚀 Health Check: http://localhost:8081/ServeurCentral/api/actes/health");
        System.out.println("🚀 Mode: PERSISTANCE RÉELLE (PostgreSQL)");
        System.out.println("🚀 ===========================================");
    }
}