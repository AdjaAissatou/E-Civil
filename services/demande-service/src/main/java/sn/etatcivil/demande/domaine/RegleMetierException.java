package sn.etatcivil.demande.domaine;

/** Action refusée par une règle de gestion ; le message est affiché tel quel à l'utilisateur. */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) { super(message); }
}
