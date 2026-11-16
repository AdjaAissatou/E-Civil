package sn.etatcivil.document.domaine;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

/** Copie de l'acte du registre au moment de la signature. C'est exactement ce qui est imprimé et vérifiable. */
public record ContenuActe(
        String typeActe, String numeroActe, int annee, String statut,
        String centre, String commune, String volume, String folio,
        LocalDate dateEvenement, String lieuEvenement,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String heureDeces, String causeDeces, String temoin1, String temoin2,
        String mentionsMarginales, String observations,
        String origine, String referenceOrigine) {

    public boolean estValide() { return "VALIDE".equals(statut); }

    /** Acte dressé par jugement supplétif ou transcrit d'un acte étranger. */
    public boolean origineParticuliere() {
        return origine != null && !"ORDINAIRE".equals(origine);
    }

    /**
     * Représentation textuelle stable, base du calcul de l'empreinte. L'origine n'y est ajoutée que lorsqu'elle
     * est connue, pour que l'empreinte des documents signés avant son introduction reste identique.
     */
    String texteCanonique() {
        String base = String.join("|", Stream.of(typeActe, numeroActe, annee, centre, commune, volume, folio, dateEvenement,
                        lieuEvenement, nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                        prenomMere, nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint, heureDeces,
                        causeDeces, temoin1, temoin2, mentionsMarginales, observations)
                .map(v -> Objects.toString(v, "")).toList());
        return origine == null && referenceOrigine == null ? base
                : base + "|" + Objects.toString(origine, "") + "|" + Objects.toString(referenceOrigine, "");
    }
}
