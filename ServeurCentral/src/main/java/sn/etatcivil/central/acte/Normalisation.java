package sn.etatcivil.central.acte;

import java.text.Normalizer;
import java.util.Locale;

/** Met un nom sous une forme comparable : sans accents, majuscules, espaces et tirets unifiés. */
public final class Normalisation {
    private Normalisation() {}

    public static String normaliser(String valeur) {
        if (valeur == null) return null;
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static boolean equivalents(String a, String b) {
        return a != null && b != null && normaliser(a).equals(normaliser(b));
    }
}
