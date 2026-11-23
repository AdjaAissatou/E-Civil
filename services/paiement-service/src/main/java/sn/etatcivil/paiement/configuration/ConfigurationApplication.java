package sn.etatcivil.paiement.configuration;

import org.springframework.amqp.core.Declarables;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import sn.etatcivil.paiement.application.CasDUsage;
import sn.etatcivil.paiement.application.port.sortie.Horloge;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.FilesEvenements;
import sn.etatcivil.socle.transactions.TransactionsParAnnotation;

import java.time.Instant;

/** Assemble l'hexagone : enregistre les cas d'usage et leur ajoute les transactions. */
@Configuration
@ComponentScan(basePackages = "sn.etatcivil.paiement.application", useDefaultFilters = false,
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

    /** File de paiement-service : changements de statut des demandes (pour savoir lesquelles sont payables). */
    @Bean
    Declarables filePaiementService() {
        return FilesEvenements.file("paiement-service", Routages.DEMANDE_STATUT);
    }
}
