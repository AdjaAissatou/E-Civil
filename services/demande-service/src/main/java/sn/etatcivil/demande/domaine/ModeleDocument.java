package sn.etatcivil.demande.domaine;

/** Ce que contient le document délivré, et donc comment il se prépare. */
public enum ModeleDocument {
    /** Acte « original » : l'acte tel qu'il figure au registre, mentions marginales comprises. */
    ACTE_ORIGINAL("Acte original", Base.ACTE),
    /** Extrait d'un acte : identité, date et lieu, filiation. */
    EXTRAIT("Extrait", Base.ACTE),
    /** Extrait sans les noms des parents. */
    EXTRAIT_SANS_FILIATION("Extrait sans filiation", Base.ACTE),
    /** Reproduction intégrale de l'acte, témoins et mentions marginales compris. */
    COPIE_INTEGRALE("Copie littérale", Base.ACTE),
    /** Certificat attestant l'événement (naissance, mariage, décès, reconnaissance) d'après l'acte. */
    CERTIFICAT_ACTE("Certificat d'acte", Base.ACTE),
    /** Extrait d'une mention marginale (divorce, légitimation…) portée en marge de l'acte. */
    EXTRAIT_MENTION("Extrait de mention marginale", Base.ACTE),
    /** Livret de famille : l'acte de mariage et les naissances des enfants du couple. */
    LIVRET_FAMILLE("Livret de famille", Base.ACTE),
    /** Atteste qu'aucun mariage n'est inscrit au registre au nom de la personne. */
    CERTIFICAT_CELIBAT("Certificat de célibat", Base.SITUATION),
    /** Atteste que la personne peut contracter mariage (aucun mariage en cours). */
    CERTIFICAT_CAPACITE_MATRIMONIALE("Certificat de capacité matrimoniale", Base.SITUATION),
    /** Atteste que la personne est en vie (aucun décès inscrit, comparution vérifiée par le délégué). */
    CERTIFICAT_VIE("Certificat de vie", Base.SITUATION),
    /** Atteste la nationalité sénégalaise d'après l'acte de naissance et la filiation, sous le contrôle de l'officier. */
    CERTIFICAT_NATIONALITE("Certificat de nationalité", Base.ACTE),
    /** Atteste le domicile déclaré par la personne, après comparution devant le délégué. */
    CERTIFICAT_DOMICILE("Certificat de domicile", Base.ACTE),
    /** Établi par l'officier sur comparution, sans acte du registre (personne étrangère). */
    CERTIFICAT_NON_APPARTENANCE("Certificat de non-appartenance", Base.COMPARUTION),
    /** Atteste la coutume applicable (mariage mixte), établi par l'officier sur déclaration. */
    CERTIFICAT_COUTUME("Certificat de coutume", Base.COMPARUTION);

    /** Ce sur quoi le document s'appuie. */
    public enum Base {
        /** un acte du registre national */
        ACTE,
        /** l'acte de naissance et la situation de la personne dans le registre (mariages, décès) */
        SITUATION,
        /** la comparution de la personne devant l'officier : aucun acte n'est recherché */
        COMPARUTION
    }

    private final String libelle;
    private final Base base;

    ModeleDocument(String libelle, Base base) {
        this.libelle = libelle;
        this.base = base;
    }

    public String libelle() { return libelle; }

    public Base base() { return base; }

    /** Les certificats de situation s'appuient sur l'acte de naissance et sur ce que le registre sait de la personne. */
    public boolean estCertificat() { return base == Base.SITUATION; }

    /** Faut-il retrouver un acte dans le registre ? */
    public boolean exigeActe() { return base != Base.COMPARUTION; }

    /** Type d'acte imposé par ce modèle, ou {@code null} si tout type convient. */
    public TypeActe typeActeImpose() {
        return switch (this) {
            case LIVRET_FAMILLE -> TypeActe.MARIAGE;
            case EXTRAIT_SANS_FILIATION, CERTIFICAT_CELIBAT, CERTIFICAT_CAPACITE_MATRIMONIALE, CERTIFICAT_VIE,
                 CERTIFICAT_NATIONALITE, CERTIFICAT_DOMICILE, CERTIFICAT_NON_APPARTENANCE, CERTIFICAT_COUTUME -> TypeActe.NAISSANCE;
            default -> null;
        };
    }
}
