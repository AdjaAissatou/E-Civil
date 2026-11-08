package sn.etatcivil.portail.catalogue;

/** Mêmes valeurs que dans le registre central. */
public enum TypeActe {
    NAISSANCE("Naissance"), MARIAGE("Mariage"), DIVORCE("Divorce"), DECES("Décès");

    private final String libelle;

    TypeActe(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }

    public boolean concerneUnCouple() { return this == MARIAGE || this == DIVORCE; }
}
