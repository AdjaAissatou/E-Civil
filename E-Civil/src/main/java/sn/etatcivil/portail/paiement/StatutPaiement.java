package sn.etatcivil.portail.paiement;

public enum StatutPaiement {
    /** Facture créée chez PayDunya, le citoyen n'a pas encore payé. */
    INITIE,
    /** Paiement confirmé par l'API PayDunya. */
    PAYE,
    /** Annulé par le citoyen ou expiré chez PayDunya. */
    ANNULE
}
