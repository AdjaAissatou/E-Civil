package sn.etatcivil.demande.adaptateur.entree.web;

import sn.etatcivil.demande.domaine.ComplementsDemande;

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
        @PastOrPresent LocalDate dateEvenement,
        @Size(max = 255) String profession,
        @Size(max = 255) String residence,
        @Size(max = 255) String professionPere,
        @Size(max = 255) String residencePere,
        @Size(max = 255) String professionMere,
        @Size(max = 255) String residenceMere,
        @PastOrPresent LocalDate dateNaissanceConjoint,
        @Size(max = 255) String lieuNaissanceConjoint,
        @Size(max = 255) String professionConjoint,
        @Size(max = 255) String residenceConjoint,
        @Size(max = 255) String temoin1,
        @Size(max = 255) String temoin2,
        @Size(max = 255) String medecin,
        @Size(max = 255) String adresseMedecin,
        @Size(max = 255) String causeDeces,
        @Size(max = 255) String lieuDeces,
        @Size(max = 255) String typeMention,
        @Size(max = 1000) String detailsMention,
        @Size(max = 1000) String precisions) {

    DemandeADeposer versCommande() {
        return new DemandeADeposer(typeDocumentId, motifId, precisionMotif, lien, format, adresseLivraison, villeLivraison,
                new PersonneConcernee(nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                        prenomMere, nomConjoint, prenomConjoint, dateEvenement),
                new ComplementsDemande(profession, residence, professionPere, residencePere, professionMere, residenceMere, dateNaissanceConjoint, lieuNaissanceConjoint, professionConjoint, residenceConjoint, temoin1, temoin2, medecin, adresseMedecin, causeDeces, lieuDeces, typeMention, detailsMention, precisions));
    }
}
