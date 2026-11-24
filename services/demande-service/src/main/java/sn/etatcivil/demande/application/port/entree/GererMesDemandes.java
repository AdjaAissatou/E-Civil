package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.demande.domaine.Demandeur;

import java.util.List;
import java.util.UUID;

/** Cas d'usage du citoyen. */
public interface GererMesDemandes {
    default Demande deposer(DemandeADeposer commande, Demandeur demandeur) {
        return deposer(commande, demandeur, sn.etatcivil.demande.domaine.PiecesDeposees.AUCUNE);
    }

    /** Dépôt avec la pièce d'identité et le selfie, pour les documents qui exigent la vérification d'identité. */
    Demande deposer(DemandeADeposer commande, Demandeur demandeur, sn.etatcivil.demande.domaine.PiecesDeposees pieces);

    void annuler(UUID id, String demandeurId);

    List<Demande> mesDemandes(String demandeurId);
}
