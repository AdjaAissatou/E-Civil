package EtatCivil.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hasher le mot de passe avant de l'enregistrer en BD
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Vérifier le mot de passe entré avec celui en BD
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    
}
