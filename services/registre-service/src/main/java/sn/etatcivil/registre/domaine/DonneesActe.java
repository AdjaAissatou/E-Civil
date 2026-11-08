package sn.etatcivil.registre.domaine;

import java.time.LocalDate;

/** Contenu d'un acte tel qu'il figure dans le registre papier. */
public record DonneesActe(
        TypeActe typeActe,
        String centre, String commune, String volume, String folio,
        LocalDate dateEvenement, String lieuEvenement,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String heureDeces, String causeDeces, String temoin1, String temoin2,
        String mentionsMarginales, String observations) {

    /** Pour une naissance, la date et le lieu de naissance sont ceux de l'événement. */
    DonneesActe completer() {
        if (typeActe != TypeActe.NAISSANCE) return this;
        String lieu = lieuNaissance == null || lieuNaissance.isBlank() ? lieuEvenement : lieuNaissance;
        return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                nom.trim(), prenom.trim(), sexe, dateEvenement, lieu, nomPere, prenomPere, nomMere, prenomMere,
                nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                heureDeces, causeDeces, temoin1, temoin2, mentionsMarginales, observations);
    }

    DonneesActe avecMentions(String mentions, String obs) {
        return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere, prenomMere,
                nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                heureDeces, causeDeces, temoin1, temoin2, mentions, obs);
    }

    void verifierCoherence() {
        if (typeActe == null || dateEvenement == null || vide(centre) || vide(lieuEvenement) || vide(nom) || vide(prenom)) {
            throw new RegleMetierException("Type, centre, date, lieu, nom et prénom sont obligatoires");
        }
        if (dateEvenement.isAfter(LocalDate.now())) {
            throw new RegleMetierException("La date de l'événement ne peut pas être dans le futur");
        }
        if (dateNaissance != null && dateNaissance.isAfter(dateEvenement)) {
            throw new RegleMetierException("La date de naissance ne peut pas être postérieure à l'événement");
        }
        switch (typeActe) {
            case NAISSANCE -> {
                if (vide(sexe)) throw new RegleMetierException("Le sexe de l'enfant est obligatoire");
                if (dateNaissance != null && !dateNaissance.equals(dateEvenement)) {
                    throw new RegleMetierException("Pour une naissance, la date de naissance est la date de l'événement");
                }
            }
            case MARIAGE, DIVORCE -> {
                if (vide(nomConjoint) || vide(prenomConjoint)) {
                    throw new RegleMetierException("Le nom et le prénom du conjoint sont obligatoires");
                }
            }
            case DECES -> { /* pas de règle supplémentaire */ }
        }
    }

    static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
