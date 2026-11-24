package sn.etatcivil.habilitation.domaine;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Compte d'agent créé par un administrateur, avec un mot de passe provisoire à changer à la première connexion. */
public record NouvelUtilisateur(String email, String prenom, String nom, Set<String> profils, String motDePasseProvisoire) {

    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public NouvelUtilisateur {
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) throw new RegleMetierException("L'adresse email n'est pas valide");
        prenom = Textes.obligatoire(prenom, "Le prénom", 100);
        nom = Textes.obligatoire(nom, "Le nom", 100);
        if (profils == null || profils.isEmpty()) throw new RegleMetierException("Choisissez au moins un profil");
        MotDePasse.verifier(motDePasseProvisoire, email);
    }
}
