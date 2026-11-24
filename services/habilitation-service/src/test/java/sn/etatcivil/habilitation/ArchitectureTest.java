package sn.etatcivil.habilitation;

import org.junit.jupiter.api.Test;
import sn.etatcivil.socle.tests.ReglesHexagonales;

class ArchitectureTest {
    @Test
    void respecteLArchitectureHexagonale() {
        ReglesHexagonales.verifier("sn.etatcivil.habilitation");
    }
}
