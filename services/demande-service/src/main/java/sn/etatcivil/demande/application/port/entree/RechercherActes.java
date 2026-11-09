package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.demande.domaine.TypeActe;

import java.util.List;
import java.util.UUID;

/** Recherche de l'acte dans le registre national, automatique ou par un agent. */
public interface RechercherActes {
    Demande rechercher(UUID id, Acteur auteur);

    Demande rattacher(UUID id, String numeroActe, Acteur agent);

    void contester(UUID id, String raison, Acteur officier);

    /** Demandes payées dont la recherche n'a pas encore abouti (registre injoignable). */
    List<UUID> enAttenteDeRecherche();

    /** Demandes dont l'acte de ce type n'avait pas été retrouvé : à relancer quand un acte est validé. */
    List<UUID> introuvables(TypeActe type);
}
