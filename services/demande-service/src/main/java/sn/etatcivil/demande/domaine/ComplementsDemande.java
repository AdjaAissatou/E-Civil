package sn.etatcivil.demande.domaine;

import java.time.LocalDate;

/**
 * Informations complémentaires de la demande, rubriques de la version 1 demandées selon la variété :
 * professions et domiciles, conjoint, témoins, médecin, décès, mention marginale, précisions.
 */
public record ComplementsDemande(
        String profession, String residence,
        String professionPere, String residencePere,
        String professionMere, String residenceMere,
        LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String professionConjoint, String residenceConjoint,
        String temoin1, String temoin2,
        String medecin, String adresseMedecin,
        String causeDeces, String lieuDeces,
        String typeMention, String detailsMention,
        String precisions) {

    public static final ComplementsDemande AUCUN = new ComplementsDemande(null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null);

    /** Ne garde que les rubriques demandées par la variété, espaces nettoyés. */
    ComplementsDemande pour(TypeDocument type) {
        boolean pro = type.demande(Champ.PROFESSIONS), conj = type.demande(Champ.CONJOINT);
        return new ComplementsDemande(
                pro || type.demande(Champ.DOMICILE) ? t(profession) : null,
                pro || type.demande(Champ.DOMICILE) ? t(residence) : null,
                pro ? t(professionPere) : null, pro ? t(residencePere) : null,
                pro ? t(professionMere) : null, pro ? t(residenceMere) : null,
                conj ? dateNaissanceConjoint : null, conj ? t(lieuNaissanceConjoint) : null,
                conj && pro ? t(professionConjoint) : null, conj && pro ? t(residenceConjoint) : null,
                type.demande(Champ.TEMOINS) ? t(temoin1) : null, type.demande(Champ.TEMOINS) ? t(temoin2) : null,
                type.demande(Champ.MEDECIN) ? t(medecin) : null, type.demande(Champ.MEDECIN) ? t(adresseMedecin) : null,
                type.demande(Champ.DECES) ? t(causeDeces) : null, type.demande(Champ.DECES) ? t(lieuDeces) : null,
                type.demande(Champ.MENTION) ? t(typeMention) : (type.typeMention() != null ? type.typeMention() : null),
                type.demande(Champ.MENTION) ? t(detailsMention) : null,
                type.demande(Champ.PRECISIONS) ? t(precisions) : null);
    }

    void verifier(TypeDocument type, LocalDate aujourdhui) {
        if (type.demande(Champ.DOMICILE) && vide(residence)) {
            throw new RegleMetierException("Indiquez l'adresse du domicile à attester");
        }
        if (type.demande(Champ.TEMOINS) && vide(temoin1)) throw new RegleMetierException("Indiquez au moins un témoin");
        if (type.demande(Champ.MEDECIN) && vide(medecin)) throw new RegleMetierException("Indiquez le médecin qui a constaté le décès");
        if (type.demande(Champ.DECES) && vide(lieuDeces)) throw new RegleMetierException("Indiquez le lieu du décès");
        // rubrique facultative : une mention connue se décrit entièrement (type et détails)
        if (type.demande(Champ.MENTION) && vide(typeMention) != vide(detailsMention)) {
            throw new RegleMetierException("Pour signaler une mention marginale, indiquez son type et ses détails");
        }
        if (type.demande(Champ.PRECISIONS) && vide(precisions)) throw new RegleMetierException("Apportez les précisions demandées");
        if (dateNaissanceConjoint != null && dateNaissanceConjoint.isAfter(aujourdhui)) {
            throw new RegleMetierException("La date de naissance du conjoint ne peut pas être dans le futur");
        }
        for (String s : new String[]{profession, residence, professionPere, residencePere, professionMere, residenceMere,
                lieuNaissanceConjoint, professionConjoint, residenceConjoint, temoin1, temoin2, medecin, adresseMedecin,
                causeDeces, lieuDeces, typeMention}) {
            if (s != null && s.length() > 255) throw new RegleMetierException("Une information dépasse 255 caractères");
        }
        if (detailsMention != null && detailsMention.length() > 1000) throw new RegleMetierException("Les détails de la mention dépassent 1000 caractères");
        if (precisions != null && precisions.length() > 1000) throw new RegleMetierException("Les précisions dépassent 1000 caractères");
    }

    private static boolean vide(String s) { return s == null || s.isBlank(); }

    private static String t(String s) { return vide(s) ? null : s.trim(); }
}
