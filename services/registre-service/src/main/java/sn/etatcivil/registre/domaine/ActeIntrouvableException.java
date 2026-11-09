package sn.etatcivil.registre.domaine;

public class ActeIntrouvableException extends RuntimeException {
    public ActeIntrouvableException(Object identifiant) {
        super("Aucun acte ne correspond à " + identifiant);
    }
}
