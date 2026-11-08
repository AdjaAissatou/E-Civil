package sn.etatcivil.demande.domaine;

import java.time.LocalDate;

/** Personne dont l'acte est demandé ; ces informations servent à retrouver l'acte dans le registre. */
public record PersonneConcernee(
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateEvenement) {

    public String nomComplet() { return prenom + " " + nom; }

    /** Nettoie les espaces et ne garde que les informations pertinentes pour ce type d'acte. */
    PersonneConcernee pour(TypeActe type) {
        return new PersonneConcernee(t(nom), t(prenom), t(sexe), dateNaissance, t(lieuNaissance),
                t(nomPere), t(prenomPere), t(nomMere), t(prenomMere),
                type.concerneUnCouple() ? t(nomConjoint) : null, type.concerneUnCouple() ? t(prenomConjoint) : null,
                type == TypeActe.NAISSANCE ? null : dateEvenement);
    }

    void verifier(TypeActe type, LocalDate aujourdhui) {
        if (vide(nom) || vide(prenom)) throw new RegleMetierException("Le nom et le prénom sont obligatoires");
        if (dateNaissance != null && (dateNaissance.isAfter(aujourdhui) || dateNaissance.isBefore(aujourdhui.minusYears(130)))) {
            throw new RegleMetierException("La date de naissance semble erronée");
        }
        if (dateEvenement != null && dateEvenement.isAfter(aujourdhui)) {
            throw new RegleMetierException("La date de l'événement ne peut pas être dans le futur");
        }
        switch (type) {
            case NAISSANCE -> {
                if (dateNaissance == null || vide(lieuNaissance)) {
                    throw new RegleMetierException("La date et le lieu de naissance sont obligatoires");
                }
            }
            case MARIAGE, DIVORCE -> {
                if (vide(nomConjoint) || vide(prenomConjoint)) {
                    throw new RegleMetierException("Le nom et le prénom du conjoint sont obligatoires");
                }
            }
            case DECES -> {
                if (dateEvenement == null) throw new RegleMetierException("La date du décès est obligatoire");
            }
        }
    }

    static boolean vide(String s) { return s == null || s.isBlank(); }

    static String t(String s) { return vide(s) ? null : s.trim(); }
}
