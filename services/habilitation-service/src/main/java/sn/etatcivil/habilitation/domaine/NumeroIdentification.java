package sn.etatcivil.habilitation.domaine;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Numéro d'identification nationale (NIN) porté par la carte d'identité sénégalaise : 13 caractères,
 * 14 avec l'indice de différenciation.
 * <ul>
 *     <li>1 chiffre pour le sexe : 1 homme, 2 femme ;</li>
 *     <li>3 caractères pour le code du centre d'état civil (le premier peut être une lettre, ex. G01) ;</li>
 *     <li>4 chiffres pour l'année d'enregistrement ;</li>
 *     <li>5 chiffres pour le numéro d'ordre ;</li>
 *     <li>1 chiffre facultatif : indice de différenciation.</li>
 * </ul>
 * La saisie tolère espaces, points et tirets ; le numéro est conservé sans séparateurs.
 */
public final class NumeroIdentification {

    private static final Pattern FORMAT = Pattern.compile("([12])([0-9A-Z][0-9]{2})((?:19|20)[0-9]{2})([0-9]{5})([0-9]?)");

    /** Même règle pour Keycloak (formulaire d'inscription), séparateurs compris. */
    public static final String MOTIF_SAISIE = "^\\s*[12][\\s.-]?[0-9A-Za-z][0-9]{2}[\\s.-]?(19|20)[0-9]{2}[\\s.-]?[0-9]{5}[0-9]?\\s*$";

    public static final String EXPLICATION = "Le numéro d'identification nationale (NIN) compte 13 chiffres : 1 ou 2 (sexe), "
            + "le code du centre (3 caractères), l'année (4 chiffres) puis le numéro d'ordre (5 chiffres). "
            + "Ex. 2 018 2018 83088";

    private NumeroIdentification() {}

    /** Numéro sans séparateurs, en majuscules ; {@code null} si rien n'est saisi. */
    public static String nettoyer(String saisie) {
        if (saisie == null || saisie.isBlank()) return null;
        return saisie.replaceAll("[\\s.-]", "").toUpperCase(Locale.ROOT);
    }

    /** Numéro normalisé, ou vide s'il ne respecte pas le format. */
    public static Optional<String> normaliser(String saisie, int anneeCourante) {
        String n = nettoyer(saisie);
        if (n == null) return Optional.empty();
        Matcher m = FORMAT.matcher(n);
        if (!m.matches() || Integer.parseInt(m.group(3)) > anneeCourante) return Optional.empty();
        return Optional.of(n);
    }

    /**
     * Vérifie le numéro saisi et sa cohérence avec le sexe déclaré.
     * @return le numéro normalisé, ou {@code null} si rien n'est saisi
     */
    public static String verifier(String saisie, String sexe, int anneeCourante) {
        if (nettoyer(saisie) == null) return null;
        String n = normaliser(saisie, anneeCourante).orElseThrow(() -> new RegleMetierException(EXPLICATION));
        if (sexe != null && !sexe.equals(n.charAt(0) == '1' ? "M" : "F")) {
            throw new RegleMetierException("Le premier chiffre du NIN indique le sexe (1 homme, 2 femme) : "
                    + "il ne correspond pas au sexe indiqué");
        }
        return n;
    }

    /** Présentation lisible : 2 018 2018 83088. */
    public static String presenter(String numero) {
        Matcher m = numero == null ? null : FORMAT.matcher(numero);
        if (m == null || !m.matches()) return numero;
        return m.group(1) + " " + m.group(2) + " " + m.group(3) + " " + m.group(4) + (m.group(5).isEmpty() ? "" : " " + m.group(5));
    }
}
