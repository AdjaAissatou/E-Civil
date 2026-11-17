package sn.etatcivil.demande.domaine;

import java.util.EnumSet;
import java.util.Set;

/** Cycle de vie d'une demande ; chaque passage est contrôlé par {@link #peutPasserA}. */
public enum StatutDemande {
    SOUMISE("Soumise", "En attente d'examen par un délégué"),
    A_PAYER("À payer", "Validée : le paiement en ligne est attendu"),
    PAYEE("Payée", "Paiement reçu : recherche dans le registre national en cours"),
    ACTE_TROUVE("Acte retrouvé", "L'acte a été retrouvé, il attend la signature de l'officier"),
    ACTE_INTROUVABLE("Acte non retrouvé", "Un agent du registre vérifie les registres papier"),
    SIGNEE("Signée", "Document signé et disponible"),
    LIVREE("Livrée", "Document papier remis ou expédié"),
    REJETEE("Rejetée", "Demande refusée"),
    ANNULEE("Annulée", "Demande annulée par le demandeur");

    private final String libelle;
    private final String explication;

    StatutDemande(String libelle, String explication) {
        this.libelle = libelle;
        this.explication = explication;
    }

    public String libelle() { return libelle; }
    public String explication() { return explication; }

    public Set<StatutDemande> suivantsPossibles() {
        return switch (this) {
            case SOUMISE -> EnumSet.of(A_PAYER, REJETEE, ANNULEE);
            case A_PAYER -> EnumSet.of(PAYEE, REJETEE, ANNULEE);
            case PAYEE -> EnumSet.of(ACTE_TROUVE, ACTE_INTROUVABLE);
            case ACTE_INTROUVABLE -> EnumSet.of(ACTE_TROUVE, REJETEE);
            case ACTE_TROUVE -> EnumSet.of(SIGNEE, ACTE_INTROUVABLE);
            case SIGNEE -> EnumSet.of(LIVREE);
            case LIVREE, REJETEE, ANNULEE -> EnumSet.noneOf(StatutDemande.class);
        };
    }

    public boolean peutPasserA(StatutDemande suivant) { return suivantsPossibles().contains(suivant); }

    public boolean estTerminal() { return suivantsPossibles().isEmpty(); }
}
