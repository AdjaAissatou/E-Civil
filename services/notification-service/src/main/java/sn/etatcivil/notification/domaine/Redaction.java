package sn.etatcivil.notification.domaine;

import java.util.Optional;

/** Rédige le message adressé au citoyen pour chaque étape de sa demande. */
public final class Redaction {
    private Redaction() {}

    public static Optional<Message> rediger(ChangementDemande c, String lienSuivi) {
        if (c.emailDemandeur() == null || c.emailDemandeur().isBlank()) return Optional.empty();
        String document = c.libelleDocument() + " de " + c.personneConcernee();
        String texte = switch (c.nouveauStatut()) {
            case "SOUMISE" -> "Nous avons bien reçu votre demande (" + document + "). Un délégué va l'examiner.";
            case "A_PAYER" -> "Votre demande a été validée. Réglez " + c.montant()
                    + " FCFA en ligne (Wave, Orange Money, Free Money ou carte) pour lancer la recherche de l'acte.";
            case "PAYEE" -> "Nous avons reçu votre paiement de " + c.montant()
                    + " FCFA. La recherche de l'acte dans le registre national est en cours.";
            case "ACTE_TROUVE" -> "L'acte a été retrouvé dans le registre national. Il est en cours de signature.";
            case "ACTE_INTROUVABLE" -> "L'acte n'a pas été retrouvé automatiquement. Un agent du registre vérifie les "
                    + "registres papier ; vous serez informé(e) dès qu'il aura abouti.";
            case "SIGNEE" -> "NUMERIQUE".equals(c.format())
                    ? "Votre document est signé. Vous pouvez le télécharger dans votre espace."
                    : "Votre document est signé. Il va être imprimé et expédié à l'adresse indiquée.";
            case "LIVREE" -> "Votre document a été remis au service de livraison.";
            case "REJETEE" -> "Votre demande a été rejetée. Motif : " + c.motifRejet();
            default -> null; // ANNULEE : action du citoyen lui-même, pas de message
        };
        if (texte == null) return Optional.empty();
        String prenom = c.prenomDemandeur() == null || c.prenomDemandeur().isBlank() ? "" : " " + c.prenomDemandeur();
        return Optional.of(new Message(c.emailDemandeur(), "E-Civil – " + c.reference() + " : " + libelle(c.nouveauStatut()),
                "Bonjour" + prenom + ",\n\n" + texte + "\n\nSuivre votre demande : " + lienSuivi + "\n\nL'équipe E-Civil"));
    }

    private static String libelle(String statut) {
        return switch (statut) {
            case "SOUMISE" -> "demande reçue";
            case "A_PAYER" -> "paiement attendu";
            case "PAYEE" -> "paiement reçu";
            case "ACTE_TROUVE" -> "acte retrouvé";
            case "ACTE_INTROUVABLE" -> "recherche en cours";
            case "SIGNEE" -> "document disponible";
            case "LIVREE" -> "document expédié";
            case "REJETEE" -> "demande rejetée";
            default -> statut.toLowerCase();
        };
    }
}
