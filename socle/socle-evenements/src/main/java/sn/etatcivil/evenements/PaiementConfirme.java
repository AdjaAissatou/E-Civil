package sn.etatcivil.evenements;

import java.time.Instant;

/** PayDunya a confirmé le paiement complet d'une demande (statut « completed » et montant exact). */
public record PaiementConfirme(
        String paiementId,
        String demandeId,
        String reference,
        int montant,
        String tokenFacture,
        Instant date) {
}
