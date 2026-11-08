package sn.etatcivil.central.acte;

/**
 * Cycle de vie d'une transcription : un agent la saisit, puis un officier de l'état civil la valide.
 * Seuls les actes validés peuvent être retrouvés et délivrés.
 */
public enum StatutActe {
    SAISI, VALIDE
}
