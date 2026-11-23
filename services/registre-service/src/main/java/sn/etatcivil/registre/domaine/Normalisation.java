package sn.etatcivil.registre.domaine;

import java.text.Normalizer;
import java.util.Locale;

/** Forme comparable d'un nom : sans accents, en majuscules, ponctuation et espaces unifiés. */
public final class Normalisation {
    private Normalisation() {}

    public static String normaliser(String valeur) {
        if (valeur == null) return null;
        String sansAccents = Normalizer.normalize(valeur, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sansAccents.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    public static boolean equivalents(String a, String b) {
        return a != null && b != null && normaliser(a).equals(normaliser(b));
    }

    /** « Dakar » est compatible avec « Hôpital Principal, Dakar » : un lieu peut être plus ou moins précis. */
    public static boolean lieuxCompatibles(String a, String b) {
        String x = " " + normaliser(a) + " ";
        String y = " " + normaliser(b) + " ";
        return x.contains(y) || y.contains(x);
    }
}
