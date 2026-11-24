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
        DetailsActe details, String observations,
        OrigineActe origine, String referenceOrigine) {

    /** Origine ordinaire par défaut ; pour une naissance, la date et le lieu de naissance sont ceux de l'événement. */
    DonneesActe completer() {
        if (origine == null) {
            return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                    nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere, prenomMere,
                    nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                    heureDeces, causeDeces, temoin1, temoin2, DetailsActe.ou(details), observations,
                    OrigineActe.ORDINAIRE, null).completer();
        }
        if (details == null) {
            return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                    nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere, prenomMere,
                    nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                    heureDeces, causeDeces, temoin1, temoin2, DetailsActe.AUCUN, observations, origine, referenceOrigine)
                    .completer();
        }
        if (typeActe != TypeActe.NAISSANCE) return this;
        String lieu = lieuNaissance == null || lieuNaissance.isBlank() ? lieuEvenement : lieuNaissance;
        return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                nom.trim(), prenom.trim(), sexe, dateEvenement, lieu, nomPere, prenomPere, nomMere, prenomMere,
                nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                heureDeces, causeDeces, temoin1, temoin2, details, observations, origine, referenceOrigine);
    }

    DonneesActe avecObservations(String obs) {
        return new DonneesActe(typeActe, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere, prenomMere,
                nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint,
                heureDeces, causeDeces, temoin1, temoin2, details, obs, origine, referenceOrigine);
    }

    /** Champs qui diffèrent entre ces données et les nouvelles, sous la forme « libellé : avant → après ». */
    public java.util.List<String> differences(DonneesActe nouvelles) {
        java.util.List<String> r = new java.util.ArrayList<>();
        comparer(this, nouvelles, "", r);
        return r;
    }

    private static void comparer(Record avant, Record apres, String prefixe, java.util.List<String> r) {
        for (var c : avant.getClass().getRecordComponents()) {
            try {
                Object a = c.getAccessor().invoke(avant), b = c.getAccessor().invoke(apres);
                if (a instanceof Record ra && b instanceof Record rb) {
                    comparer(ra, rb, prefixe, r);
                } else if (!java.util.Objects.equals(texte(a), texte(b))) {
                    r.add(prefixe + c.getName() + " : " + (texte(a).isEmpty() ? "∅" : texte(a)) + " → "
                            + (texte(b).isEmpty() ? "∅" : texte(b)));
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static String texte(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    void verifierCoherence() {
        if (typeActe == null || dateEvenement == null || vide(centre) || vide(lieuEvenement) || vide(nom) || vide(prenom)) {
            throw new RegleMetierException("Type, centre, date, lieu, nom et prénom sont obligatoires");
        }
        if (dateEvenement.isAfter(LocalDate.now())) {
            throw new RegleMetierException("La date de l'événement ne peut pas être dans le futur");
        }
        if (origine != null && origine != OrigineActe.ORDINAIRE && vide(referenceOrigine)) {
            throw new RegleMetierException(origine == OrigineActe.JUGEMENT_SUPPLETIF
                    ? "Indiquez les références du jugement supplétif (numéro, date, tribunal)"
                    : "Indiquez les références de l'acte étranger transcrit (pays, autorité, date)");
        }
        // une reconnaissance peut précéder la naissance (reconnaissance anticipée)
        if (typeActe != TypeActe.RECONNAISSANCE && dateNaissance != null && dateNaissance.isAfter(dateEvenement)) {
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
            case RECONNAISSANCE -> {
                if (vide(nomPere) && vide(nomMere)) {
                    throw new RegleMetierException("Indiquez le parent qui reconnaît l'enfant (père ou mère)");
                }
            }
        }
    }

    static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
