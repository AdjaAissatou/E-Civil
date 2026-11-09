package sn.etatcivil.paiement.domaine;

public enum StatutPaiement {
    /** Facture créée chez l'opérateur, le citoyen n'a pas encore payé. */
    INITIE,
    /** Paiement confirmé par l'opérateur (statut payé et montant exact). */
    PAYE,
    /** Annulé par le citoyen ou expiré chez l'opérateur. */
    ANNULE
}
