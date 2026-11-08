package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.demande.domaine.Demandeur;

import java.util.List;
import java.util.UUID;

/** Cas d'usage du citoyen. */
public interface GererMesDemandes {
    Demande deposer(DemandeADeposer commande, Demandeur demandeur);

    void annuler(UUID id, String demandeurId);

    List<Demande> mesDemandes(String demandeurId);
}
