package sn.etatcivil.habilitation.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import sn.etatcivil.habilitation.application.CasDUsage;
import sn.etatcivil.habilitation.application.port.sortie.Horloge;

import java.time.Instant;

/** Assemble l'hexagone : enregistre les cas d'usage (sans dépendance de la couche application à Spring). */
@Configuration
@ComponentScan(basePackages = "sn.etatcivil.habilitation.application", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CasDUsage.class))
class ConfigurationApplication {

    @Bean
    Horloge horloge() {
        return Instant::now;
    }
}
