package sn.etatcivil.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/** Registre national : stocke les actes d'état civil et répond aux recherches du portail E-Civil. */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class RegistreCentralApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegistreCentralApplication.class, args);
    }
}
