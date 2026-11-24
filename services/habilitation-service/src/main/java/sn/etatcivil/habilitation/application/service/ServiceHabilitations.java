package sn.etatcivil.habilitation.application.service;

import sn.etatcivil.habilitation.application.CasDUsage;
import sn.etatcivil.habilitation.application.port.entree.*;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIdentites;
import sn.etatcivil.habilitation.application.port.sortie.Horloge;
import sn.etatcivil.habilitation.application.port.sortie.JournalOperations;
import sn.etatcivil.habilitation.domaine.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cas d'usage de l'administration des habilitations. Chaque modification est d'abord simulée sur une
 * {@link Organisation} pour vérifier qu'elle ne retire pas l'administration de la plateforme, puis appliquée
 * dans l'annuaire et inscrite au journal.
 */
@CasDUsage
public class ServiceHabilitations implements GererUtilisateurs, GererProfils, GererRoles, GererPermissions, ConsulterJournal {

    private final AnnuaireIdentites annuaire;
    private final JournalOperations journal;
    private final Horloge horloge;

    public ServiceHabilitations(AnnuaireIdentites annuaire, JournalOperations journal, Horloge horloge) {
        this.annuaire = annuaire;
        this.journal = journal;
        this.horloge = horloge;
    }

    // ================================================================== utilisateurs

    @Override
    public PageResultat<Utilisateur> utilisateurs(String recherche, PageDemandee page) {
        return annuaire.utilisateurs(recherche == null ? null : recherche.trim(), page);
    }

    @Override
    public FicheUtilisateur fiche(String id) {
        Utilisateur u = compte(id);
        Set<String> effectifs = annuaire.rolesEffectifs(id);
        List<Role> roles = annuaire.roles().stream().filter(r -> effectifs.contains(r.code())).toList();
        List<Permission> permissions = annuaire.permissions().stream().filter(p -> effectifs.contains(p.code())).toList();
        return new FicheUtilisateur(u, roles, permissions, journal.concernant(u.email(), 20));
    }

    @Override
    public String creerUtilisateur(NouvelUtilisateur nouveau, Administrateur auteur) {
        Map<String, Profil> profils = profilsParId();
        for (String p : nouveau.profils()) {
            if (!profils.containsKey(p)) throw new RegleMetierException("Profil inconnu");
        }
        if (annuaire.emailUtilise(nouveau.email())) {
            throw new RegleMetierException("Un compte existe déjà avec l'adresse " + nouveau.email());
        }
        String id = annuaire.creerUtilisateur(nouveau);
        nouveau.profils().forEach(p -> annuaire.ajouterAuProfil(id, p));
        // Keycloak place tout nouveau compte dans le profil par défaut (Citoyen) : seuls les profils choisis restent
        annuaire.utilisateur(id).ifPresent(u -> u.profils().stream().map(ProfilResume::id)
                .filter(p -> !nouveau.profils().contains(p)).forEach(p -> annuaire.retirerDuProfil(id, p)));
        tracer(auteur, TypeOperation.UTILISATEUR_CREE, nouveau.email(),
                nouveau.prenom() + " " + nouveau.nom() + " — profils : " + noms(nouveau.profils(), profils));
        return id;
    }

    @Override
    public void activer(String id, boolean actif, Administrateur auteur) {
        Utilisateur u = compte(id);
        if (!actif && id.equals(auteur.id())) {
            throw new RegleMetierException("Vous ne pouvez pas désactiver votre propre compte");
        }
        if (u.actif() == actif) return;
        organisation().avecCompteActif(id, actif).verifierAdministrationPreservee(auteur);
        annuaire.activer(id, actif);
        if (!actif) annuaire.deconnecter(id);
        tracer(auteur, actif ? TypeOperation.UTILISATEUR_ACTIVE : TypeOperation.UTILISATEUR_DESACTIVE, u.email(), u.nomComplet());
    }

    @Override
    public void reinitialiserMotDePasse(String id, String motDePasse, Administrateur auteur) {
        Utilisateur u = compte(id);
        MotDePasse.verifier(motDePasse, u.email());
        annuaire.motDePasseProvisoire(id, motDePasse);
        annuaire.deconnecter(id);
        tracer(auteur, TypeOperation.MOT_DE_PASSE_REINITIALISE, u.email(),
                "Mot de passe provisoire, à changer à la prochaine connexion");
    }

    @Override
    public void definirProfils(String id, Set<String> profils, Administrateur auteur) {
        Utilisateur u = compte(id);
        Map<String, Profil> tous = profilsParId();
        Set<String> voulus = profils == null ? Set.of() : new LinkedHashSet<>(profils);
        if (voulus.isEmpty()) throw new RegleMetierException("Un compte doit avoir au moins un profil");
        for (String p : voulus) if (!tous.containsKey(p)) throw new RegleMetierException("Profil inconnu");
        Set<String> actuels = u.profils().stream().map(ProfilResume::id).collect(Collectors.toSet());
        if (actuels.equals(voulus)) return;
        organisation().avecProfilsDe(id, voulus).verifierAdministrationPreservee(auteur);
        voulus.stream().filter(p -> !actuels.contains(p)).forEach(p -> annuaire.ajouterAuProfil(id, p));
        actuels.stream().filter(p -> !voulus.contains(p)).forEach(p -> annuaire.retirerDuProfil(id, p));
        annuaire.deconnecter(id);
        tracer(auteur, TypeOperation.PROFILS_MODIFIES, u.email(),
                noms(actuels, tous) + " → " + noms(voulus, tous));
    }

    // ================================================================== profils

    @Override
    public List<ProfilDetaille> profils() {
        return annuaire.profils().stream()
                .sorted(Comparator.comparing((Profil p) -> !p.parDefaut()).thenComparing(Profil::nom))
                .map(p -> new ProfilDetaille(p, annuaire.membres(p.id())))
                .toList();
    }

    @Override
    public ProfilDetaille profil(String id) {
        Profil p = annuaire.profil(id).orElseThrow(() -> new RegleMetierException("Profil introuvable"));
        return new ProfilDetaille(p, annuaire.membres(id));
    }

    @Override
    public String creerProfil(String nom, String description, Set<String> roles, Administrateur auteur) {
        Profil p = Profil.creer(nom, description, roles);
        verifierRolesExistants(p.roles());
        verifierNomLibre(p.nom(), null);
        String id = annuaire.creerProfil(p);
        tracer(auteur, TypeOperation.PROFIL_CREE, p.nom(), "Rôles : " + String.join(", ", p.roles()));
        return id;
    }

    @Override
    public void modifierProfil(String id, String nom, String description, Set<String> roles, Administrateur auteur) {
        Profil p = annuaire.profil(id).orElseThrow(() -> new RegleMetierException("Profil introuvable"));
        String avant = p.nom() + " (" + String.join(", ", p.roles()) + ")";
        p.modifier(nom, description, roles);
        verifierRolesExistants(p.roles());
        verifierNomLibre(p.nom(), id);
        organisation().avecProfil(id, p.roles()).verifierAdministrationPreservee(auteur);
        annuaire.modifierProfil(p);
        annuaire.membres(id).forEach(m -> annuaire.deconnecter(m.id()));
        tracer(auteur, TypeOperation.PROFIL_MODIFIE, p.nom(), avant + " → " + p.nom() + " (" + String.join(", ", p.roles()) + ")");
    }

    @Override
    public void supprimerProfil(String id, Administrateur auteur) {
        Profil p = annuaire.profil(id).orElseThrow(() -> new RegleMetierException("Profil introuvable"));
        p.verifierSuppressible(annuaire.membres(id).size());
        annuaire.supprimerProfil(id);
        tracer(auteur, TypeOperation.PROFIL_SUPPRIME, p.nom(), null);
    }

    // ================================================================== rôles

    @Override
    public List<RoleDetaille> roles() {
        List<Profil> profils = annuaire.profils();
        return annuaire.roles().stream()
                .sorted(Comparator.comparing((Role r) -> !r.deBase()).thenComparing(Role::libelle))
                .map(r -> new RoleDetaille(r, profils.stream().filter(p -> p.roles().contains(r.code())).toList()))
                .toList();
    }

    @Override
    public RoleDetaille role(String code) {
        Role r = annuaire.role(code).orElseThrow(() -> new RegleMetierException("Rôle introuvable"));
        return new RoleDetaille(r, annuaire.profils().stream().filter(p -> p.roles().contains(code)).toList());
    }

    @Override
    public String creerRole(String code, String libelle, String description, Set<String> permissions, Administrateur auteur) {
        Role r = Role.creer(code, libelle, description, permissions);
        if (annuaire.nomDeRoleUtilise(r.code())) throw new RegleMetierException("Le code " + r.code() + " est déjà utilisé");
        annuaire.creerRole(r);
        tracer(auteur, TypeOperation.ROLE_CREE, r.code(), r.libelle() + " — " + r.permissions().size() + (r.permissions().size() > 1 ? " permissions" : " permission"));
        return r.code();
    }

    @Override
    public void modifierRole(String code, String libelle, String description, Set<String> permissions, Administrateur auteur) {
        Role r = annuaire.role(code).orElseThrow(() -> new RegleMetierException("Rôle introuvable"));
        Set<String> avant = r.permissions();
        r.modifier(libelle, description, permissions);
        organisation().avecRole(code, r.permissions()).verifierAdministrationPreservee(auteur);
        annuaire.modifierRole(r);
        deconnecterLesPorteurs(Set.of(code));
        tracer(auteur, TypeOperation.ROLE_MODIFIE, code, difference(avant, r.permissions()));
    }

    @Override
    public void supprimerRole(String code, Administrateur auteur) {
        Role r = annuaire.role(code).orElseThrow(() -> new RegleMetierException("Rôle introuvable"));
        r.verifierSuppressible();
        List<String> utilisateurs = annuaire.profils().stream().filter(p -> p.roles().contains(code)).map(Profil::nom).toList();
        if (!utilisateurs.isEmpty()) {
            throw new RegleMetierException("Le rôle est encore utilisé par : " + String.join(", ", utilisateurs)
                    + ". Retirez-le d'abord de ces profils");
        }
        annuaire.supprimerRole(code);
        tracer(auteur, TypeOperation.ROLE_SUPPRIME, code, r.libelle());
    }

    @Override
    public void modifierMatrice(Map<String, Set<String>> permissionsParRole, Administrateur auteur) {
        Map<String, Role> roles = annuaire.roles().stream().collect(Collectors.toMap(Role::code, r -> r));
        Organisation simulee = organisation();
        List<Role> changes = new ArrayList<>();
        Map<String, Set<String>> avant = new HashMap<>();
        for (var e : permissionsParRole.entrySet()) {
            Role r = roles.get(e.getKey());
            if (r == null) throw new RegleMetierException("Rôle introuvable : " + e.getKey());
            Set<String> nouvelles = e.getValue() == null ? Set.of() : e.getValue();
            if (r.permissions().equals(nouvelles)) continue;
            avant.put(r.code(), r.permissions());
            r.modifier(r.libelle(), r.description(), nouvelles);
            simulee = simulee.avecRole(r.code(), r.permissions());
            changes.add(r);
        }
        if (changes.isEmpty()) return;
        simulee.verifierAdministrationPreservee(auteur);
        for (Role r : changes) {
            annuaire.modifierRole(r);
            tracer(auteur, TypeOperation.ROLE_MODIFIE, r.code(), difference(avant.get(r.code()), r.permissions()));
        }
        deconnecterLesPorteurs(changes.stream().map(Role::code).collect(Collectors.toSet()));
    }

    // ================================================================== permissions

    @Override
    public List<Permission> permissions() {
        Map<String, Permission> parCode = annuaire.permissions().stream().collect(Collectors.toMap(Permission::code, p -> p));
        // ordre et familles du catalogue de l'application
        return Catalogue.PERMISSIONS.stream().map(p -> parCode.getOrDefault(p.code(), p)).toList();
    }

    @Override
    public void modifierPermission(String code, String libelle, String description, String avantage, String interdiction,
                         Administrateur auteur) {
        Permission p = permissions().stream().filter(x -> x.code().equals(code)).findFirst()
                .orElseThrow(() -> new RegleMetierException("Permission inconnue"));
        Permission nouvelle = p.avecTextes(libelle, description, avantage, interdiction);
        annuaire.enregistrerPermission(nouvelle);
        tracer(auteur, TypeOperation.PERMISSION_MODIFIEE, code, nouvelle.libelle());
    }

    // ================================================================== journal

    @Override
    public PageResultat<Operation> journal(String recherche, PageDemandee page) {
        return journal.lire(recherche == null || recherche.isBlank() ? null : recherche.trim(), page);
    }

    @Override
    public Synthese synthese() {
        return new Synthese(annuaire.utilisateurs(null, new PageDemandee(0, 1)).total(), annuaire.profils().size(),
                annuaire.roles().size(), Catalogue.PERMISSIONS.size(), journal.lire(null, new PageDemandee(0, 8)).contenu());
    }

    // ================================================================== interne

    /** Photographie actuelle des habilitations, pour simuler une modification avant de l'appliquer. */
    private Organisation organisation() {
        Map<String, Set<String>> permissionsParRole = annuaire.roles().stream()
                .collect(Collectors.toMap(Role::code, Role::permissions));
        Map<String, Set<String>> rolesParProfil = new HashMap<>();
        Map<String, Set<String>> membresParProfil = new HashMap<>();
        Set<String> inactifs = new HashSet<>();
        for (Profil p : annuaire.profils()) {
            rolesParProfil.put(p.id(), p.roles());
            // le profil des citoyens peut compter des milliers de membres : inutile de les lire s'il ne donne
            // aucun droit d'administration
            if (p.parDefaut() && !confereAdministration(p, permissionsParRole)) continue;
            List<Utilisateur> membres = annuaire.membres(p.id());
            membresParProfil.put(p.id(), membres.stream().map(Utilisateur::id).collect(Collectors.toSet()));
            membres.stream().filter(m -> !m.actif()).forEach(m -> inactifs.add(m.id()));
        }
        return new Organisation(permissionsParRole, rolesParProfil, membresParProfil, inactifs);
    }

    private static boolean confereAdministration(Profil p, Map<String, Set<String>> permissionsParRole) {
        return p.roles().stream().flatMap(r -> permissionsParRole.getOrDefault(r, Set.of()).stream())
                .anyMatch(Catalogue.ADMINISTRATION::contains);
    }

    private void deconnecterLesPorteurs(Set<String> roles) {
        annuaire.profils().stream().filter(p -> !p.parDefaut())
                .filter(p -> p.roles().stream().anyMatch(roles::contains))
                .flatMap(p -> annuaire.membres(p.id()).stream()).map(Utilisateur::id).distinct()
                .forEach(annuaire::deconnecter);
    }

    private Utilisateur compte(String id) {
        return annuaire.utilisateur(id).orElseThrow(() -> new RegleMetierException("Compte introuvable"));
    }

    private Map<String, Profil> profilsParId() {
        return annuaire.profils().stream().collect(Collectors.toMap(Profil::id, p -> p, (a, b) -> a, LinkedHashMap::new));
    }

    private void verifierRolesExistants(Set<String> roles) {
        Set<String> connus = annuaire.roles().stream().map(Role::code).collect(Collectors.toSet());
        for (String r : roles) if (!connus.contains(r)) throw new RegleMetierException("Rôle inconnu : " + r);
    }

    private void verifierNomLibre(String nom, String idCourant) {
        boolean pris = annuaire.profils().stream()
                .anyMatch(p -> p.nom().equalsIgnoreCase(nom) && !p.id().equals(idCourant));
        if (pris) throw new RegleMetierException("Un profil porte déjà le nom « " + nom + " »");
    }

    private static String noms(Set<String> ids, Map<String, Profil> profils) {
        return ids.isEmpty() ? "aucun" : ids.stream().map(i -> profils.containsKey(i) ? profils.get(i).nom() : i)
                .sorted().collect(Collectors.joining(", "));
    }

    private static String difference(Set<String> avant, Set<String> apres) {
        List<String> ajoutees = apres.stream().filter(p -> !avant.contains(p)).sorted().toList();
        List<String> retirees = avant.stream().filter(p -> !apres.contains(p)).sorted().toList();
        String d = (ajoutees.isEmpty() ? "" : "+ " + String.join(", ", ajoutees))
                + (retirees.isEmpty() ? "" : (ajoutees.isEmpty() ? "" : " ; ") + "− " + String.join(", ", retirees));
        return d.isEmpty() ? "Textes modifiés" : d;
    }

    private void tracer(Administrateur auteur, TypeOperation type, String cible, String detail) {
        journal.enregistrer(new Operation(horloge.maintenant(), auteur.signature(), type, cible, detail));
    }
}
