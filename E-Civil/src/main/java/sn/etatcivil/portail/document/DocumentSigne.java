package sn.etatcivil.portail.document;

import sn.etatcivil.portail.registre.ActeRegistre;

import java.time.LocalDateTime;

/** Contenu figé au moment de la signature ; c'est exactement ce que le PDF et la page de vérification affichent. */
public record DocumentSigne(
        String reference,
        String libelleDocument,
        boolean copieIntegrale,
        ActeRegistre acte,
        String signataire,
        LocalDateTime dateSignature,
        String codeVerification) {
}
