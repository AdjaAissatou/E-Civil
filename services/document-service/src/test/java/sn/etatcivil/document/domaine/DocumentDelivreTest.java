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
                null, null, null, null, "ORDINAIRE", null);
    }

    public static ContenuActe mariage() {
        return new ContenuActe("MARIAGE", "M-2000-000001", 2000, "VALIDE", "Kaolack", "Kaolack", "1", "56",
                LocalDate.of(2000, 8, 12), "Kaolack", "Mbaye", "Babacar", "M", LocalDate.of(1972, 2, 14), "Kaolack",
                "Mbaye", "Modou", "Sène", "Fatou", "Diouf", "Seynabou", LocalDate.of(1976, 5, 5), "Fatick", null, null,
                "Pape Diouf", "Moussa Sène", null, null, "ORDINAIRE", null);
    }

    static final Complements EXTRAIT = new Complements("EXTRAIT", "Dossier administratif", null, null);

    static DocumentDelivre document() {
        return DocumentDelivre.delivrer(UUID.randomUUID(), "DEM-2026-000001", "u-fatou", "Extrait d'acte de naissance",
                "NUMERIQUE", acte("VALIDE"), EXTRAIT, "Mariama Sow", "ABCDE-23456", Instant.parse("2026-11-08T10:00:00.123Z"));
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
                null, null, null, null, "ORDINAIRE", null);
        DocumentDelivre modifie = new DocumentDelivre(d.id(), d.demandeId(), d.reference(), d.demandeurId(), d.libelleDocument(),
                d.copieIntegrale(), d.format(), falsifie, d.complements(), d.signataire(), d.dateSignature(),
                d.codeVerification(), d.empreinte());
        assertThat(modifie.integre()).isFalse();
    }

    @Test
    void reglesDeDelivrance() {
        assertThatThrownBy(() -> DocumentDelivre.delivrer(UUID.randomUUID(), "R", "u", "E", "NUMERIQUE",
                acte("SAISI"), EXTRAIT, "Officier", "C", Instant.now())).hasMessageContaining("pas validé");
        assertThatThrownBy(() -> DocumentDelivre.delivrer(UUID.randomUUID(), "R", "u", "Certificat de célibat", "NUMERIQUE",
                acte("VALIDE"), new Complements("CERTIFICAT_CELIBAT", "Mariage", " ", null), "Officier", "C", Instant.now()))
                .hasMessageContaining("constat");
        assertThatThrownBy(() -> DocumentDelivre.delivrer(UUID.randomUUID(), "R", "u", "Livret", "NUMERIQUE",
                acte("VALIDE"), new Complements("LIVRET_FAMILLE", null, null, null), "Officier", "C", Instant.now()))
                .hasMessageContaining("acte de mariage");
        assertThat(DocumentDelivre.nouveauCode(new Random(1))).matches("[A-HJ-NP-Z2-9]{5}-[A-HJ-NP-Z2-9]{5}");
        DocumentDelivre d = document();
        assertThat(d.telechargeablePar("u-fatou", false)).isTrue();
        assertThat(d.telechargeablePar("u-autre", false)).isFalse();
        assertThat(d.telechargeablePar("u-agent", true)).isTrue();
    }

    /** Les documents signés avant l'ajout des compléments doivent rester vérifiables : même empreinte qu'avant. */
    @Test
    void lesDocumentsDejaSignesGardentLeurEmpreinte() throws Exception {
        ContenuActe ancien = new ContenuActe("NAISSANCE", "N-1998-000001", 1998, "VALIDE", "Dakar Plateau", null, "12", "34",
                LocalDate.of(1998, 3, 14), "Hôpital Principal, Dakar", "Diallo", "Fatou", "F", LocalDate.of(1998, 3, 14),
                "Hôpital Principal, Dakar", "Diallo", "Mamadou", "Sow", "Aminata", null, null, null, null, null, null,
                null, null, null, null, null, null);
        Instant date = Instant.parse("2026-11-08T10:00:00Z");
        // formule d'origine : référence, libellé, copie intégrale, signataire, date, code, puis l'acte
        String texte = String.join("\n", "DEM-2026-000001", "Extrait d'acte de naissance", "false", "Mariama Sow",
                date.toString(), "ABCDE-23456", String.join("|", "NAISSANCE", "N-1998-000001", "1998", "Dakar Plateau", "",
                        "12", "34", "1998-03-14", "Hôpital Principal, Dakar", "Diallo", "Fatou", "F", "1998-03-14",
                        "Hôpital Principal, Dakar", "Diallo", "Mamadou", "Sow", "Aminata", "", "", "", "", "", "", "", "", "", ""));
        String empreinte = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(texte.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        DocumentDelivre d = new DocumentDelivre(UUID.randomUUID(), UUID.randomUUID(), "DEM-2026-000001", "u",
                "Extrait d'acte de naissance", false, "NUMERIQUE", ancien, null, "Mariama Sow", date, "ABCDE-23456", empreinte);
        assertThat(d.integre()).isTrue();
        assertThat(d.complements().modele()).isEqualTo("EXTRAIT");
    }
}
