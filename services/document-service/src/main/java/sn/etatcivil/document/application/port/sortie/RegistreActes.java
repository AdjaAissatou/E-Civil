package sn.etatcivil.document.application.port.sortie;

import sn.etatcivil.document.domaine.ContenuActe;

import java.util.UUID;

public interface RegistreActes {
    /** Contenu actuel de l'acte dans le registre national. */
    ContenuActe contenu(UUID acteId);
}
