package sn.etatcivil.notification.application.port.entree;

import sn.etatcivil.notification.domaine.ChangementDemande;

public interface NotifierDemandeur {
    void notifier(ChangementDemande changement);
}
