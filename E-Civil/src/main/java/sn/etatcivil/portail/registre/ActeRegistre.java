package sn.etatcivil.portail.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sn.etatcivil.portail.catalogue.TypeActe;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Acte tel que renvoyé par le registre central. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActeRegistre(
        Long id, TypeActe typeActe, String numeroActe, Integer annee, String statut,
        String centre, String commune, String volume, String folio,
        LocalDate dateEvenement, String lieuEvenement,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String heureDeces, String causeDeces, String temoin1, String temoin2,
        String mentionsMarginales, String observations,
        String agentSaisie, LocalDateTime dateSaisie, LocalDateTime dateModification) {

    public boolean estValide() { return "VALIDE".equals(statut); }
}
