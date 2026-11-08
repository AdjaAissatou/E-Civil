package sn.etatcivil.socle.messagerie;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Exécute le traitement d'un événement une seule fois, dans une transaction : l'identifiant de
 * l'événement est enregistré avec les effets du traitement. Un doublon est simplement ignoré ;
 * une erreur annule tout, et RabbitMQ renvoie le message (puis le range en file d'erreurs).
 */
public class TraitementIdempotent {

    private static final Logger log = LoggerFactory.getLogger(TraitementIdempotent.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final ObjectMapper json;

    public TraitementIdempotent(JdbcTemplate jdbc, TransactionTemplate transaction, ObjectMapper json) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.json = json;
    }

    /** @return {@code true} si l'événement a été traité, {@code false} s'il l'avait déjà été. */
    public <T> boolean traiter(EnveloppeEvenement enveloppe, Class<T> type, Consumer<T> action) {
        T evenement = json.convertValue(enveloppe.donnees(), type);
        Boolean traite = transaction.execute(statut -> {
            int ajoute = jdbc.update("INSERT INTO evenement_traite (id, date_traitement) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    enveloppe.id().toString(), Timestamp.from(Instant.now()));
            if (ajoute == 0) return false;
            action.accept(evenement);
            return true;
        });
        if (!Boolean.TRUE.equals(traite)) {
            log.info("Événement {} ({}) déjà traité : ignoré", enveloppe.id(), enveloppe.type());
            return false;
        }
        return true;
    }
}
