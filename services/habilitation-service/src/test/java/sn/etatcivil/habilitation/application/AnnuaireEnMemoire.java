package sn.etatcivil.habilitation.application;

import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIdentites;
import sn.etatcivil.habilitation.domaine.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Imite Keycloak : rôles du realm (simples ou composites), groupes, rôles par défaut, comptes. */
public class AnnuaireEnMemoire implements AnnuaireIdentites {

    public final Map<String, Permission> permissions = new LinkedHashMap<>();
    public final Map<String, Role> roles = new LinkedHashMap<>();
    /** Rôles présents dans le realm sans être marqués (état d'un realm d'avant la migration). */
    public final Set<String> rolesBruts = new HashSet<>();
    public final Map<String, Profil> profils = new LinkedHashMap<>();
    public final Map<String, Utilisateur> comptes = new LinkedHashMap<>();
    public final Map<String, Set<String>> rolesDirectsParCompte = new HashMap<>();
    public final Map<String, Set<String>> profilsParCompte = new HashMap<>();
    public final Set<String> rolesParDefaut = new HashSet<>(Set.of("CITOYEN", "offline_access"));
    public String profilParDefaut;
    public final List<String> deconnectes = new ArrayList<>();
    private int compteur;

    public String ajouterCompte(String email, boolean actif, String... rolesDirects) {
        String id = "u" + (++compteur);
        comptes.put(id, new Utilisateur(id, email, "Prénom", "Nom", actif, Instant.now(), List.of()));
        Set<String> r = new HashSet<>(Set.of("default-roles-ecivil"));
        r.addAll(List.of(rolesDirects));
        rolesDirectsParCompte.put(id, r);
        profilsParCompte.put(id, new HashSet<>());
        if (profilParDefaut != null) profilsParCompte.get(id).add(profilParDefaut); // comme les groupes par défaut de Keycloak
        return id;
    }

    @Override public List<Permission> permissions() { return List.copyOf(permissions.values()); }
    @Override public void enregistrerPermission(Permission p) { permissions.put(p.code(), p); }
    @Override public List<Role> roles() { return roles.values().stream().map(this::copie).toList(); }
    @Override public Optional<Role> role(String code) { return Optional.ofNullable(roles.get(code)).map(this::copie); }
    @Override public boolean nomDeRoleUtilise(String code) {
        return roles.containsKey(code) || permissions.containsKey(code) || rolesBruts.contains(code);
    }
    @Override public void creerRole(Role r) { roles.put(r.code(), copie(r)); }
    @Override public void modifierRole(Role r) { rolesBruts.remove(r.code()); roles.put(r.code(), copie(r)); }
    @Override public void supprimerRole(String code) { roles.remove(code); }

    @Override public List<Profil> profils() { return profils.values().stream().map(this::copie).toList(); }
    @Override public Optional<Profil> profil(String id) { return Optional.ofNullable(profils.get(id)).map(this::copie); }
    @Override public String creerProfil(Profil p) {
        String id = "g" + (++compteur);
        profils.put(id, new Profil(id, p.nom(), p.description(), p.roles(), p.parDefaut()));
        if (p.parDefaut()) profilParDefaut = id;
        return id;
    }
    @Override public void modifierProfil(Profil p) { profils.put(p.id(), copie(p)); }
    @Override public void supprimerProfil(String id) { profils.remove(id); }
    @Override public List<Utilisateur> membres(String profilId) {
        return profilsParCompte.entrySet().stream().filter(e -> e.getValue().contains(profilId))
                .map(e -> utilisateur(e.getKey()).orElseThrow()).toList();
    }
    @Override public void definirProfilParDefaut(String profilId) { profilParDefaut = profilId; }

    @Override public PageResultat<Utilisateur> utilisateurs(String recherche, PageDemandee page) {
        List<Utilisateur> tous = comptes.keySet().stream().map(id -> utilisateur(id).orElseThrow())
                .filter(u -> recherche == null || u.email().contains(recherche)).toList();
        return new PageResultat<>(tous.stream().skip((long) page.numero() * page.taille()).limit(page.taille()).toList(),
                page.numero(), page.taille(), tous.size());
    }
    @Override public Optional<Utilisateur> utilisateur(String id) {
        Utilisateur u = comptes.get(id);
        if (u == null) return Optional.empty();
        List<ProfilResume> p = profilsParCompte.get(id).stream().map(g -> new ProfilResume(g, profils.get(g).nom())).toList();
        return Optional.of(new Utilisateur(u.id(), u.email(), u.prenom(), u.nom(), u.actif(), u.dateCreation(), p));
    }
    @Override public boolean emailUtilise(String email) { return comptes.values().stream().anyMatch(u -> u.email().equals(email)); }
    @Override public String creerUtilisateur(NouvelUtilisateur n) { return ajouterCompte(n.email(), true); }
    @Override public void activer(String id, boolean actif) {
        Utilisateur u = comptes.get(id);
        comptes.put(id, new Utilisateur(u.id(), u.email(), u.prenom(), u.nom(), actif, u.dateCreation(), List.of()));
    }
    @Override public void motDePasseProvisoire(String id, String motDePasse) {}
    @Override public void ajouterAuProfil(String u, String p) { profilsParCompte.get(u).add(p); }
    @Override public void retirerDuProfil(String u, String p) { profilsParCompte.get(u).remove(p); }
    @Override public Set<String> rolesEffectifs(String u) {
        Set<String> r = new HashSet<>(rolesDirectsParCompte.get(u));
        if (r.contains("default-roles-ecivil")) r.addAll(rolesParDefaut);
        profilsParCompte.get(u).forEach(g -> r.addAll(profils.get(g).roles()));
        Set<String> effectifs = new HashSet<>(r);
        r.forEach(x -> { if (roles.containsKey(x)) effectifs.addAll(roles.get(x).permissions()); });
        return effectifs;
    }
    @Override public void deconnecter(String u) { deconnectes.add(u); }

    @Override public List<String> utilisateursAvecRoleDirect(String role) {
        return rolesDirectsParCompte.entrySet().stream().filter(e -> e.getValue().contains(role)).map(Map.Entry::getKey)
                .sorted().toList();
    }
    @Override public Set<String> rolesDirects(String u) { return Set.copyOf(rolesDirectsParCompte.get(u)); }
    @Override public void retirerRolesDirects(String u, Set<String> r) { rolesDirectsParCompte.get(u).removeAll(r); }
    @Override public void retirerDesRolesParDefaut(String role) { rolesParDefaut.remove(role); }
    @Override public String roleParDefaut() { return "default-roles-ecivil"; }

    public Set<String> permissionsDe(String u) {
        return rolesEffectifs(u).stream().filter(Catalogue.CODES_PERMISSIONS::contains).collect(Collectors.toSet());
    }

    public String idProfil(String nom) {
        return profils.values().stream().filter(p -> p.nom().equals(nom)).map(Profil::id).findFirst().orElseThrow();
    }

    private Role copie(Role r) { return new Role(r.code(), r.libelle(), r.description(), r.permissions(), r.deBase()); }
    private Profil copie(Profil p) {
        return new Profil(p.id(), p.nom(), p.description(), p.roles(), p.id() != null && p.id().equals(profilParDefaut));
    }
}
