package sn.etatcivil.demande.domaine;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Personne ou système à l'origine d'une action, tracée dans l'historique.
 * @param fonction      libellé affiché dans l'historique (Délégué, Officier de l'état civil…)
 * @param habilitations ce que l'acteur a le droit de faire
 */
public record Acteur(String id, String nomComplet, String fonction, Set<Habilitation> habilitations) {

    public static final Acteur SYSTEME = new Acteur("systeme", "Système E-Civil", "Système",
            EnumSet.allOf(Habilitation.class));

    public Acteur {
        Objects.requireNonNull(id);
        habilitations = habilitations == null || habilitations.isEmpty()
                ? Set.of() : Set.copyOf(habilitations);
    }

    /** Le demandeur lui-même, pour ses propres demandes. */
    public static Acteur citoyen(Demandeur d) {
        return new Acteur(d.id(), d.nomComplet(), "Citoyen", Set.of(Habilitation.DEMANDES_DEPOSER));
    }

    public boolean peut(Habilitation h) {
        return habilitations.contains(h);
    }

    public String signature() {
        return this == SYSTEME || "systeme".equals(id) ? nomComplet : nomComplet + " (" + fonction + ")";
    }
}
