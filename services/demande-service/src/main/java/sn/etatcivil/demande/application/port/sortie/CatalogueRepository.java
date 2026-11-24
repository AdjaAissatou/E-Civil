package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.Nature;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;
import java.util.Optional;

public interface CatalogueRepository {
    List<TypeDocument> documents(boolean disponiblesSeulement);

    Optional<TypeDocument> document(Long id);

    Optional<TypeDocument> documentParCode(String code);

    /** Crée ou met à jour la variété ; @return son identifiant */
    Long enregistrer(TypeDocument document);

    List<Nature> natures();

    Optional<Nature> nature(Long id);

    Optional<Nature> natureParCode(String code);

    Long enregistrer(Nature nature);

    List<Motif> motifs();

    Optional<Motif> motif(Long id);

    Long enregistrer(Motif motif);
}
