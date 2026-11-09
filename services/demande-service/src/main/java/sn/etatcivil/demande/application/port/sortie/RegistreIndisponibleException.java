package sn.etatcivil.demande.application.port.sortie;

/** Un service distant (registre, documents) ne répond pas : l'opération pourra être retentée. */
public class RegistreIndisponibleException extends RuntimeException {
    public RegistreIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
