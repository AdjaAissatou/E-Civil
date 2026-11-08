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
                "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null, null, null, null, null);
    }

    public static CriteresRecherche criteres(String lieu, LocalDate naissance) {
        return new CriteresRecherche(TypeActe.NAISSANCE, "DIALLO", "fatou  bintou", naissance, lieu,
                null, null, "Sow", null, null, null, null);
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
                LocalDate.of(1990, 1, 2), null, null, null, null, null, null, null);
        Acte a = Acte.transcrire(m, "M-2015-000001", "agent", Instant.now());
        a.valider(Instant.now());
        assertThat(a.correspond(new CriteresRecherche(TypeActe.MARIAGE, "Fall", "Mariama", LocalDate.of(1990, 1, 2),
                null, null, null, null, null, "Ndiaye", "Ousmane", null))).isTrue();
    }

    @Test
    void reglesDeSaisie() {
        DonneesActe sansConjoint = new DonneesActe(TypeActe.MARIAGE, "C", null, null, null, LocalDate.of(2015, 7, 4),
                "Thiès", "Ndiaye", "Ousmane", null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
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
                "Marié le 04/07/2015", null);
        a.corriger(modif, Instant.now());
        assertThat(a.donnees().nom()).isEqualTo("Diallo");
        assertThat(a.donnees().mentionsMarginales()).isEqualTo("Marié le 04/07/2015");
    }
}
