package sn.etatcivil.socle.messagerie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import sn.etatcivil.evenements.Routages;

/** Configuration automatique de la messagerie, partagée par tous les services. */
@AutoConfiguration(after = {RabbitAutoConfiguration.class, JdbcTemplateAutoConfiguration.class})
@EnableScheduling
public class ConfigurationMessagerie {

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    MessageConverter convertisseurJson(ObjectMapper json) {
        Jackson2JsonMessageConverter c = new Jackson2JsonMessageConverter(json);
        c.setAlwaysConvertToInferredType(true); // le type est celui du paramètre de l'écouteur
        return c;
    }

    @Bean
    TopicExchange echangeEvenements() {
        return new TopicExchange(Routages.ECHANGE, true, false);
    }

    @Bean
    @ConditionalOnMissingBean
    PublicateurOutbox publicateurOutbox(JdbcTemplate jdbc, ObjectMapper json,
                                        @Value("${spring.application.name:ecivil}") String source) {
        return new PublicateurOutbox(jdbc, json, source);
    }

    @Bean
    @ConditionalOnProperty(name = "ecivil.messagerie.relais-actif", havingValue = "true", matchIfMissing = true)
    RelaisOutbox relaisOutbox(JdbcTemplate jdbc, RabbitTemplate rabbit) {
        return new RelaisOutbox(jdbc, rabbit);
    }

    @Bean
    @ConditionalOnMissingBean
    TraitementIdempotent traitementIdempotent(JdbcTemplate jdbc, PlatformTransactionManager tm, ObjectMapper json) {
        return new TraitementIdempotent(jdbc, new TransactionTemplate(tm), json);
    }
}
