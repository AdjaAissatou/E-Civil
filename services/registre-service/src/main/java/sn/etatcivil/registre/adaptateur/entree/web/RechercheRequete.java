package sn.etatcivil.registre.adaptateur.entree.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sn.etatcivil.registre.domaine.CriteresRecherche;
import sn.etatcivil.registre.domaine.TypeActe;

import java.time.LocalDate;

public record RechercheRequete(
        @NotNull TypeActe typeActe, @NotBlank String nom, @NotBlank String prenom,
        LocalDate dateNaissance, String lieuNaissance, String nomPere, String prenomPere,
        String nomMere, String prenomMere, String nomConjoint, String prenomConjoint, LocalDate dateEvenement) {

    CriteresRecherche versDomaine() {
        return new CriteresRecherche(typeActe, nom, prenom, dateNaissance, lieuNaissance, nomPere, prenomPere,
                nomMere, prenomMere, nomConjoint, prenomConjoint, dateEvenement);
    }
}
