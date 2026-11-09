package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Acteur;

import java.util.UUID;

/** Cas d'usage du délégué (et rejet par les agents habilités). */
public interface InstruireDemandes {
    void valider(UUID id, Acteur delegue);

    void rejeter(UUID id, String motif, Acteur agent);

    void marquerLivree(UUID id, Acteur delegue);
}
