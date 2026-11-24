package sn.etatcivil.document.domaine;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ce que la personne a déclaré dans sa demande et que le document atteste : identité (documents établis sur
 * comparution, sans acte), profession, domicile, témoins, précisions (coutume, nationalité d'origine…).
 */
public record Declaration(String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                          String nomPere, String prenomPere, String nomMere, String prenomMere,
                          String profession, String residence, String temoin1, String temoin2, String precisions) {

    String texteCanonique() {
        return String.join(",", Stream.of(nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere,
                prenomMere, profession, residence, temoin1, temoin2, precisions).map(v -> Objects.toString(v, "")).toList());
    }
}
