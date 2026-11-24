package sn.etatcivil.registre.domaine;

/**
 * Nature d'un centre d'état civil : le centre principal de la commune, un centre secondaire qui lui est rattaché,
 * ou le centre d'une ambassade ou d'un consulat, qui tient les actes des Sénégalais de l'étranger.
 */
public enum TypeCentre {
    PRINCIPAL("Centre principal"),
    SECONDAIRE("Centre secondaire"),
    CONSULAIRE("Centre consulaire");

    private final String libelle;

    TypeCentre(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
