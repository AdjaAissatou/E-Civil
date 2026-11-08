package sn.etatcivil.demande.domaine;

/** Demande inexistante, ou invisible pour l'utilisateur (on ne révèle pas son existence). */
public class DemandeIntrouvableException extends RuntimeException {
    public DemandeIntrouvableException() { super("Demande introuvable"); }
}
