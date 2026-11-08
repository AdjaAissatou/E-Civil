package sn.etatcivil.document.application.port.sortie;

import sn.etatcivil.document.domaine.DocumentDelivre;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {
    void enregistrer(DocumentDelivre document);

    Optional<DocumentDelivre> parDemande(UUID demandeId);

    Optional<DocumentDelivre> parCode(String code);
}
