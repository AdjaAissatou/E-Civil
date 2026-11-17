package sn.etatcivil.registre.domaine;

/** D'où provient un acte inscrit au registre. */
public enum OrigineActe {
    /** Déclaré à l'officier de l'état civil dans les délais. */
    ORDINAIRE,
    /** Dressé en exécution d'un jugement qui supplée un acte jamais établi. */
    JUGEMENT_SUPPLETIF,
    /** Acte dressé à l'étranger, transcrit sur les registres sénégalais. */
    TRANSCRIPTION
}
