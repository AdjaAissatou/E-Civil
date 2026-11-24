package sn.etatcivil.habilitation.domaine;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Photographie des habilitations (profils, rôles, membres, comptes actifs), sur laquelle on simule une
 * modification avant de l'appliquer. Elle porte la règle de sûreté : une opération ne doit jamais laisser la
 * plateforme sans administrateur actif, ni retirer à l'administrateur qui agit ses propres droits d'administration.
 */
public final class Organisation {

    private final Map<String, Set<String>> permissionsParRole;
    private final Map<String, Set<String>> rolesParProfil;
    private final Map<String, Set<String>> membresParProfil;
    private final Set<String> comptesInactifs;

    public Organisation(Map<String, Set<String>> permissionsParRole, Map<String, Set<String>> rolesParProfil,
                        Map<String, Set<String>> membresParProfil, Set<String> comptesInactifs) {
        this.permissionsParRole = copie(permissionsParRole);
        this.rolesParProfil = copie(rolesParProfil);
        this.membresParProfil = copie(membresParProfil);
        this.comptesInactifs = new HashSet<>(comptesInactifs);
    }

    // ------------------------------------------------------------------ simulations

    public Organisation avecRole(String code, Set<String> permissions) {
        Organisation o = copie();
        o.permissionsParRole.put(code, new HashSet<>(permissions));
        return o;
    }

    public Organisation avecProfil(String id, Set<String> roles) {
        Organisation o = copie();
        o.rolesParProfil.put(id, new HashSet<>(roles));
        return o;
    }

    public Organisation avecProfilsDe(String utilisateur, Set<String> profils) {
        Organisation o = copie();
        o.membresParProfil.values().forEach(m -> m.remove(utilisateur));
        for (String p : profils) o.membresParProfil.computeIfAbsent(p, k -> new HashSet<>()).add(utilisateur);
        return o;
    }

    public Organisation avecCompteActif(String utilisateur, boolean actif) {
        Organisation o = copie();
        if (actif) o.comptesInactifs.remove(utilisateur);
        else o.comptesInactifs.add(utilisateur);
        return o;
    }

    // ------------------------------------------------------------------ lecture

    public Set<String> permissionsDuProfil(String profil) {
        return rolesParProfil.getOrDefault(profil, Set.of()).stream()
                .flatMap(r -> permissionsParRole.getOrDefault(r, Set.of()).stream())
                .collect(Collectors.toSet());
    }

    public Set<String> permissionsDe(String utilisateur) {
        return membresParProfil.entrySet().stream()
                .filter(e -> e.getValue().contains(utilisateur))
                .flatMap(e -> permissionsDuProfil(e.getKey()).stream())
                .collect(Collectors.toSet());
    }

    public boolean estAdministrateur(String utilisateur) {
        return !comptesInactifs.contains(utilisateur) && permissionsDe(utilisateur).containsAll(Catalogue.ADMINISTRATION);
    }

    public Set<String> administrateursActifs() {
        return membresParProfil.values().stream().flatMap(Set::stream).distinct()
                .filter(this::estAdministrateur).collect(Collectors.toSet());
    }

    /** Règle de sûreté, vérifiée sur l'organisation qui résulterait de l'opération. */
    public void verifierAdministrationPreservee(Administrateur auteur) {
        if (administrateursActifs().isEmpty()) {
            throw new RegleMetierException("Opération refusée : la plateforme n'aurait plus aucun administrateur actif "
                    + "(gestion des utilisateurs et des habilitations)");
        }
        if (auteur != null && !estAdministrateur(auteur.id())) {
            throw new RegleMetierException("Opération refusée : vous perdriez vous-même vos droits d'administration. "
                    + "Demandez à un autre administrateur de la faire");
        }
    }

    private Organisation copie() {
        return new Organisation(permissionsParRole, rolesParProfil, membresParProfil, comptesInactifs);
    }

    private static Map<String, Set<String>> copie(Map<String, Set<String>> m) {
        Map<String, Set<String>> c = new HashMap<>();
        m.forEach((k, v) -> c.put(k, new HashSet<>(v)));
        return c;
    }
}
