package sn.etatcivil.notification.application.port.sortie;

import sn.etatcivil.notification.domaine.Message;

public interface EnvoiEmail {
    void envoyer(Message message);
}
