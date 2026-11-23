package sn.etatcivil.demande.application.port.entree;

import java.util.UUID;

public interface TraiterPaiements {
    /** @return {@code true} si la demande vient de passer « payée » (et doit donc être recherchée). */
    boolean enregistrerPaiement(UUID demandeId, int montant, String detail);
}
