package sn.etatcivil.document.domaine;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class DocumentDelivreTest {

    public static ContenuActe acte(String statut) {
        return new ContenuActe("NAISSANCE", "N-1998-000001", 1998, statut, "Dakar Plateau", null, "12", "34",
                LocalDate.of(1998, 3, 14), "Hôpital Principal, Dakar", "Diallo", "Fatou", "F", LocalDate.of(1998, 3, 14),
                "Hôpital Principal, Dakar", "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null,
                null, null, null, null);
    }

    static DocumentDelivre document() {
        return DocumentDelivre.delivrer(UUID.randomUUID(), "DEM-2026-000001", "u-fatou", "Extrait d'acte de naissance",
                false, "NUMERIQUE", acte("VALIDE"), "Mariama Sow", "ABCDE-23456", Instant.parse("2026-11-08T10:00:00.123Z"));
    }

    @Test
    void empreinteEtIntegrite() {
        DocumentDelivre d = document();
        assertThat(d.empreinte()).hasSize(64);
        assertThat(d.integre()).isTrue();
        // Le même document avec un nom modifié en base n'est plus intègre
        ContenuActe falsifie = new ContenuActe("NAISSANCE", "N-1998-000001", 1998, "VALIDE", "Dakar Plateau", null, "12", "34",
                LocalDate.of(1998, 3, 14), "Hôpital Principal, Dakar", "Ba", "Fatou", "F", LocalDate.of(1998, 3, 14),
                "Hôpital Principal, Dakar", "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null,
                null, null, null, null);
        DocumentDelivre modifie = new DocumentDelivre(d.id(), d.demandeId(), d.reference(), d.demandeurId(), d.libelleDocument(),
                d.copieIntegrale(), d.format(), falsifie, d.signataire(), d.dateSignature(), d.codeVerification(), d.empreinte());
        assertThat(modifie.integre()).isFalse();
    }

    @Test
    void reglesDeDelivrance() {
        assertThatThrownBy(() -> DocumentDelivre.delivrer(UUID.randomUUID(), "R", "u", "E", false, "NUMERIQUE",
                acte("SAISI"), "Officier", "C", Instant.now())).hasMessageContaining("pas validé");
        assertThat(DocumentDelivre.nouveauCode(new Random(1))).matches("[A-HJ-NP-Z2-9]{5}-[A-HJ-NP-Z2-9]{5}");
        DocumentDelivre d = document();
        assertThat(d.telechargeablePar("u-fatou", false)).isTrue();
        assertThat(d.telechargeablePar("u-autre", false)).isFalse();
        assertThat(d.telechargeablePar("u-agent", true)).isTrue();
    }
}
