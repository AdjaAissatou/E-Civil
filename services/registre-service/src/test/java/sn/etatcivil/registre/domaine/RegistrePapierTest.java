package sn.etatcivil.registre.domaine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrePapierTest {

    @Test
    void un_centre_secondaire_est_rattache_et_un_centre_consulaire_a_un_pays() {
        assertThatThrownBy(() -> CentreEtatCivil.nouveau(null, "Centre de la Médina", TypeCentre.SECONDAIRE, "Dakar", null, null, true))
                .hasMessageContaining("rattaché");
        assertThatThrownBy(() -> CentreEtatCivil.nouveau(null, "Centre consulaire de Rome", TypeCentre.CONSULAIRE, null, " ", null, true))
                .hasMessageContaining("pays");
        CentreEtatCivil c = CentreEtatCivil.nouveau(null, " Centre consulaire de Rome ", TypeCentre.CONSULAIRE, "Dakar", "Italie", 4L, true);
        assertThat(c.nom()).isEqualTo("Centre consulaire de Rome");
        assertThat(c.commune()).isNull();
        assertThat(c.rattachementId()).isNull();
        assertThat(c.localisation()).isEqualTo("Italie");
    }

    @Test
    void un_registre_porte_une_annee_passee_et_un_exemplaire() {
        assertThatThrownBy(() -> RegistrePapier.nouveau(null, 1L, TypeActe.NAISSANCE, 2031, Exemplaire.CENTRE, null, null, null,
                false, null, 2026)).hasMessageContaining("1900 et 2026");
        assertThatThrownBy(() -> RegistrePapier.nouveau(null, 1L, TypeActe.NAISSANCE, 1998, null, null, null, null,
                false, null, 2026)).hasMessageContaining("exemplaire");
        RegistrePapier r = RegistrePapier.nouveau(null, 1L, TypeActe.DECES, 1998, Exemplaire.GREFFE, " 2 ", "", null,
                false, null, 2026);
        assertThat(r.etat()).isEqualTo(EtatRegistre.BON);
        assertThat(r.volume()).isEqualTo("2");
        assertThat(r.lieuConservation()).isNull();
        assertThat(EtatRegistre.MANQUANT.consultable()).isFalse();
    }
}
