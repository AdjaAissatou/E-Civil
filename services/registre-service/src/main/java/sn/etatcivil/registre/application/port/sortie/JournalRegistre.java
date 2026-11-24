package sn.etatcivil.registre.application.port.sortie;

import sn.etatcivil.registre.domaine.EntreeJournal;

import java.util.List;
import java.util.UUID;

public interface JournalRegistre {
    void enregistrer(EntreeJournal entree);

    List<EntreeJournal> pourActe(UUID acteId);
}
