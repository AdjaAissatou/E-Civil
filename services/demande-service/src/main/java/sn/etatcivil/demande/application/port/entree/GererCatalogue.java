package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.Nature;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;

/** Catalogue : natures (familles), variétés de documents et motifs avec leurs pièces à fournir. */
public interface GererCatalogue {
    List<TypeDocument> documents(boolean disponiblesSeulement);

    TypeDocument document(Long id);

    /** Motifs proposés pour cette variété (communs et propres), ou tous les motifs communs si {@code null}. */
    List<Motif> motifs(Long typeDocumentId);

    default List<Motif> motifs() { return motifs(null); }

    /** Tous les motifs, communs et propres à une variété (administration). */
    List<Motif> tousLesMotifs();

    TypeDocument modifier(Long id, int tarif, int delaiJours, boolean actif);

    TypeDocument creerDocument(String code, TypeDocument.Caracteristiques caracteristiques);

    TypeDocument modifierDocument(Long id, TypeDocument.Caracteristiques caracteristiques);

    List<Nature> natures(boolean activesSeulement);

    Nature creerNature(String code, String libelle, String description, int ordre);

    Nature modifierNature(Long id, String libelle, String description, int ordre, boolean active);

    Motif creerMotif(String libelle, int ordre, String piecesRequises, Long typeDocumentId);

    Motif modifierMotif(Long id, String libelle, int ordre, String piecesRequises, Long typeDocumentId);
}
