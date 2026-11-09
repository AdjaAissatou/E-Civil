package sn.etatcivil.portail.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.demande.ChangementStatut;
import sn.etatcivil.portail.demande.Demande;
import sn.etatcivil.portail.demande.DemandeRepository;

/**
 * Informe le demandeur par email à chaque étape. Les emails partent après validation de la
 * transaction (jamais pour un changement annulé) et en arrière-plan (sans ralentir les agents).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final DemandeRepository demandes;
    private final ObjectProvider<JavaMailSender> envoi;
    private final ProprietesPortail proprietes;
    private final String hote;

    public NotificationService(DemandeRepository demandes, ObjectProvider<JavaMailSender> envoi,
                               ProprietesPortail proprietes, @Value("${spring.mail.host:}") String hote) {
        this.demandes = demandes;
        this.envoi = envoi;
        this.proprietes = proprietes;
        this.hote = hote;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surChangement(ChangementStatut e) {
        demandes.avecDemandeur(e.demandeId()).ifPresent(d -> {
            String corps = corps(d);
            if (corps != null) envoyer(d.getDemandeur().getEmail(),
                    "E-Civil – " + d.getReference() + " : " + d.getStatut().getLibelle(), corps);
        });
    }

    private String corps(Demande d) {
        String lien = proprietes.lien("/demandes/" + d.getId());
        String document = d.getTypeDocument().getLibelle() + " de " + d.getPersonneConcernee();
        String message = switch (d.getStatut()) {
            case SOUMISE -> "Nous avons bien reçu votre demande (" + document + "). Un délégué va l'examiner.";
            case A_PAYER -> "Votre demande a été validée. Réglez " + d.getMontant()
                    + " FCFA en ligne (Wave, Orange Money, Free Money ou carte) pour lancer la recherche de l'acte.";
            case PAYEE -> "Nous avons reçu votre paiement de " + d.getMontant()
                    + " FCFA. La recherche de l'acte dans le registre central est en cours.";
            case ACTE_TROUVE -> "L'acte a été retrouvé dans le registre central. Il est en cours de signature.";
            case ACTE_INTROUVABLE -> "L'acte n'a pas été retrouvé automatiquement. Un agent du registre "
                    + "vérifie les registres papier ; vous serez informé(e) dès qu'il aura abouti.";
            case SIGNEE -> switch (d.getFormat()) {
                case NUMERIQUE -> "Votre document est signé. Vous pouvez le télécharger dans votre espace.";
                case PHYSIQUE -> "Votre document est signé. Il va être imprimé et expédié à l'adresse indiquée.";
            };
            case LIVREE -> "Votre document a été remis au service de livraison.";
            case REJETEE -> "Votre demande a été rejetée. Motif : " + d.getMotifRejet();
            case ANNULEE -> null;
        };
        if (message == null) return null;
        return "Bonjour " + d.getDemandeur().getPrenom() + ",\n\n" + message
                + "\n\nSuivre votre demande : " + lien + "\n\nL'équipe E-Civil";
    }

    private void envoyer(String destinataire, String sujet, String texte) {
        JavaMailSender mail = envoi.getIfAvailable();
        if (hote.isBlank() || mail == null) {
            log.info("Email non envoyé à {} (MAIL_HOST non configuré) : {}", destinataire, sujet);
            return;
        }
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(proprietes.mail().expediteur());
        m.setTo(destinataire);
        m.setSubject(sujet);
        m.setText(texte);
        try {
            mail.send(m);
        } catch (MailException ex) {
            log.error("Échec d'envoi de l'email à {} : {}", destinataire, ex.getMessage());
        }
    }
}
