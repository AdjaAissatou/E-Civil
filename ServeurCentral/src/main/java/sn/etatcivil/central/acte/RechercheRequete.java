package sn.etatcivil.central.acte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Critères transmis par le portail lorsqu'un citoyen a payé sa demande.
 * Nom, prénom et type sont obligatoires ; chaque critère facultatif fourni doit concorder
 * avec l'acte (s'il y est renseigné) pour que celui-ci soit retenu.
 */
public record RechercheRequete(
        @NotNull TypeActe typeActe,
        @NotBlank String nom,
        @NotBlank String prenom,
        LocalDate dateNaissance,
        String lieuNaissance,
        String nomPere,
        String prenomPere,
        String nomMere,
        String prenomMere,
        String nomConjoint,
        String prenomConjoint,
        LocalDate dateEvenement) {
}
