package sn.etatcivil.notification.domaine;

/** Ce que notification-service sait d'une demande qui vient de changer de statut. */
public record ChangementDemande(String demandeId, String reference, String nouveauStatut, String emailDemandeur,
                                String prenomDemandeur, String libelleDocument, String personneConcernee, int montant,
                                String format, String motifRejet) {
}
