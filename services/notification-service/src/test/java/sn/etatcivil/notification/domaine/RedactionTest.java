package sn.etatcivil.notification.domaine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedactionTest {

    ChangementDemande c(String statut, String format) {
        return new ChangementDemande("id", "DEM-2026-000001", statut, "fatou@example.sn", "Fatou",
                "Extrait d'acte de naissance", "Fatou Diallo", 1000, format, "Pièces manquantes");
    }

    @Test
    void unMessageParEtape() {
        assertThat(Redaction.rediger(c("A_PAYER", "NUMERIQUE"), "http://lien").orElseThrow().corps())
                .startsWith("Bonjour Fatou,").contains("1000 FCFA").contains("http://lien");
        assertThat(Redaction.rediger(c("SIGNEE", "NUMERIQUE"), "l").orElseThrow().corps()).contains("télécharger");
        assertThat(Redaction.rediger(c("SIGNEE", "PHYSIQUE"), "l").orElseThrow().corps()).contains("expédié");
        assertThat(Redaction.rediger(c("REJETEE", "NUMERIQUE"), "l").orElseThrow().corps()).contains("Pièces manquantes");
        assertThat(Redaction.rediger(c("ANNULEE", "NUMERIQUE"), "l")).isEmpty();
        assertThat(Redaction.rediger(c("PAYEE", "NUMERIQUE"), "l").orElseThrow().sujet())
                .isEqualTo("E-Civil – DEM-2026-000001 : paiement reçu");
    }
}
