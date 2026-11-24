package sn.etatcivil.habilitation.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.application.port.sortie.JournalOperations;
import sn.etatcivil.habilitation.application.port.sortie.MarqueurInitialisation;
import sn.etatcivil.habilitation.application.service.ServiceHabilitations;
import sn.etatcivil.habilitation.application.service.ServiceInitialisation;
import sn.etatcivil.habilitation.domaine.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static sn.etatcivil.habilitation.domaine.Catalogue.*;

/** Cas d'usage avec un annuaire en mémoire : ni Keycloak, ni base, ni Spring. */
class ServiceHabilitationsTest {

    final AnnuaireEnMemoire annuaire = new AnnuaireEnMemoire();
    final List<Operation> journal = new ArrayList<>();
    final Set<String> marqueurs = new HashSet<>();

    final JournalOperations journalOperations = new JournalOperations() {
        public void enregistrer(Operation o) { journal.add(o); }
        public PageResultat<Operation> lire(String r, PageDemandee p) { return new PageResultat<>(journal, 0, 30, journal.size()); }
        public List<Operation> concernant(String cible, int n) { return journal.stream().filter(o -> o.cible().equals(cible)).toList(); }
    };
    final MarqueurInitialisation marqueur = new MarqueurInitialisation() {
        public boolean estFaite(String e) { return marqueurs.contains(e); }
        public void marquer(String e) { marqueurs.add(e); }
    };

    final ServiceInitialisation initialisation = new ServiceInitialisation(annuaire, marqueur, journalOperations, Instant::now);
    final ServiceHabilitations service = new ServiceHabilitations(annuaire, journalOperations, Instant::now);

    String admin, officier, citoyen;
    Administrateur auteur;

    /** Realm tel qu'il était avant cette version : rôles attribués directement, CITOYEN par défaut. */
    @BeforeEach
    void realmExistant() {
        annuaire.rolesBruts.addAll(Set.of("CITOYEN", "DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN", "SERVICE"));
        admin = annuaire.ajouterCompte("admin@ecivil.sn", true, "ADMIN");
        officier = annuaire.ajouterCompte("officier@ecivil.sn", true, "OFFICIER");
        citoyen = annuaire.ajouterCompte("fatou@example.sn", true);
        initialisation.initialiser();
        auteur = new Administrateur(admin, "Adja Dione", "admin@ecivil.sn");
    }

    @Test
    void lInitialisationMigreLesComptesSansPerteDeDroits() {
        assertThat(annuaire.permissions).hasSize(15);
        assertThat(annuaire.profils.values()).extracting(Profil::nom).containsExactlyInAnyOrder(
                "Citoyen", "Délégué de commune", "Agent du registre", "Officier de l'état civil", "Chef de centre", "Administrateur");
        // chaque compte est rangé dans son profil, ses rôles directs ont disparu
        assertThat(annuaire.profilsParCompte.get(admin)).containsExactly(annuaire.idProfil("Administrateur"));
        assertThat(annuaire.profilsParCompte.get(officier)).containsExactly(annuaire.idProfil("Officier de l'état civil"));
        assertThat(annuaire.profilsParCompte.get(citoyen)).containsExactly(annuaire.idProfil("Citoyen"));
        assertThat(annuaire.rolesDirectsParCompte.get(admin)).doesNotContain("ADMIN");
        // les droits sont devenus des permissions
        assertThat(annuaire.permissionsDe(officier)).contains(DOCUMENTS_SIGNER, REGISTRE_VALIDER).doesNotContain(UTILISATEURS_GERER);
        assertThat(annuaire.permissionsDe(citoyen)).containsExactly(DEMANDES_DEPOSER);
        assertThat(annuaire.permissionsDe(admin)).containsAll(ADMINISTRATION);
        // les nouvelles inscriptions reçoivent le profil Citoyen, plus le rôle par défaut
        assertThat(annuaire.profilParDefaut).isEqualTo(annuaire.idProfil("Citoyen"));
        assertThat(annuaire.rolesParDefaut).doesNotContain("CITOYEN");
        assertThat(journal).singleElement().extracting(Operation::type).isEqualTo(TypeOperation.INITIALISATION);

        // un second démarrage ne rejoue rien et ne défait pas les choix de l'administrateur
        service.modifierRole("DELEGUE", "Délégué", null, Set.of(DEMANDES_CONSULTER, DEMANDES_VALIDER), auteur);
        initialisation.initialiser();
        assertThat(annuaire.roles.get("DELEGUE").permissions()).containsExactlyInAnyOrder(DEMANDES_CONSULTER, DEMANDES_VALIDER);
        assertThat(annuaire.profils).hasSize(6);
    }

    @Test
    void creerUnAgentEtLuiAttribuerUnProfil() {
        String chef = annuaire.idProfil("Chef de centre");
        String id = service.creerUtilisateur(new NouvelUtilisateur("Awa.Ndiaye@ecivil.sn", "Awa", "Ndiaye", Set.of(chef),
                "Provisoire2026"), auteur);
        assertThat(annuaire.permissionsDe(id)).contains(DOCUMENTS_SIGNER, DEMANDES_VALIDER).doesNotContain(DEMANDES_DEPOSER);
        assertThat(annuaire.profilsParCompte.get(id)).containsExactly(chef);
        assertThat(service.fiche(id).permissions()).extracting(Permission::code).contains(DOCUMENTS_SIGNER);
        assertThat(journal).last().satisfies(o -> {
            assertThat(o.type()).isEqualTo(TypeOperation.UTILISATEUR_CREE);
            assertThat(o.cible()).isEqualTo("awa.ndiaye@ecivil.sn");
            assertThat(o.auteur()).isEqualTo("Adja Dione <admin@ecivil.sn>");
        });
        assertThatThrownBy(() -> service.creerUtilisateur(new NouvelUtilisateur("awa.ndiaye@ecivil.sn", "A", "N", Set.of(chef),
                "Provisoire2026"), auteur)).hasMessageContaining("existe déjà");
        assertThatThrownBy(() -> new NouvelUtilisateur("x@ecivil.sn", "A", "N", Set.of(chef), "court"))
                .hasMessageContaining("8 caractères");
    }

    @Test
    void laPlateformeGardeToujoursUnAdministrateur() {
        String profilAdmin = annuaire.idProfil("Administrateur");
        assertThatThrownBy(() -> service.activer(admin, false, auteur)).hasMessageContaining("votre propre compte");
        assertThatThrownBy(() -> service.definirProfils(admin, Set.of(annuaire.idProfil("Citoyen")), auteur))
                .hasMessageContaining("aucun administrateur actif");
        assertThatThrownBy(() -> service.modifierRole("ADMIN", "Administrateur", null, Set.of(STATISTIQUES_CONSULTER), auteur))
                .hasMessageContaining("administrat");
        assertThatThrownBy(() -> service.modifierProfil(profilAdmin, "Administrateur", null, Set.of("OFFICIER"), auteur))
                .hasMessageContaining("administrat");

        // avec un second administrateur, le premier peut quitter le profil… mais pas se retirer lui-même ses droits
        String second = service.creerUtilisateur(new NouvelUtilisateur("second@ecivil.sn", "S", "A", Set.of(profilAdmin),
                "Provisoire2026"), auteur);
        Administrateur secondAuteur = new Administrateur(second, "Second", "second@ecivil.sn");
        service.definirProfils(admin, Set.of(annuaire.idProfil("Officier de l'état civil")), secondAuteur);
        assertThat(annuaire.permissionsDe(admin)).doesNotContain(UTILISATEURS_GERER);
        assertThat(annuaire.deconnectes).contains(admin);
        // désactiver le dernier administrateur actif est refusé
        assertThatThrownBy(() -> service.activer(second, false, new Administrateur("autre", "Autre", "a@ecivil.sn")))
                .hasMessageContaining("aucun administrateur");
    }

    @Test
    void profilsEtRolesProtegent() {
        String citoyens = annuaire.idProfil("Citoyen");
        assertThatThrownBy(() -> service.supprimerProfil(citoyens, auteur)).hasMessageContaining("chaque nouvelle inscription");
        assertThatThrownBy(() -> service.supprimerProfil(annuaire.idProfil("Officier de l'état civil"), auteur))
                .hasMessageContaining("compte encore 1 membre");
        assertThatThrownBy(() -> service.supprimerRole("OFFICIER", auteur)).hasMessageContaining("rôle de base");
        assertThatThrownBy(() -> service.creerProfil("administrateur", null, Set.of("ADMIN"), auteur))
                .hasMessageContaining("porte déjà le nom");

        // un nouveau rôle, un profil qui l'utilise, puis suppression dans le bon ordre
        String code = service.creerRole("agent accueil", "Agent d'accueil", "Guichet", Set.of(DEMANDES_CONSULTER), auteur);
        assertThat(code).isEqualTo("AGENT_ACCUEIL");
        assertThatThrownBy(() -> service.creerRole("DOCUMENTS_SIGNER", "X", null, Set.of(DEMANDES_CONSULTER), auteur))
                .hasMessageContaining("permission");
        String guichet = service.creerProfil("Guichet", null, Set.of("AGENT_ACCUEIL"), auteur);
        assertThatThrownBy(() -> service.supprimerRole("AGENT_ACCUEIL", auteur)).hasMessageContaining("Guichet");
        service.supprimerProfil(guichet, auteur);
        service.supprimerRole("AGENT_ACCUEIL", auteur);
        assertThat(annuaire.roles).doesNotContainKey("AGENT_ACCUEIL");
    }

    @Test
    void laMatriceNeModifieQueLesRolesChanges() {
        Map<String, Set<String>> matrice = new HashMap<>();
        annuaire.roles.values().forEach(r -> matrice.put(r.code(), r.permissions()));
        matrice.put("DELEGUE", Set.of(DEMANDES_CONSULTER, DEMANDES_VALIDER, DEMANDES_SUPERVISER));
        int avant = journal.size();
        service.modifierMatrice(matrice, auteur);
        assertThat(journal).hasSize(avant + 1);
        assertThat(journal).last().extracting(Operation::detail).asString()
                .contains("+ DEMANDES_SUPERVISER").contains("− DOCUMENTS_LIVRER");
        matrice.put("ADMIN", Set.of(DEMANDES_CONSULTER));
        assertThatThrownBy(() -> service.modifierMatrice(matrice, auteur)).hasMessageContaining("administrat");
        assertThat(annuaire.roles.get("ADMIN").permissions()).containsAll(ADMINISTRATION);
    }
}
