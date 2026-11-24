package sn.etatcivil.demande.domaine;

/** Où en est la recherche d'un acte dans les registres papier. */
public enum EtatRenseignement {
    EN_COURS("En cours"),
    TROUVE("Acte retrouvé"),
    NON_TROUVE("Acte non trouvé"),
    CLOS("Clos sans suite");

    private final String libelle;

    EtatRenseignement(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
