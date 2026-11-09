package sn.etatcivil.portail.registre;

import sn.etatcivil.portail.web.ActionRefuseeException;

/** Le registre a refusé la saisie (règle métier, données invalides) : message affichable à l'agent. */
public class RefusRegistreException extends ActionRefuseeException {
    public RefusRegistreException(String message) {
        super(message);
    }
}
