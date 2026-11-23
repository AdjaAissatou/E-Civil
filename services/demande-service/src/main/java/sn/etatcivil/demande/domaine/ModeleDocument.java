package sn.etatcivil.demande.domaine;

/** Ce que contient le document délivré, et donc comment il se prépare. */
public enum ModeleDocument {
    /** Extrait d'un acte : identité, date et lieu, filiation. */
    EXTRAIT,
    /** Extrait sans les noms des parents. */
    EXTRAIT_SANS_FILIATION,
    /** Reproduction intégrale de l'acte, témoins et mentions marginales compris. */
    COPIE_INTEGRALE,
    /** Duplicata du livret de famille : l'acte de mariage et les naissances des enfants du couple. */
    LIVRET_FAMILLE,
    /** Atteste qu'aucun mariage n'est inscrit au registre au nom de la personne. */
    CERTIFICAT_CELIBAT,
    /** Atteste que la personne peut contracter mariage (aucun mariage en cours). */
    CERTIFICAT_CAPACITE_MATRIMONIALE,
    /** Atteste que la personne est en vie (aucun décès inscrit, comparution vérifiée par le délégué). */
    CERTIFICAT_VIE;

    /** Les certificats s'appuient sur l'acte de naissance et sur la situation de la personne dans le registre. */
    public boolean estCertificat() {
        return this == CERTIFICAT_CELIBAT || this == CERTIFICAT_CAPACITE_MATRIMONIALE || this == CERTIFICAT_VIE;
    }
}
