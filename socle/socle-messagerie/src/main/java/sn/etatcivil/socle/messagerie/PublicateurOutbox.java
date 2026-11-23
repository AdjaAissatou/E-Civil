package sn.etatcivil.socle.messagerie;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Enregistre un événement dans la table « evenement_sortant », dans la transaction en cours.
 * Si la transaction métier est annulée, l'événement l'est aussi : jamais d'événement fantôme.
 */
public class PublicateurOutbox {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final String source;

    public PublicateurOutbox(JdbcTemplate jdbc, ObjectMapper json, String source) {
        this.jdbc = jdbc;
        this.json = json;
        this.source = source;
    }

    public UUID publier(String routage, Object evenement) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Un événement doit être publié dans une transaction métier");
        }
        UUID id = UUID.randomUUID();
        Instant maintenant = Instant.now();
        EnveloppeEvenement enveloppe = new EnveloppeEvenement(id, routage, source, maintenant, json.valueToTree(evenement));
        try {
            jdbc.update("INSERT INTO evenement_sortant (id, routage, contenu, date_creation) VALUES (?, ?, ?, ?)",
                    id.toString(), routage, json.writeValueAsString(enveloppe), Timestamp.from(maintenant));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Événement non sérialisable : " + routage, e);
        }
        return id;
    }
}
