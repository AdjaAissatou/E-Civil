package sn.etatcivil.socle.messagerie;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/** Format commun de tous les messages : un identifiant unique (pour l'idempotence), le type et le contenu. */
public record EnveloppeEvenement(UUID id, String type, String source, Instant dateEmission, JsonNode donnees) {
}
