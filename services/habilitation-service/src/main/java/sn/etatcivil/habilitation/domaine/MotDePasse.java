package sn.etatcivil.habilitation.domaine;

import java.util.Locale;

/** Règles des mots de passe provisoires (alignées sur la politique du realm Keycloak). */
public final class MotDePasse {
    private MotDePasse() {}

    public static void verifier(String motDePasse, String email) {
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new RegleMetierException("Le mot de passe provisoire doit contenir au moins 8 caractères");
        }
        if (motDePasse.length() > 64) throw new RegleMetierException("Le mot de passe ne doit pas dépasser 64 caractères");
        if (email != null && !email.isBlank() && motDePasse.toLowerCase(Locale.ROOT).equals(email.toLowerCase(Locale.ROOT))) {
            throw new RegleMetierException("Le mot de passe ne peut pas être l'adresse email");
        }
    }
}
