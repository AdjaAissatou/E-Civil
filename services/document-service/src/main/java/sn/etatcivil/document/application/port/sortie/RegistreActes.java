package sn.etatcivil.document.application.port.sortie;

import sn.etatcivil.document.domaine.Complements;
import sn.etatcivil.document.domaine.ContenuActe;

import java.util.List;
import java.util.UUID;

public interface RegistreActes {
    /** Contenu actuel de l'acte dans le registre national. */
    ContenuActe contenu(UUID acteId);

    /** Naissances inscrites des enfants du couple de cet acte de mariage. */
    List<Complements.Enfant> enfants(ContenuActe mariage);

    /** Texte des mentions de ce type (DIVORCE, LEGITIMATION…) portées en marge de l'acte. */
    List<String> mentions(UUID acteId, String type);
}
