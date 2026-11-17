package sn.etatcivil.paiement.application.port.sortie;

public class OperateurIndisponibleException extends RuntimeException {
    public OperateurIndisponibleException(String message, Throwable cause) { super(message, cause); }
}
