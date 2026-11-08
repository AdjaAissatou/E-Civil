package sn.etatcivil.paiement;

import org.junit.jupiter.api.Test;
import sn.etatcivil.socle.tests.ReglesHexagonales;

class ArchitectureTest {
    @Test
    void respecteLArchitectureHexagonale() {
        ReglesHexagonales.verifier("sn.etatcivil.paiement");
    }
}
