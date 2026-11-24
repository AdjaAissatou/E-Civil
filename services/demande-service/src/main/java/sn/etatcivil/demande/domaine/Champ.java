package sn.etatcivil.demande.domaine;

/** Rubriques du formulaire de demande qu'une variété de document fait remplir (indicateurs de la version 1). */
public enum Champ {
    PARENTS("Filiation (père et mère)"),
    PROFESSIONS("Professions et domiciles"),
    CONJOINT("Conjoint"),
    TEMOINS("Témoins"),
    MEDECIN("Médecin"),
    DECES("Décès (cause, date et lieu)"),
    MENTION("Mention marginale"),
    DOMICILE("Domicile à attester"),
    PRECISIONS("Précisions (coutume, situation particulière)");

    private final String libelle;

    Champ(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
