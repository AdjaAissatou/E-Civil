package sn.etatcivil.notification.configuration;

import org.springframework.amqp.core.Declarables;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import sn.etatcivil.notification.application.CasDUsage;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.FilesEvenements;
import sn.etatcivil.socle.transactions.TransactionsParAnnotation;


/** Assemble l'hexagone : enregistre les cas d'usage et leur ajoute les transactions. */
@Configuration
@ComponentScan(basePackages = "sn.etatcivil.notification.application", useDefaultFilters = false,
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

    /** File de notification-service : chaque changement de statut d'une demande. */
    @Bean
    Declarables fileNotificationService() {
        return FilesEvenements.file("notification-service", Routages.DEMANDE_STATUT);
    }
}
