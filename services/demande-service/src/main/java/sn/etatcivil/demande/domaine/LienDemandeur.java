package sn.etatcivil.demande.domaine;

public enum LienDemandeur {
    MOI_MEME("Moi-même"), ENFANT("Mon enfant"), PARENT("Mon père ou ma mère"),
    CONJOINT("Mon conjoint"), AUTRE("Autre (mandataire)");

    private final String libelle;

    LienDemandeur(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
