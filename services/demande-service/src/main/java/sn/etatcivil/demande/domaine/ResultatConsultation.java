package sn.etatcivil.demande.domaine;

/** Ce que le chargé de renseignement a constaté en consultant un registre papier. */
public enum ResultatConsultation {
    ACTE_PRESENT("Acte présent dans ce registre"),
    ABSENT("Aucun acte correspondant"),
    PAGE_ILLISIBLE("Page illisible ou abîmée"),
    REGISTRE_INDISPONIBLE("Registre manquant ou indisponible");

    private final String libelle;

    ResultatConsultation(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
