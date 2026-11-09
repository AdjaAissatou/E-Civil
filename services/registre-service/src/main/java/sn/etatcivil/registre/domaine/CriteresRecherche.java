package sn.etatcivil.registre.domaine;

import java.time.LocalDate;

/** Critères transmis lors d'une demande d'acte. Les critères facultatifs servent à écarter les homonymes. */
public record CriteresRecherche(
        TypeActe typeActe, String nom, String prenom, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateEvenement) {

    public CriteresRecherche {
        if (typeActe == null || DonneesActe.vide(nom) || DonneesActe.vide(prenom)) {
            throw new RegleMetierException("Le type d'acte, le nom et le prénom sont obligatoires pour une recherche");
        }
    }
}
