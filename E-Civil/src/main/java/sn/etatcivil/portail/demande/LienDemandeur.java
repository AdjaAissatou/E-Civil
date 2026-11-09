package sn.etatcivil.portail.demande;

/** Qualité du demandeur vis-à-vis de la personne concernée par l'acte. */
public enum LienDemandeur {
    MOI_MEME("Moi-même"), ENFANT("Mon enfant"), PARENT("Mon père ou ma mère"),
    CONJOINT("Mon conjoint"), AUTRE("Autre (mandataire)");

    private final String libelle;

    LienDemandeur(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
