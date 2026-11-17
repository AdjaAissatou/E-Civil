package sn.etatcivil.registre.domaine;

import java.time.LocalDate;

/** Une personne telle qu'elle peut être désignée dans un acte. */
public record Personne(String nom, String prenom, LocalDate dateNaissance) {

    public Personne {
        if (DonneesActe.vide(nom) || DonneesActe.vide(prenom)) {
            throw new RegleMetierException("Le nom et le prénom sont obligatoires");
        }
    }
}
