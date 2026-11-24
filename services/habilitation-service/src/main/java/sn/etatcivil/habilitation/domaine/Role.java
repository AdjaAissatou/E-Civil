package sn.etatcivil.habilitation.domaine;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Fonction dans l'organisation ; regroupe des permissions. Il devient un rôle composite dans Keycloak. */
public class Role {

    private static final Pattern CODE = Pattern.compile("[A-Z][A-Z0-9_]{2,39}");

    private final String code;
    private String libelle;
    private String description;
    private Set<String> permissions;
    private final boolean deBase;

    public Role(String code, String libelle, String description, Set<String> permissions, boolean deBase) {
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.permissions = new TreeSet<>(permissions);
        this.deBase = deBase;
    }

    /** Nouveau rôle créé par l'administrateur : le code est saisi en majuscules (ex. AGENT_ACCUEIL). */
    public static Role creer(String code, String libelle, String description, Set<String> permissions) {
        String c = code == null ? "" : code.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        if (!CODE.matcher(c).matches()) {
            throw new RegleMetierException("Le code du rôle doit contenir de 3 à 40 lettres majuscules, chiffres ou « _ », "
                    + "et commencer par une lettre (ex. AGENT_ACCUEIL)");
        }
        if (Catalogue.CODES_RESERVES.contains(c) || c.startsWith("DEFAULT_ROLES")) {
            throw new RegleMetierException("Le code " + c + " est réservé par la plateforme");
        }
        if (Catalogue.CODES_PERMISSIONS.contains(c)) {
            throw new RegleMetierException("Le code " + c + " est celui d'une permission : choisissez un autre code");
        }
        Role r = new Role(c, "", null, Set.of(), false);
        r.modifier(libelle, description, permissions);
        return r;
    }

    public void modifier(String libelle, String description, Set<String> permissions) {
        this.libelle = Textes.obligatoire(libelle, "Le libellé du rôle", 80);
        this.description = Textes.facultatif(description, 300);
        if (permissions == null || permissions.isEmpty()) {
            throw new RegleMetierException("Un rôle doit accorder au moins une permission");
        }
        for (String p : permissions) {
            if (!Catalogue.CODES_PERMISSIONS.contains(p)) throw new RegleMetierException("Permission inconnue : " + p);
        }
        this.permissions = new TreeSet<>(permissions);
    }

    public void verifierSuppressible() {
        if (deBase) throw new RegleMetierException("Le rôle " + libelle + " est un rôle de base de la plateforme : il ne peut pas être supprimé");
    }

    public String code() { return code; }
    public String libelle() { return libelle; }
    public String description() { return description; }
    public Set<String> permissions() { return Collections.unmodifiableSet(permissions); }
    public boolean deBase() { return deBase; }
}
