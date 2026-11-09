package sn.etatcivil.socle.messagerie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import sn.etatcivil.evenements.Routages;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Relaie vers RabbitMQ les événements en attente dans l'outbox, dans leur ordre de création.
 * Livraison « au moins une fois » : un message peut exceptionnellement être envoyé deux fois
 * (arrêt brutal entre l'envoi et le marquage), d'où la consommation idempotente côté abonnés.
 */
public class RelaisOutbox {

    private static final Logger log = LoggerFactory.getLogger(RelaisOutbox.class);

    private final JdbcTemplate jdbc;
    private final RabbitTemplate rabbit;

    public RelaisOutbox(JdbcTemplate jdbc, RabbitTemplate rabbit) {
        this.jdbc = jdbc;
        this.rabbit = rabbit;
    }

    private record EnAttente(String id, String routage, String contenu) {}

    @Scheduled(fixedDelayString = "${ecivil.messagerie.intervalle-relais:PT1S}")
    public void relayer() {
        List<EnAttente> lot = jdbc.query(
                "SELECT id, routage, contenu FROM evenement_sortant WHERE date_envoi IS NULL "
                        + "ORDER BY date_creation LIMIT 100",
                (rs, i) -> new EnAttente(rs.getString("id"), rs.getString("routage"), rs.getString("contenu")));
        for (EnAttente e : lot) {
            try {
                rabbit.send(Routages.ECHANGE, e.routage(), message(e));
                jdbc.update("UPDATE evenement_sortant SET date_envoi = ? WHERE id = ?", Timestamp.from(Instant.now()), e.id());
            } catch (AmqpException ex) {
                jdbc.update("UPDATE evenement_sortant SET tentatives = tentatives + 1 WHERE id = ?", e.id());
                log.warn("RabbitMQ indisponible, l'événement {} ({}) sera renvoyé : {}", e.id(), e.routage(), ex.getMessage());
                return; // on garde l'ordre : on réessaiera tout le lot au prochain passage
            }
        }
    }

    private static Message message(EnAttente e) {
        return MessageBuilder.withBody(e.contenu().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setMessageId(e.id())
                .setType(e.routage())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
    }
}
