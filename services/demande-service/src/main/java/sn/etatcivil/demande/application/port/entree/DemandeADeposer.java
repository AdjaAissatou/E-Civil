package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.FormatDocument;
import sn.etatcivil.demande.domaine.LienDemandeur;
import sn.etatcivil.demande.domaine.PersonneConcernee;

public record DemandeADeposer(Long typeDocumentId, Long motifId, String precisionMotif, LienDemandeur lien,
                              FormatDocument format, String adresseLivraison, String villeLivraison,
                              PersonneConcernee personne) {
}
