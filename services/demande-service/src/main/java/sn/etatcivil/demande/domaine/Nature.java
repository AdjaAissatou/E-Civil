package sn.etatcivil.demande.domaine;

import java.util.Locale;
import java.util.regex.Pattern;

/** Famille de documents (Naissance, Mariage, Célibat, Copie…) qui regroupe des variétés. */
public class Nature {

    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{1,39}");

    private final Long id;
    private final String code;
    private String libelle;
    private String description;
    private int ordre;
    private boolean active;

    public Nature(Long id, String code, String libelle, String description, int ordre, boolean active) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.ordre = ordre;
        this.active = active;
    }

    public static Nature creer(String code, String libelle, String description, int ordre) {
        String c = code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (!CODE.matcher(c).matches()) {
            throw new RegleMetierException("Le code de la nature : 2 à 40 lettres majuscules, chiffres ou « _ » (ex. NAISSANCE)");
        }
        Nature n = new Nature(null, c, "", null, 0, true);
        n.modifier(libelle, description, ordre, true);
        return n;
    }

    public void modifier(String libelle, String description, int ordre, boolean active) {
        if (libelle == null || libelle.isBlank()) throw new RegleMetierException("Le libellé de la nature est obligatoire");
        if (libelle.trim().length() > 100) throw new RegleMetierException("Le libellé ne doit pas dépasser 100 caractères");
        if (description != null && description.length() > 500) throw new RegleMetierException("La description ne doit pas dépasser 500 caractères");
        if (ordre < 0 || ordre > 999) throw new RegleMetierException("L'ordre d'affichage doit être compris entre 0 et 999");
        this.libelle = libelle.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.ordre = ordre;
        this.active = active;
    }

    public Long id() { return id; }
    public String code() { return code; }
    public String libelle() { return libelle; }
    public String description() { return description; }
    public int ordre() { return ordre; }
    public boolean active() { return active; }
}
