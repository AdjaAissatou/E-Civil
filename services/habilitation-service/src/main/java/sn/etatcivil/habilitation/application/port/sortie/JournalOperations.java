package sn.etatcivil.habilitation.application.port.sortie;

import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.domaine.Operation;

import java.util.List;

/** Journal des opérations d'administration, en ajout seul. */
public interface JournalOperations {
    void enregistrer(Operation operation);

    PageResultat<Operation> lire(String recherche, PageDemandee page);

    List<Operation> concernant(String cible, int nombre);
}
