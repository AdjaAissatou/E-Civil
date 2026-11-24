package sn.etatcivil.habilitation.domaine;

/**
 * Droit élémentaire vérifié par les services sur chaque action. Le code est fixé par l'application (il est
 * vérifié dans le code des services) ; l'administrateur peut seulement en préciser les textes.
 */
public record Permission(String code, String libelle, String description, String avantage, String interdiction,
                         String famille) {

    public Permission {
        if (code == null || code.isBlank()) throw new RegleMetierException("Le code de la permission est obligatoire");
    }

    /** Nouveaux textes, mêmes code et famille. */
    public Permission avecTextes(String libelle, String description, String avantage, String interdiction) {
        String l = Textes.obligatoire(libelle, "Le libellé de la permission", 100);
        return new Permission(code, l, Textes.facultatif(description, 300), Textes.facultatif(avantage, 300),
                Textes.facultatif(interdiction, 300), famille);
    }
}
