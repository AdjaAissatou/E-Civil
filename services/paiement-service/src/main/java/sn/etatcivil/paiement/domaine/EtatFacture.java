package sn.etatcivil.paiement.domaine;

/** État d'une facture tel que l'opérateur de paiement le rapporte. */
public record EtatFacture(Etat etat, long montant, String urlRecu) {
    public enum Etat { PAYEE, EN_ATTENTE, ANNULEE, INCONNUE }
}
