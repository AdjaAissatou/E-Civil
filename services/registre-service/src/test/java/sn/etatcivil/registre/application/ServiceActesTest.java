package sn.etatcivil.registre.application;

import org.junit.jupiter.api.Test;
import sn.etatcivil.registre.application.port.entree.FiltreActes;
import sn.etatcivil.registre.application.port.entree.PageDemandee;
import sn.etatcivil.registre.application.port.entree.PageResultat;
import sn.etatcivil.registre.application.port.sortie.ActeRepository;
import sn.etatcivil.registre.application.service.ServiceActes;
import sn.etatcivil.registre.domaine.*;
import sn.etatcivil.registre.domaine.ActeTest;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/** Les cas d'usage se testent avec de simples implémentations en mémoire des ports : c'est l'intérêt de l'hexagone. */
class ServiceActesTest {

    static class ActesEnMemoire implements ActeRepository {
        final Map<UUID, Acte> actes = new LinkedHashMap<>();
        public Acte enregistrer(Acte a) { actes.put(a.id(), a); return a; }
        public Optional<Acte> parId(UUID id) { return Optional.ofNullable(actes.get(id)); }
        public Optional<Acte> parNumero(String n) { return actes.values().stream().filter(a -> a.numeroActe().equals(n)).findFirst(); }
        public List<Acte> candidats(TypeActe t, String nom, String prenom) {
            return actes.values().stream().filter(a -> a.typeActe() == t).toList();
        }
        public List<Acte> naissancesParNom(String nom) {
            return actes.values().stream().filter(a -> a.typeActe() == TypeActe.NAISSANCE && a.nomNormalise().equals(nom)).toList();
        }
        public PageResultat<Acte> lister(FiltreActes f, PageDemandee p) { return new PageResultat<>(List.copyOf(actes.values()), 0, 20, actes.size()); }
        public long compter() { return actes.size(); }
        public long compterParStatut(StatutActe s) { return actes.values().stream().filter(a -> a.statut() == s).count(); }
        public long compterParType(TypeActe t) { return actes.values().stream().filter(a -> a.typeActe() == t).count(); }
    }

    final ActesEnMemoire actes = new ActesEnMemoire();
    final List<Acte> valides = new ArrayList<>();
    int compteur = 0;
    final ServiceActes service = new ServiceActes(actes,
            (type, annee) -> String.format("%s-%d-%06d", type.prefixe(), annee, ++compteur),
            valides::add, Instant::now);

    @Test
    void transcrireValiderPuisRetrouver() {
        Acte a = service.transcrire(ActeTest.naissance(), "agent@ecivil.sn");
        assertThat(a.numeroActe()).isEqualTo("N-1998-000001");
        assertThat(service.rechercher(ActeTest.criteres("Dakar", null)).resultat())
                .isEqualTo(ResultatRecherche.Resultat.NON_TROUVE);

        service.valider(a.id());
        assertThat(valides).extracting(Acte::numeroActe).containsExactly("N-1998-000001");
        assertThat(service.rechercher(ActeTest.criteres("Dakar", null)).resultat())
                .isEqualTo(ResultatRecherche.Resultat.TROUVE);

        Acte homonyme = service.transcrire(ActeTest.naissance(), "agent@ecivil.sn");
        service.valider(homonyme.id());
        assertThat(service.rechercher(ActeTest.criteres(null, null)).resultat())
                .isEqualTo(ResultatRecherche.Resultat.AMBIGU);
        assertThat(service.statistiques().valides()).isEqualTo(2);
    }

    @Test
    void situationEtEnfantsDUnCouple() {
        java.time.LocalDate naissance = java.time.LocalDate.of(1972, 2, 14);
        Acte mariage = service.transcrire(ActeTest.acte(TypeActe.MARIAGE, java.time.LocalDate.of(2000, 8, 12), "Mbaye",
                "Babacar", naissance, "Diouf", "Seynabou", null, null), "agent");
        Personne babacar = new Personne("MBAYE", "babacar", naissance);
        assertThat(service.situation(babacar).mariages()).as("un acte non validé ne compte pas").isEmpty();
        service.valider(mariage.id());
        assertThat(service.situation(babacar).mariages()).hasSize(1);
        assertThat(service.situation(babacar).mariages().get(0).dissous()).isFalse();

        Acte deces = service.transcrire(ActeTest.acte(TypeActe.DECES, java.time.LocalDate.of(2019, 6, 1), "Diouf",
                "Seynabou", null, null, null, null, null), "agent");
        service.valider(deces.id());
        assertThat(service.situation(babacar).mariages().get(0).dissous()).as("veuvage").isTrue();

        DonneesActe enfant = new DonneesActe(TypeActe.NAISSANCE, "Kaolack", null, null, null, java.time.LocalDate.of(2003, 4, 5),
                "Kaolack", "Mbaye", "Awa", "F", null, null, "Mbaye", "Babacar", "Diouf", "Seynabou",
                null, null, null, null, null, null, null, null, null, null, null, null);
        service.valider(service.transcrire(enfant, "agent").id());
        assertThat(service.enfants(babacar, new Personne("Diouf", "Seynabou", null)))
                .extracting(a -> a.donnees().prenom()).containsExactly("Awa");
    }
}
