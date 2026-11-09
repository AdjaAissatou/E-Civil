package sn.etatcivil.portail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Portail citoyen et back-office des agents de l'état civil. */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableAsync
public class PortailApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortailApplication.class, args);
    }
}
