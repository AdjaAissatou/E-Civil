package sn.etatcivil.central.acte;

/** Données cohérentes au format mais refusées par une règle de l'état civil. */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) {
        super(message);
    }
}
