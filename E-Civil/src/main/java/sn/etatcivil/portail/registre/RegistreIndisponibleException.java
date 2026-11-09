package sn.etatcivil.portail.registre;

/** Le registre central ne répond pas (réseau, arrêt, clé API refusée). */
public class RegistreIndisponibleException extends RuntimeException {
    public RegistreIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
