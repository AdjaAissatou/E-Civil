package sn.etatcivil.demande.domaine;

import java.time.Instant;
import java.util.UUID;

/** Ligne de l'historique d'une demande. */
public record ChangementStatut(UUID id, StatutDemande ancien, StatutDemande nouveau, String auteur,
                               String commentaire, Instant date) {
}
