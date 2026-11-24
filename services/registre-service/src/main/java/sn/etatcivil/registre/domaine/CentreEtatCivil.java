package sn.etatcivil.registre.domaine;

/**
 * Centre d'état civil qui tient les registres. Un centre secondaire est rattaché à un centre principal ;
 * un centre consulaire est situé à l'étranger (pays) au lieu d'une commune.
 */
public record CentreEtatCivil(Long id, String nom, TypeCentre type, String commune, String pays, Long rattachementId,
                              String nomRattachement, boolean actif) {

    /** Vérifie et nettoie les données saisies ; l'identifiant et le nom du rattachement viennent du stockage. */
    public static CentreEtatCivil nouveau(Long id, String nom, TypeCentre type, String commune, String pays,
                                          Long rattachementId, boolean actif) {
        String n = net(nom);
        if (n == null) throw new RegleMetierException("Le nom du centre est obligatoire");
        if (n.length() > 150) throw new RegleMetierException("Le nom du centre ne doit pas dépasser 150 caractères");
        if (type == null) throw new RegleMetierException("Indiquez le type de centre");
        String c = net(commune);
        String p = net(pays);
        if (type == TypeCentre.CONSULAIRE) {
            if (p == null) throw new RegleMetierException("Un centre consulaire indique son pays");
            c = null;
        } else {
            if (c == null) throw new RegleMetierException("Indiquez la commune du centre");
            p = null;
        }
        if (type == TypeCentre.SECONDAIRE && rattachementId == null) {
            throw new RegleMetierException("Un centre secondaire est rattaché à un centre principal");
        }
        if (type != TypeCentre.SECONDAIRE) rattachementId = null;
        if (rattachementId != null && rattachementId.equals(id)) {
            throw new RegleMetierException("Un centre ne peut pas être rattaché à lui-même");
        }
        return new CentreEtatCivil(id, n, type, c, p, rattachementId, null, actif);
    }

    public String localisation() {
        return type == TypeCentre.CONSULAIRE ? pays : commune;
    }

    static String net(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
