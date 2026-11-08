package sn.etatcivil.registre.domaine;

/** Opération refusée par une règle de l'état civil ; le message est destiné à l'utilisateur. */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) { super(message); }
}
