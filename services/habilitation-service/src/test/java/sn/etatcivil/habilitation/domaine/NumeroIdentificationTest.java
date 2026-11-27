package sn.etatcivil.habilitation.domaine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class NumeroIdentificationTest {

    @Test
    void le_nin_est_range_sans_separateurs() {
        assertThat(NumeroIdentification.verifier(" 2 018 2018 83088 ", "F", 2026)).isEqualTo("2018201883088");
        assertThat(NumeroIdentification.verifier("1-G01-1985-00654", "M", 2026)).isEqualTo("1G01198500654");
        assertThat(NumeroIdentification.verifier("17511989012345", null, 2026)).isEqualTo("17511989012345"); // indice
        assertThat(NumeroIdentification.verifier("  ", "F", 2026)).isNull();
        assertThat(NumeroIdentification.presenter("2018201883088")).isEqualTo("2 018 2018 83088");
    }

    @Test
    void un_nin_hors_format_ou_incoherent_est_refuse() {
        for (String faux : new String[] {"201820188308", "3018201883088", "2018201883088123", "20182O1883088", "2018203083088", "AB12345"}) {
            assertThatThrownBy(() -> NumeroIdentification.verifier(faux, null, 2026)).as(faux)
                    .hasMessageContaining("13 chiffres");
        }
        assertThatThrownBy(() -> NumeroIdentification.verifier("2018201883088", "M", 2026))
                .hasMessageContaining("sexe");
    }

    @Test
    void un_ancien_numero_reste_lisible_mais_doit_etre_corrige_a_la_modification() {
        ProfilPersonnel p = new ProfilPersonnel("Adja", "Dione", "a@example.sn", null, "F", LocalDate.of(2003, 1, 1), null, null,
                null, null, null, "AB 12 34", null, null, true, "fr");
        assertThat(p.numeroCni()).isEqualTo("AB 12 34");
        assertThatThrownBy(() -> p.verifierNumeroCni(2026)).hasMessageContaining("13 chiffres");
        ProfilPersonnel bon = new ProfilPersonnel("Adja", "Dione", "a@example.sn", null, "F", null, null, null,
                null, null, null, "2 018 2018 83088", null, null, true, "fr");
        assertThat(bon.numeroCni()).isEqualTo("2018201883088");
        bon.verifierNumeroCni(2026);
    }
}
