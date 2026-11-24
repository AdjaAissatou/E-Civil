package sn.etatcivil.habilitation.domaine;

import org.junit.jupiter.api.Test;
import sn.etatcivil.socle.securite.Permissions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static sn.etatcivil.habilitation.domaine.Catalogue.*;

class DomaineTest {

    @Test
    void leCatalogueCorrespondAuxPermissionsVerifieesParLesServices() {
        Set<String> codesDuSocle = Arrays.stream(Permissions.class.getFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && !f.getName().startsWith("A_") && !f.getName().equals("SERVICE"))
                .map(DomaineTest::valeur).collect(Collectors.toSet());
        assertThat(CODES_PERMISSIONS).isEqualTo(codesDuSocle).hasSize(15);
        // chaque rôle de base n'accorde que des permissions du catalogue ; l'administrateur administre
        ROLES_DE_BASE.forEach(r -> assertThat(CODES_PERMISSIONS).containsAll(r.permissions()));
        assertThat(ROLES_DE_BASE.stream().filter(r -> r.code().equals("ADMIN")).findFirst().orElseThrow().permissions())
                .containsAll(ADMINISTRATION).doesNotContain(DEMANDES_DEPOSER);
        PROFILS_DE_BASE.forEach(p -> assertThat(CODES_ROLES_DE_BASE).containsAll(p.roles()));
    }

    @Test
    void reglesDesRolesEtDesProfils() {
        Role r = Role.creer(" agent-accueil ", "Agent d'accueil", "", Set.of(DEMANDES_CONSULTER));
        assertThat(r.code()).isEqualTo("AGENT_ACCUEIL");
        assertThat(r.description()).isNull();
        assertThatThrownBy(() -> Role.creer("x", "X", null, Set.of(DEMANDES_CONSULTER))).hasMessageContaining("3 à 40");
        assertThatThrownBy(() -> Role.creer("SERVICE", "S", null, Set.of(DEMANDES_CONSULTER))).hasMessageContaining("réservé");
        assertThatThrownBy(() -> Role.creer("GUICHET", "G", null, Set.of())).hasMessageContaining("au moins une permission");
        assertThatThrownBy(() -> Role.creer("GUICHET", "G", null, Set.of("INVENTEE"))).hasMessageContaining("inconnue");
        assertThatThrownBy(() -> Profil.creer("A/B", null, Set.of("CITOYEN"))).hasMessageContaining("/");
        assertThatThrownBy(() -> Profil.creer("Vide", null, Set.of())).hasMessageContaining("au moins un rôle");
    }

    @Test
    void lOrganisationSimuleAvantDAppliquer() {
        Organisation o = new Organisation(
                Map.of("ADMIN", Set.of(UTILISATEURS_GERER, HABILITATIONS_GERER), "OFFICIER", Set.of(DOCUMENTS_SIGNER)),
                Map.of("pa", Set.of("ADMIN"), "po", Set.of("OFFICIER")),
                Map.of("pa", Set.of("adja"), "po", Set.of("mariama")), Set.of());
        Administrateur adja = new Administrateur("adja", "Adja", "adja@ecivil.sn");
        assertThat(o.administrateursActifs()).containsExactly("adja");
        assertThatCode(() -> o.avecProfilsDe("mariama", Set.of("pa", "po")).verifierAdministrationPreservee(adja)).doesNotThrowAnyException();
        assertThatThrownBy(() -> o.avecCompteActif("adja", false).verifierAdministrationPreservee(null))
                .hasMessageContaining("aucun administrateur");
        Organisation deux = o.avecProfilsDe("mariama", Set.of("pa"));
        assertThatThrownBy(() -> deux.avecProfilsDe("adja", Set.of("po")).verifierAdministrationPreservee(adja))
                .hasMessageContaining("vos droits");
        assertThat(deux.avecProfilsDe("adja", Set.of("po")).administrateursActifs()).containsExactly("mariama");
    }

    private static String valeur(Field f) {
        try {
            return (String) f.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
