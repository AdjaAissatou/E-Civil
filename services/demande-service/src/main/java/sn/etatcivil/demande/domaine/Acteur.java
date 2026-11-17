package sn.etatcivil.demande.domaine;

import java.util.Objects;

/** Personne ou système à l'origine d'une action, tracée dans l'historique. */
public record Acteur(String id, String nomComplet, Role role) {

    public static final Acteur SYSTEME = new Acteur("systeme", "Système E-Civil", Role.ADMIN);

    public Acteur {
        Objects.requireNonNull(role);
    }

    public String signature() {
        return this == SYSTEME || "systeme".equals(id) ? nomComplet : nomComplet + " (" + role.libelle() + ")";
    }
}
