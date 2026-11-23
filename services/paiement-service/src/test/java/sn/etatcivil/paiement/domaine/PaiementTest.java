package sn.etatcivil.paiement.domaine;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PaiementTest {

    final Instant t0 = Instant.parse("2026-11-08T10:00:00Z");

    @Test
    void seulUnPaiementCompletAuBonMontantEstConfirme() {
        Paiement p = Paiement.initier(UUID.randomUUID(), "tok", 1000, "https://pay", t0);
        assertThat(p.appliquer(new EtatFacture(EtatFacture.Etat.EN_ATTENTE, 0, null), t0)).isFalse();
        assertThat(p.statut()).isEqualTo(StatutPaiement.INITIE);
        assertThatThrownBy(() -> p.appliquer(new EtatFacture(EtatFacture.Etat.PAYEE, 200, null), t0))
                .hasMessageContaining("montant");
        assertThat(p.appliquer(new EtatFacture(EtatFacture.Etat.PAYEE, 1000, "recu"), t0)).isTrue();
        assertThat(p.appliquer(new EtatFacture(EtatFacture.Etat.PAYEE, 1000, "recu"), t0)).isFalse(); // une seule fois
        assertThat(p.urlRecu()).isEqualTo("recu");
    }

    @Test
    void factureReutilisablePendantUneHeure() {
        Paiement p = Paiement.initier(UUID.randomUUID(), "tok", 1000, "https://pay", t0);
        assertThat(p.reutilisable(t0.plusSeconds(3000))).isTrue();
        assertThat(p.reutilisable(t0.plusSeconds(3700))).isFalse();
    }

    @Test
    void seulLeDemandeurPeutPayerUneDemandeOuverte() {
        DemandePayable d = new DemandePayable(UUID.randomUUID(), "DEM-1", "u-fatou", "Extrait", 1000, true);
        d.verifierPayablePar("u-fatou");
        assertThatThrownBy(() -> d.verifierPayablePar("u-autre")).isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> new DemandePayable(d.demandeId(), "DEM-1", "u-fatou", "E", 1000, false).verifierPayablePar("u-fatou"))
                .hasMessageContaining("pas en attente");
    }
}
