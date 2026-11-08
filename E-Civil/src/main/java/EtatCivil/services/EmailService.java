package EtatCivil.services;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String USERNAME = "ajdaoutassia@gmail.com";
    private static final String PASSWORD = "xgkj tkqh knfl fjyb";
    
    public static boolean envoyerEmailSimple(String to, String subject, String body) {
        try {
            // Configuration des propriétés
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            
            // Création de l'authentificateur
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            };
            
            // Création de la session
            Session session = Session.getInstance(props, authenticator);
            
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            
            // Envoi du message
            Transport.send(message);
            System.out.println("✅ Email envoyé avec succès à: " + to);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur générale envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean envoyerEmailPaiementEnLigne(String to, String nomComplet, int idDemande, 
                                                     String natureDocument, double montant, 
                                                     String delai, String lienPaiement) {
        
        String subject = "Paiement en ligne pour votre demande #" + idDemande;
        String body = "Bonjour " + nomComplet + ",\n\n" +
                     "Votre demande de " + natureDocument + " (Référence: #" + idDemande + ") a été validée.\n" +
                     "Montant à payer: " + montant + " FCFA\n" +
                     "Délai de traitement: " + delai + "\n\n" +
                     "Pour procéder au paiement en ligne, cliquez sur le lien suivant:\n" +
                     lienPaiement + "\n\n" +
                     "Moyens de paiement acceptés:\n" +
                     "• Wave\n" +
                     "• Orange Money\n" +
                     "• Carte bancaire (Visa, MasterCard)\n\n" +
                     "Cordialement,\nL'équipe de l'État Civil";
        
        return envoyerEmailSimple(to, subject, body);
    }
    
    public static boolean envoyerEmailRecherche(String to, String nomComplet, int idDemande, String natureDocument) {
        String subject = "Recherche en cours pour votre demande #" + idDemande;
        String body = "Bonjour " + nomComplet + ",\n\n" +
                     "La recherche de votre " + natureDocument + " (Référence: #" + idDemande + ") est en cours.\n" +
                     "Nous vous informerons dès que des résultats seront disponibles.\n\n" +
                     "Cordialement,\nL'équipe de l'État Civil";
        
        return envoyerEmailSimple(to, subject, body);
    }
    
    public static boolean envoyerEmailDocumentPret(String to, String nomComplet, int idDemande, String natureDocument) {
        String subject = "Votre document est prêt - Demande #" + idDemande;
        String body = "Bonjour " + nomComplet + ",\n\n" +
                     "Votre " + natureDocument + " (Référence: #" + idDemande + ") est maintenant prêt.\n" +
                     "Vous pouvez venir le retirer à notre guichet aux horaires d'ouverture.\n\n" +
                     "Cordialement,\nL'équipe de l'État Civil";
        
        return envoyerEmailSimple(to, subject, body);
    }
    
    public static boolean envoyerEmailDemandeTerminee(String to, String nomComplet, int idDemande, String natureDocument) {
        String subject = "Demande terminée - #" + idDemande;
        String body = "Bonjour " + nomComplet + ",\n\n" +
                     "Votre demande de " + natureDocument + " (Référence: #" + idDemande + ") est maintenant terminée.\n" +
                     "Nous vous remercions pour votre confiance.\n\n" +
                     "Cordialement,\nL'équipe de l'État Civil";
        
        return envoyerEmailSimple(to, subject, body);
    }
    
    public static boolean envoyerEmailRejet(String to, String nomComplet, int idDemande, String natureDocument) {
        String subject = "Demande rejetée - #" + idDemande;
        String body = "Bonjour " + nomComplet + ",\n\n" +
                     "Votre demande de " + natureDocument + " (Référence: #" + idDemande + ") a été rejetée.\n" +
                     "Pour plus d'informations, veuillez contacter notre service client.\n\n" +
                     "Cordialement,\nL'équipe de l'État Civil";
        
        return envoyerEmailSimple(to, subject, body);
    }
}