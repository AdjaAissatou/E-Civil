package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.ComplementsDemande;

import sn.etatcivil.demande.domaine.FormatDocument;
import sn.etatcivil.demande.domaine.LienDemandeur;
import sn.etatcivil.demande.domaine.PersonneConcernee;

public record DemandeADeposer(Long typeDocumentId, Long motifId, String precisionMotif, LienDemandeur lien,
                              FormatDocument format, String adresseLivraison, String villeLivraison,
                              PersonneConcernee personne, ComplementsDemande complements) {

    public DemandeADeposer(Long typeDocumentId, Long motifId, String precisionMotif, LienDemandeur lien,
                           FormatDocument format, String adresseLivraison, String villeLivraison, PersonneConcernee personne) {
        this(typeDocumentId, motifId, precisionMotif, lien, format, adresseLivraison, villeLivraison, personne,
                ComplementsDemande.AUCUN);
    }
}
