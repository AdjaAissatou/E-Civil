package sn.etatcivil.demande.domaine;

public enum TypeActe {
    NAISSANCE("Naissance"), MARIAGE("Mariage"), DIVORCE("Divorce"), DECES("Décès");

    private final String libelle;

    TypeActe(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }

    public boolean concerneUnCouple() { return this == MARIAGE || this == DIVORCE; }
}
