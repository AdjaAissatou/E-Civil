package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;
import java.util.Optional;

public interface CatalogueRepository {
    List<TypeDocument> documents(boolean disponiblesSeulement);

    Optional<TypeDocument> document(Long id);

    void enregistrer(TypeDocument document);

    List<Motif> motifs();

    Optional<Motif> motif(Long id);
}
