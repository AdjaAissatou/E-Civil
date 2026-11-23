package sn.etatcivil.paiement.application.port.sortie;

import java.util.UUID;

/** Adresses publiques où l'opérateur renvoie le citoyen et envoie ses notifications. */
public interface AdressesRetour {
    PasserellePaiement.Retours pour(UUID demandeId);
}
