package sn.etatcivil.habilitation.application.service;

import sn.etatcivil.habilitation.application.CasDUsage;
import sn.etatcivil.habilitation.application.port.entree.InitialiserHabilitations;
import sn.etatcivil.habilitation.application.port.sortie.AnnuaireIdentites;
import sn.etatcivil.habilitation.application.port.sortie.Horloge;
import sn.etatcivil.habilitation.application.port.sortie.JournalOperations;
import sn.etatcivil.habilitation.application.port.sortie.MarqueurInitialisation;
import sn.etatcivil.habilitation.domaine.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Met en place les habilitations au démarrage, sur un realm neuf comme sur un realm déjà en service :
 * <ol>
 *   <li>crée les permissions et les rôles de base qui manquent (à chaque démarrage, sans toucher à l'existant) ;</li>
 *   <li>une seule fois : donne aux rôles de base leurs permissions, crée les profils de départ, range chaque
 *       compte existant dans le profil qui correspond à ses rôles, puis fait du profil « Citoyen » le profil des
 *       nouvelles inscriptions.</li>
 * </ol>
 */
@CasDUsage
public class ServiceInitialisation implements InitialiserHabilitations {

    static final String ETAPE_MIGRATION = "profils-et-permissions-v1";
    private static final Administrateur SYSTEME = new Administrateur("systeme", "Système E-Civil", "systeme");

    private final AnnuaireIdentites annuaire;
    private final MarqueurInitialisation marqueur;
    private final JournalOperations journal;
    private final Horloge horloge;

    public ServiceInitialisation(AnnuaireIdentites annuaire, MarqueurInitialisation marqueur, JournalOperations journal,
                                 Horloge horloge) {
        this.annuaire = annuaire;
        this.marqueur = marqueur;
        this.journal = journal;
        this.horloge = horloge;
    }

    @Override
    public void initialiser() {
        Set<String> permissionsExistantes = annuaire.permissions().stream().map(Permission::code).collect(Collectors.toSet());
        Catalogue.PERMISSIONS.stream().filter(p -> !permissionsExistantes.contains(p.code()))
                .forEach(annuaire::enregistrerPermission);

        boolean premiere = !marqueur.estFaite(ETAPE_MIGRATION);
        Set<String> rolesConnus = annuaire.roles().stream().map(Role::code).collect(Collectors.toSet());
        for (Catalogue.RoleDeBase base : Catalogue.ROLES_DE_BASE) {
            Role r = new Role(base.code(), base.libelle(), base.description(), base.permissions(), true);
            if (!annuaire.nomDeRoleUtilise(base.code())) annuaire.creerRole(r);
            else if (premiere || !rolesConnus.contains(base.code())) annuaire.modifierRole(r);
        }
        if (!premiere) return;

        Map<String, String> profils = new HashMap<>();
        annuaire.profils().forEach(p -> profils.put(p.nom(), p.id()));
        for (Catalogue.ProfilDeBase base : Catalogue.PROFILS_DE_BASE) {
            if (!profils.containsKey(base.nom())) {
                profils.put(base.nom(), annuaire.creerProfil(new Profil(null, base.nom(), base.description(), base.roles(), base.parDefaut())));
            }
        }

        int ranges = migrerLesComptes(profils);
        annuaire.definirProfilParDefaut(profils.get(Catalogue.PROFIL_CITOYEN.nom()));
        annuaire.retirerDesRolesParDefaut("CITOYEN");
        marqueur.marquer(ETAPE_MIGRATION);
        journal.enregistrer(new Operation(horloge.maintenant(), SYSTEME.signature(), TypeOperation.INITIALISATION,
                "Habilitations", Catalogue.PERMISSIONS.size() + " permissions, " + Catalogue.ROLES_DE_BASE.size()
                + " rôles, " + Catalogue.PROFILS_DE_BASE.size() + " profils ; " + ranges + " compte(s) rangé(s) dans leur profil"));
    }

    /**
     * Chaque compte qui recevait directement un rôle de base rejoint le profil correspondant et perd ce rôle
     * direct (il le retrouve par le profil). Les comptes qui n'avaient que le rôle par défaut sont des citoyens.
     */
    private int migrerLesComptes(Map<String, String> profils) {
        Set<String> agents = Set.of("DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN");
        Set<String> ranges = new HashSet<>();
        for (String role : agents) {
            for (String u : annuaire.utilisateursAvecRoleDirect(role)) {
                annuaire.ajouterAuProfil(u, profils.get(Catalogue.profilDuRole(role)));
                annuaire.retirerRolesDirects(u, Set.of(role));
                ranges.add(u);
            }
        }
        for (String u : annuaire.utilisateursAvecRoleDirect("CITOYEN")) {
            annuaire.ajouterAuProfil(u, profils.get(Catalogue.PROFIL_CITOYEN.nom()));
            annuaire.retirerRolesDirects(u, Set.of("CITOYEN"));
            ranges.add(u);
        }
        for (String u : annuaire.utilisateursAvecRoleDirect(annuaire.roleParDefaut())) {
            if (ranges.contains(u)) continue;
            annuaire.ajouterAuProfil(u, profils.get(Catalogue.PROFIL_CITOYEN.nom()));
            ranges.add(u);
        }
        return ranges.size();
    }
}
