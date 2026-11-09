package sn.etatcivil.portail.registre;

import sn.etatcivil.portail.catalogue.TypeActe;

import java.time.LocalDate;

public record CriteresRecherche(
        TypeActe typeActe, String nom, String prenom, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateEvenement) {
}
