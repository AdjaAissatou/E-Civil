package sn.etatcivil.notification.adaptateur.sortie.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import sn.etatcivil.notification.application.port.sortie.EnvoiEmail;
import sn.etatcivil.notification.domaine.Message;

/** Envoi SMTP. Si MAIL_HOST n'est pas configuré, le message est seulement journalisé (aucun envoi simulé). */
@Component
class EmailSmtp implements EnvoiEmail {

    private static final Logger log = LoggerFactory.getLogger(EmailSmtp.class);

    private final ObjectProvider<JavaMailSender> envoi;
    private final String hote;
    private final String expediteur;

    EmailSmtp(ObjectProvider<JavaMailSender> envoi, @Value("${spring.mail.host:}") String hote,
              @Value("${ecivil.mail.expediteur:}") String expediteur) {
        this.envoi = envoi;
        this.hote = hote;
        this.expediteur = expediteur;
    }

    @Override
    public void envoyer(Message m) {
        JavaMailSender mail = envoi.getIfAvailable();
        if (hote.isBlank() || mail == null) {
            log.info("Email non envoyé à {} (MAIL_HOST non configuré) : {}", m.destinataire(), m.sujet());
            return;
        }
        SimpleMailMessage s = new SimpleMailMessage();
        s.setFrom(expediteur);
        s.setTo(m.destinataire());
        s.setSubject(m.sujet());
        s.setText(m.corps());
        mail.send(s); // une erreur SMTP fait échouer le traitement : RabbitMQ retente puis range en file d'erreurs
        log.info("Email envoyé à {} : {}", m.destinataire(), m.sujet());
    }
}
