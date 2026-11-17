package sn.etatcivil.evenements;

/**
 * Clés de routage sur l'échange RabbitMQ « ecivil.evenements » (de type topic).
 * Chaque événement publié porte l'une de ces clés ; chaque service abonne sa file aux clés qui l'intéressent.
 */
public final class Routages {
    public static final String ECHANGE = "ecivil.evenements";

    /** {@link DemandeStatutChange} : publié par demande-service à chaque changement de statut. */
    public static final String DEMANDE_STATUT = "demande.statut";
    /** {@link PaiementConfirme} : publié par paiement-service quand PayDunya confirme un paiement. */
    public static final String PAIEMENT_CONFIRME = "paiement.confirme";
    /** {@link ActeValide} : publié par registre-service quand un officier valide une transcription. */
    public static final String ACTE_VALIDE = "registre.acte-valide";

    private Routages() {}
}
