package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.Demande;

import java.util.UUID;

public interface SignerDemandes {
    Demande signer(UUID id, Acteur officier);
}
