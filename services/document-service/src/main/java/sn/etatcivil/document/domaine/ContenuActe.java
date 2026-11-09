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
        String mentionsMarginales, String observations) {

    public boolean estValide() { return "VALIDE".equals(statut); }

    /** Représentation textuelle stable, base du calcul de l'empreinte. */
    String texteCanonique() {
        return String.join("|", Stream.of(typeActe, numeroActe, annee, centre, commune, volume, folio, dateEvenement,
                        lieuEvenement, nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                        prenomMere, nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint, heureDeces,
                        causeDeces, temoin1, temoin2, mentionsMarginales, observations)
                .map(v -> Objects.toString(v, "")).toList());
    }
}
