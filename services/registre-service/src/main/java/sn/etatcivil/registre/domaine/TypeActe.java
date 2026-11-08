package sn.etatcivil.registre.domaine;

public enum TypeActe {
    NAISSANCE("N"), MARIAGE("M"), DIVORCE("D"), DECES("DC");

    private final String prefixe;

    TypeActe(String prefixe) { this.prefixe = prefixe; }

    public String prefixe() { return prefixe; }

    public boolean concerneUnCouple() { return this == MARIAGE || this == DIVORCE; }
}
