package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Acteur;

import java.util.UUID;

/** Cas d'usage du délégué (et rejet par les agents habilités). */
public interface InstruireDemandes {
    default void valider(UUID id, Acteur delegue) {
        valider(id, delegue, false);
    }

    /** @param identiteVerifiee le délégué a comparé le selfie à la pièce d'identité */
    void valider(UUID id, Acteur delegue, boolean identiteVerifiee);

    void rejeter(UUID id, String motif, Acteur agent);

    void marquerLivree(UUID id, Acteur delegue);
}
