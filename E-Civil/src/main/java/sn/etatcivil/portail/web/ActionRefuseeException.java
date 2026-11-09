package sn.etatcivil.portail.web;

/** Action impossible pour une raison compréhensible par l'utilisateur (message affiché tel quel). */
public class ActionRefuseeException extends RuntimeException {
    public ActionRefuseeException(String message) {
        super(message);
    }
}
