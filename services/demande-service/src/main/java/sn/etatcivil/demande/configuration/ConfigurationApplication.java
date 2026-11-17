package sn.etatcivil.demande.configuration;

import org.springframework.amqp.core.Declarables;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.sortie.Horloge;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.FilesEvenements;
import sn.etatcivil.socle.transactions.TransactionsParAnnotation;

import java.time.Instant;

/** Assemble l'hexagone : enregistre les cas d'usage et leur ajoute les transactions. */
@Configuration
@ComponentScan(basePackages = "sn.etatcivil.demande.application", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CasDUsage.class))
class ConfigurationApplication {

    @Bean
    TransactionInterceptor intercepteurTransactions() {
        return TransactionsParAnnotation.intercepteur();
    }

    @Bean
    Advisor transactionsDesCasDUsage(TransactionInterceptor intercepteurTransactions) {
        return TransactionsParAnnotation.conseiller(CasDUsage.class, intercepteurTransactions);
    }

    @Bean
    Horloge horloge() {
        return Instant::now;
    }

    /** File de demande-service : paiements confirmés et actes validés, avec sa file d'erreurs. */
    @Bean
    Declarables fileDemandeService() {
        return FilesEvenements.file("demande-service", Routages.PAIEMENT_CONFIRME, Routages.ACTE_VALIDE);
    }
}
