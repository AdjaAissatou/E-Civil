package sn.etatcivil.registre.domaine;

/** Les registres sont tenus en double : un exemplaire reste au centre, l'autre est déposé au greffe du tribunal. */
public enum Exemplaire {
    CENTRE("Exemplaire du centre"),
    GREFFE("Exemplaire du greffe");

    private final String libelle;

    Exemplaire(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
