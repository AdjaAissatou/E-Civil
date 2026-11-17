package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.constraints.*;
import sn.etatcivil.demande.application.port.entree.DemandeADeposer;
import sn.etatcivil.demande.domaine.FormatDocument;
import sn.etatcivil.demande.domaine.LienDemandeur;
import sn.etatcivil.demande.domaine.PersonneConcernee;

import java.time.LocalDate;

public record DepotRequete(
        @NotNull Long typeDocumentId,
        @NotNull Long motifId,
        @Size(max = 255) String precisionMotif,
        @NotNull LienDemandeur lien,
        @NotNull FormatDocument format,
        @Size(max = 255) String adresseLivraison,
        @Size(max = 100) String villeLivraison,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 150) String prenom,
        @Pattern(regexp = "^$|[MF]") String sexe,
        @PastOrPresent LocalDate dateNaissance,
        @Size(max = 150) String lieuNaissance,
        @Size(max = 100) String nomPere,
        @Size(max = 150) String prenomPere,
        @Size(max = 100) String nomMere,
        @Size(max = 150) String prenomMere,
        @Size(max = 100) String nomConjoint,
        @Size(max = 150) String prenomConjoint,
        @PastOrPresent LocalDate dateEvenement) {

    DemandeADeposer versCommande() {
        return new DemandeADeposer(typeDocumentId, motifId, precisionMotif, lien, format, adresseLivraison, villeLivraison,
                new PersonneConcernee(nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                        prenomMere, nomConjoint, prenomConjoint, dateEvenement));
    }
}
