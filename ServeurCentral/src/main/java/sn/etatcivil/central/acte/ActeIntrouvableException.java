package sn.etatcivil.central.acte;

public class ActeIntrouvableException extends RuntimeException {
    public ActeIntrouvableException(Object id) {
        super("Aucun acte avec l'identifiant " + id);
    }
}
