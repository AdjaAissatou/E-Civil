package sn.etatcivil.habilitation.domaine;

/** Contrôle des textes saisis par l'administrateur. */
final class Textes {
    private Textes() {}

    static String obligatoire(String valeur, String nom, int max) {
        if (valeur == null || valeur.isBlank()) throw new RegleMetierException(nom + " est obligatoire");
        String v = valeur.trim();
        if (v.length() > max) throw new RegleMetierException(nom + " ne doit pas dépasser " + max + " caractères");
        return v;
    }

    static String facultatif(String valeur, int max) {
        if (valeur == null || valeur.isBlank()) return null;
        String v = valeur.trim();
        if (v.length() > max) throw new RegleMetierException("Un texte ne doit pas dépasser " + max + " caractères");
        return v;
    }
}
