package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;

public interface GererCatalogue {
    List<TypeDocument> documents(boolean disponiblesSeulement);

    TypeDocument document(Long id);

    List<Motif> motifs();

    TypeDocument modifier(Long id, int tarif, int delaiJours, boolean actif);
}
