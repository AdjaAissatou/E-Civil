package sn.etatcivil.habilitation.domaine;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Situation d'une personne dans l'organisation (Délégué de commune, Chef de centre…) ; regroupe des rôles.
 * Il devient un groupe dans Keycloak : ses membres reçoivent les rôles du profil, donc leurs permissions.
 */
public class Profil {

    private final String id;
    private String nom;
    private String description;
    private Set<String> roles;
    private final boolean parDefaut;

    public Profil(String id, String nom, String description, Set<String> roles, boolean parDefaut) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.roles = new TreeSet<>(roles);
        this.parDefaut = parDefaut;
    }

    public static Profil creer(String nom, String description, Set<String> roles) {
        Profil p = new Profil(null, "", null, Set.of(), false);
        p.modifier(nom, description, roles);
        return p;
    }

    public void modifier(String nom, String description, Set<String> roles) {
        String n = Textes.obligatoire(nom, "Le nom du profil", 60);
        if (n.contains("/")) throw new RegleMetierException("Le nom du profil ne peut pas contenir « / »");
        this.nom = n;
        this.description = Textes.facultatif(description, 300);
        if (roles == null || roles.isEmpty()) throw new RegleMetierException("Un profil doit comporter au moins un rôle");
        this.roles = new TreeSet<>(roles);
    }

    public void verifierSuppressible(int nombreMembres) {
        if (parDefaut) {
            throw new RegleMetierException("Le profil " + nom + " est attribué à chaque nouvelle inscription : il ne peut pas être supprimé");
        }
        if (nombreMembres > 0) {
            throw new RegleMetierException("Le profil " + nom + " compte encore " + nombreMembres
                    + (nombreMembres > 1 ? " membres" : " membre") + " : changez d'abord leur profil");
        }
    }

    public String id() { return id; }
    public String nom() { return nom; }
    public String description() { return description; }
    public Set<String> roles() { return Collections.unmodifiableSet(roles); }
    public boolean parDefaut() { return parDefaut; }
}
