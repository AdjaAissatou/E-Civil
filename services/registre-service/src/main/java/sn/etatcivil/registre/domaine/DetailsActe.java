package sn.etatcivil.registre.domaine;

/**
 * Énonciations de l'acte sur les personnes : profession et domicile du titulaire, des parents et du conjoint,
 * et, pour un décès, le médecin qui l'a constaté.
 */
public record DetailsActe(String profession, String domicile,
                          String professionPere, String domicilePere,
                          String professionMere, String domicileMere,
                          String professionConjoint, String domicileConjoint,
                          String medecin) {

    public static final DetailsActe AUCUN = new DetailsActe(null, null, null, null, null, null, null, null, null);

    static DetailsActe ou(DetailsActe d) {
        return d == null ? AUCUN : d;
    }
}
