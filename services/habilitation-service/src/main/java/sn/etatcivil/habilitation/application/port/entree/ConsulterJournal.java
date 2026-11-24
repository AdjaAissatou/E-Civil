package sn.etatcivil.habilitation.application.port.entree;

import sn.etatcivil.habilitation.domaine.Operation;

import java.util.List;

public interface ConsulterJournal {

    record Synthese(long utilisateurs, int profils, int roles, int permissions, List<Operation> dernieresOperations) {}

    PageResultat<Operation> journal(String recherche, PageDemandee page);

    Synthese synthese();
}
