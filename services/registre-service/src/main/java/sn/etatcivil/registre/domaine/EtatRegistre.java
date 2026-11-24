package sn.etatcivil.registre.domaine;

/** État matériel d'un registre papier, constaté par les agents qui le consultent. */
public enum EtatRegistre {
    BON("Bon état", true),
    ABIME("Abîmé", true),
    INCOMPLET("Incomplet (pages manquantes)", true),
    MANQUANT("Manquant ou détruit", false);

    private final String libelle;
    private final boolean consultable;

    EtatRegistre(String libelle, boolean consultable) {
        this.libelle = libelle;
        this.consultable = consultable;
    }

    public String libelle() { return libelle; }

    /** Un registre manquant ne peut pas être consulté : il faut l'autre exemplaire ou un jugement. */
    public boolean consultable() { return consultable; }
}
