package sn.etatcivil.habilitation.domaine;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Informations personnelles d'un compte (inscription de la version 1) et préférences : notifications par email,
 * langue du portail. Le citoyen les consulte et les modifie depuis « Mon compte ».
 */
public record ProfilPersonnel(String prenom, String nom, String email, String telephone, String sexe,
                              LocalDate dateNaissance, String lieuNaissance, String profession, String residence,
                              String localite, Integer nombreEnfants, String numeroCni, String typeUtilisateur,
                              String paysResidence, boolean notifications, String langue) {

    public static final Set<String> LANGUES = Set.of("fr", "wo", "en");
    private static final Pattern TELEPHONE = Pattern.compile("\\+?[0-9 ().-]{7,20}");

    public ProfilPersonnel {
        prenom = Textes.obligatoire(prenom, "Le prénom", 100);
        nom = Textes.obligatoire(nom, "Le nom", 100);
        telephone = Textes.facultatif(telephone, 20);
        if (telephone != null && !TELEPHONE.matcher(telephone).matches()) {
            throw new RegleMetierException("Le numéro de téléphone n'est pas valide (ex. +221 77 123 45 67)");
        }
        sexe = Textes.facultatif(sexe, 1);
        if (sexe != null && !sexe.equals("M") && !sexe.equals("F")) throw new RegleMetierException("Sexe : M ou F");
        if (dateNaissance != null && (dateNaissance.isAfter(LocalDate.now()) || dateNaissance.isBefore(LocalDate.now().minusYears(130)))) {
            throw new RegleMetierException("La date de naissance semble erronée");
        }
        lieuNaissance = Textes.facultatif(lieuNaissance, 150);
        profession = Textes.facultatif(profession, 150);
        residence = Textes.facultatif(residence, 255);
        localite = Textes.facultatif(localite, 150);
        if (nombreEnfants != null && (nombreEnfants < 0 || nombreEnfants > 50)) {
            throw new RegleMetierException("Le nombre d'enfants doit être compris entre 0 et 50");
        }
        // un numéro au bon format est rangé sans séparateurs ; un ancien numéro mal saisi reste lisible
        // (la vérification complète a lieu à la modification, voir verifierNumeroCni)
        String cni = NumeroIdentification.nettoyer(Textes.facultatif(numeroCni, 30));
        numeroCni = cni != null && NumeroIdentification.normaliser(cni, LocalDate.now().getYear()).isPresent() ? cni
                : Textes.facultatif(numeroCni, 30);
        typeUtilisateur = typeUtilisateur == null || typeUtilisateur.isBlank() ? "RESIDENT" : typeUtilisateur.trim().toUpperCase(Locale.ROOT);
        if (!typeUtilisateur.equals("RESIDENT") && !typeUtilisateur.equals("DIASPORA")) {
            throw new RegleMetierException("Type d'utilisateur : résident ou diaspora");
        }
        paysResidence = Textes.facultatif(paysResidence, 150);
        if (typeUtilisateur.equals("DIASPORA") && paysResidence == null) {
            throw new RegleMetierException("Indiquez votre pays de résidence");
        }
        langue = langue == null || langue.isBlank() ? "fr" : langue.trim().toLowerCase(Locale.ROOT);
        if (!LANGUES.contains(langue)) throw new RegleMetierException("Langue proposée : français, wolof ou anglais");
    }

    /** Le NIN saisi respecte le format national et correspond au sexe déclaré. */
    public void verifierNumeroCni(int anneeCourante) {
        NumeroIdentification.verifier(numeroCni, sexe, anneeCourante);
    }
}
