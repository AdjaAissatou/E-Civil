package sn.etatcivil.central.acte;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Données envoyées par un agent pour transcrire (ou corriger) un acte du registre papier. */
public record ActeRequete(
        @NotNull TypeActe typeActe,
        @NotBlank @Size(max = 150) String centre,
        @Size(max = 150) String commune,
        @Size(max = 30) String volume,
        @Size(max = 30) String folio,
        @NotNull @PastOrPresent LocalDate dateEvenement,
        @NotBlank @Size(max = 150) String lieuEvenement,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 150) String prenom,
        @Pattern(regexp = "[MF]") String sexe,
        @PastOrPresent LocalDate dateNaissance,
        @Size(max = 150) String lieuNaissance,
        @Size(max = 100) String nomPere,
        @Size(max = 150) String prenomPere,
        @Size(max = 100) String nomMere,
        @Size(max = 150) String prenomMere,
        @Size(max = 100) String nomConjoint,
        @Size(max = 150) String prenomConjoint,
        @PastOrPresent LocalDate dateNaissanceConjoint,
        @Size(max = 150) String lieuNaissanceConjoint,
        @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d") String heureDeces,
        @Size(max = 255) String causeDeces,
        @Size(max = 200) String temoin1,
        @Size(max = 200) String temoin2,
        @Size(max = 2000) String mentionsMarginales,
        @Size(max = 2000) String observations,
        @NotBlank @Size(max = 150) String agent) {
}
