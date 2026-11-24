package sn.etatcivil.habilitation.domaine;

/** Une règle de gestion des habilitations n'est pas respectée ; le message s'adresse à l'administrateur. */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) {
        super(message);
    }
}
