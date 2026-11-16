package sn.etatcivil.registre.domaine;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/** Tests du domaine pur : aucune base, aucun Spring. */
public class ActeTest {

    public static DonneesActe naissance() {
        return new DonneesActe(TypeActe.NAISSANCE, "Dakar Plateau", null, "12", "34", LocalDate.of(1998, 3, 14),
                "Hôpital Principal, Dakar", "Diallo", "Fatou Bintou", "F", null, null,
                "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static CriteresRecherche criteres(String lieu, LocalDate naissance) {
        return new CriteresRecherche(TypeActe.NAISSANCE, "DIALLO", "fatou  bintou", naissance, lieu,
                null, null, "Sow", null, null, null, null, null);
    }

    @Test
    void uneNaissanceDeduitDateEtLieuDeNaissance() {
        Acte a = Acte.transcrire(naissance(), "N-1998-000001", "agent", Instant.now());
        assertThat(a.statut()).isEqualTo(StatutActe.SAISI);
        assertThat(a.donnees().dateNaissance()).isEqualTo(LocalDate.of(1998, 3, 14));
        assertThat(a.donnees().lieuNaissance()).isEqualTo("Hôpital Principal, Dakar");
        assertThat(a.nomNormalise()).isEqualTo("DIALLO");
        assertThat(a.prenomNormalise()).isEqualTo("FATOU BINTOU");
    }

    @Test
    void seulUnActeValideCorrespondEtLesCriteresSontTolerants() {
        Acte a = Acte.transcrire(naissance(), "N-1998-000001", "agent", Instant.now());
        assertThat(a.correspond(criteres("Dakar", LocalDate.of(1998, 3, 14)))).isFalse();
        a.valider(Instant.now());
        assertThat(a.correspond(criteres("dakar", LocalDate.of(1998, 3, 14)))).isTrue();
        assertThat(a.correspond(criteres(null, null))).isTrue();
        assertThat(a.correspond(criteres("Thiès", null))).isFalse();
        assertThat(a.correspond(criteres(null, LocalDate.of(1999, 3, 14)))).isFalse();
    }

    @Test
    void unMariageSeRetrouveParLeConjoint() {
        DonneesActe m = new DonneesActe(TypeActe.MARIAGE, "Thiès", null, null, null, LocalDate.of(2015, 7, 4), "Thiès",
                "Ndiaye", "Ousmane", "M", null, null, null, null, null, null, "Fall", "Mariama",
                LocalDate.of(1990, 1, 2), null, null, null, null, null, null, null, null, null);
        Acte a = Acte.transcrire(m, "M-2015-000001", "agent", Instant.now());
        a.valider(Instant.now());
        assertThat(a.correspond(new CriteresRecherche(TypeActe.MARIAGE, "Fall", "Mariama", LocalDate.of(1990, 1, 2),
                null, null, null, null, null, "Ndiaye", "Ousmane", null, null))).isTrue();
    }

    @Test
    void reglesDeSaisie() {
        DonneesActe sansConjoint = new DonneesActe(TypeActe.MARIAGE, "C", null, null, null, LocalDate.of(2015, 7, 4),
                "Thiès", "Ndiaye", "Ousmane", null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        assertThatThrownBy(() -> Acte.transcrire(sansConjoint, "M-1", "a", Instant.now()))
                .isInstanceOf(RegleMetierException.class).hasMessageContaining("conjoint");

        Acte a = Acte.transcrire(naissance(), "N-1998-000001", "agent", Instant.now());
        a.valider(Instant.now());
        assertThatThrownBy(() -> a.valider(Instant.now())).hasMessageContaining("déjà validé");

        // Un acte validé n'accepte plus que des mentions
        DonneesActe n = naissance();
        DonneesActe modif = new DonneesActe(n.typeActe(), n.centre(), n.commune(), n.volume(), n.folio(),
                n.dateEvenement(), n.lieuEvenement(), "Ba", n.prenom(), n.sexe(), null, null, n.nomPere(),
                n.prenomPere(), n.nomMere(), n.prenomMere(), null, null, null, null, null, null, null, null,
                "Marié le 04/07/2015", null, null, null);
        a.corriger(modif, Instant.now());
        assertThat(a.donnees().nom()).isEqualTo("Diallo");
        assertThat(a.donnees().mentionsMarginales()).isEqualTo("Marié le 04/07/2015");
    }

    public static DonneesActe acte(TypeActe type, LocalDate date, String nom, String prenom, LocalDate naissance,
                            String nomConjoint, String prenomConjoint, OrigineActe origine, String reference) {
        return new DonneesActe(type, "Centre", null, null, null, date, "Dakar", nom, prenom, "M", naissance, null,
                type == TypeActe.RECONNAISSANCE ? nom : null, type == TypeActe.RECONNAISSANCE ? "Modou" : null, null, null,
                nomConjoint, prenomConjoint, null, null, null, null, null, null, null, null, origine, reference);
    }

    static Acte valide(DonneesActe d, String numero) {
        Acte a = Acte.transcrire(d, numero, "agent", Instant.now());
        a.valider(Instant.now());
        return a;
    }

    @Test
    void lOrigineEstOrdinaireParDefautEtUnJugementExigeSesReferences() {
        Acte ordinaire = valide(naissance(), "N-1998-000001");
        assertThat(ordinaire.donnees().origine()).isEqualTo(OrigineActe.ORDINAIRE);
        assertThatThrownBy(() -> Acte.transcrire(acte(TypeActe.NAISSANCE, LocalDate.of(2001, 5, 2), "Sy", "Awa", null,
                null, null, OrigineActe.JUGEMENT_SUPPLETIF, " "), "N-1", "a", Instant.now()))
                .hasMessageContaining("jugement supplétif");

        // un document « jugement supplétif » ne retrouve que les actes dressés par jugement
        CriteresRecherche parJugement = new CriteresRecherche(TypeActe.NAISSANCE, "Diallo", "Fatou Bintou", null, null,
                null, null, null, null, null, null, null, OrigineActe.JUGEMENT_SUPPLETIF);
        assertThat(ordinaire.correspond(parJugement)).isFalse();
        assertThat(ordinaire.correspond(criteres(null, null))).isTrue();
    }

    @Test
    void uneReconnaissanceAnticipeePrecedeLaNaissance() {
        DonneesActe r = new DonneesActe(TypeActe.RECONNAISSANCE, "Centre", null, null, null, LocalDate.of(2020, 1, 10),
                "Dakar", "Sarr", "Khadija", "F", LocalDate.of(2020, 3, 1), "Dakar", "Sarr", "Ousmane", null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(Acte.transcrire(r, "R-2020-000001", "a", Instant.now()).typeActe()).isEqualTo(TypeActe.RECONNAISSANCE);
        DonneesActe sansParent = new DonneesActe(TypeActe.RECONNAISSANCE, "Centre", null, null, null, LocalDate.of(2020, 1, 10),
                "Dakar", "Sarr", "Khadija", "F", null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null);
        assertThatThrownBy(() -> Acte.transcrire(sansParent, "R-1", "a", Instant.now())).hasMessageContaining("parent");
    }

    @Test
    void laSituationCivileDistingueLesMariagesDissous() {
        Personne babacar = new Personne("Mbaye", "Babacar", LocalDate.of(1972, 2, 14));
        Acte premier = valide(acte(TypeActe.MARIAGE, LocalDate.of(2000, 8, 12), "Mbaye", "Babacar",
                LocalDate.of(1972, 2, 14), "Diouf", "Seynabou", null, null), "M-2000-000001");
        Acte second = valide(acte(TypeActe.MARIAGE, LocalDate.of(2010, 4, 3), "Ndiaye", "Coumba",
                null, "Mbaye", "Babacar", null, null), "M-2010-000001");
        Acte divorce = valide(acte(TypeActe.DIVORCE, LocalDate.of(2008, 1, 9), "Diouf", "Seynabou", null,
                "Mbaye", "Babacar", null, null), "D-2008-000001");

        SituationCivile s = SituationCivile.etablir(babacar, java.util.List.of(second, premier), java.util.List.of(divorce),
                java.util.List.of(), conjoint -> java.util.List.of());

        assertThat(s.mariages()).extracting(m -> m.acte().numeroActe()).containsExactly("M-2000-000001", "M-2010-000001");
        assertThat(s.mariages().get(0).dissous()).isTrue();
        assertThat(s.mariages().get(0).dissolution().numeroActe()).isEqualTo("D-2008-000001");
        assertThat(s.mariages().get(1).dissous()).isFalse();
        assertThat(s.mariages().get(1).conjoint().prenom()).isEqualTo("Coumba");
        // un homonyme né à une autre date n'est pas la même personne
        assertThat(premier.designe("Mbaye", "Babacar", LocalDate.of(1980, 1, 1))).isFalse();
    }
}
