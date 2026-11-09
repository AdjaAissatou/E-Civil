package sn.etatcivil.portail.demande;

/** Publié après chaque changement de statut ; les notifications l'écoutent une fois la transaction validée. */
public record ChangementStatut(Long demandeId, StatutDemande ancien, StatutDemande nouveau) {
}
