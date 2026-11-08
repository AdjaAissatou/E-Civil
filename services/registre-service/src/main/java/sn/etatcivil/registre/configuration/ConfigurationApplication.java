package sn.etatcivil.registre.configuration;

import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import sn.etatcivil.registre.application.CasDUsage;
import sn.etatcivil.registre.application.port.sortie.Horloge;
import sn.etatcivil.socle.transactions.TransactionsParAnnotation;

import java.time.Instant;

/** Assemble l'hexagone : enregistre les cas d'usage et leur ajoute les transactions. */
@Configuration
@ComponentScan(basePackages = "sn.etatcivil.registre.application", useDefaultFilters = false,
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
}
