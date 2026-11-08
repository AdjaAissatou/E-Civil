package sn.etatcivil.notification.application.service;

import sn.etatcivil.notification.application.CasDUsage;
import sn.etatcivil.notification.application.port.entree.NotifierDemandeur;
import sn.etatcivil.notification.application.port.sortie.EnvoiEmail;
import sn.etatcivil.notification.application.port.sortie.LiensPortail;
import sn.etatcivil.notification.domaine.ChangementDemande;
import sn.etatcivil.notification.domaine.Redaction;

@CasDUsage
public class ServiceNotifications implements NotifierDemandeur {

    private final EnvoiEmail email;
    private final LiensPortail liens;

    public ServiceNotifications(EnvoiEmail email, LiensPortail liens) {
        this.email = email;
        this.liens = liens;
    }

    @Override
    public void notifier(ChangementDemande c) {
        Redaction.rediger(c, liens.demande(c.demandeId())).ifPresent(email::envoyer);
    }
}
