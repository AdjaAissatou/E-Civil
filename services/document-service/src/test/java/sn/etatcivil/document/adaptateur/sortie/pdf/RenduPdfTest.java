package sn.etatcivil.document.adaptateur.sortie.pdf;

import org.junit.jupiter.api.Test;
import sn.etatcivil.document.domaine.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Chaque modèle de document se rend en PDF ; les documents sur comparution n'ont pas d'acte. */
class RenduPdfTest {

    final RenduPdfIText rendu = new RenduPdfIText("http://localhost:8080");

    static ContenuActe acte(String type) {
        return new ContenuActe(type, type.charAt(0) + "-1998-000001", 1998, "VALIDE", "Centre de Dakar Plateau", "Dakar-Plateau",
                "12", "34", LocalDate.of(1998, 3, 14), "Hôpital Principal, Dakar", "Diallo", "Fatou", "F",
                LocalDate.of(1998, 3, 14), "Dakar", "Diallo", "Mamadou", "Sow", "Aminata", "Ndiaye", "Ousmane",
                LocalDate.of(1995, 1, 1), "Thiès", null, null, "Awa Fall", "Modou Ba",
                "Mention de divorce du 01/03/2010 (Jugement n° 45)", null, "ORDINAIRE", null,
                "Commerçante", "Dakar", "Enseignant", "Dakar", "Ménagère", "Dakar", null, null, null);
    }

    static Declaration declaration() {
        return new Declaration("Martin", "Claire", "F", LocalDate.of(1990, 5, 2), "Lyon", null, null, null, null,
                "Ingénieure", "Almadies, Dakar", "Awa Fall", null, "Nationalité française");
    }

    byte[] rendre(String libelle, String modele, ContenuActe a, Declaration d, String mention, String constat) {
        Complements c = new Complements(modele, "Dossier administratif",
                constat != null ? constat : Constats.pour(modele, a, d, mention), List.of(), LocalDate.of(2026, 12, 24), d, mention);
        DocumentDelivre doc = DocumentDelivre.delivrer(UUID.randomUUID(), "DEM-2026-000001", "u-1", libelle, "NUMERIQUE",
                a, c, "Mariama Sow", "ABCDE-23456", Instant.parse("2026-11-24T10:00:00Z"));
        assertThat(doc.integre()).isTrue();
        byte[] pdf = rendu.rendre(doc);
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
        return pdf;
    }

    @Test
    void chaqueModeleSeRend() throws Exception {
        Path dossier = Path.of("target", "apercus");
        Files.createDirectories(dossier);
        Files.write(dossier.resolve("acte-original.pdf"), rendre("Acte de naissance (original)", "ACTE_ORIGINAL", acte("NAISSANCE"), null, null, null));
        Files.write(dossier.resolve("certificat-mariage.pdf"), rendre("Certificat de mariage", "CERTIFICAT_ACTE", acte("MARIAGE"), null, null, null));
        Files.write(dossier.resolve("mention-divorce.pdf"), rendre("Extrait de mention de divorce", "EXTRAIT_MENTION", acte("MARIAGE"), null,
                "Mention de divorce du 01/03/2010 (Jugement n° 45)", null));
        Files.write(dossier.resolve("nationalite.pdf"), rendre("Certificat de nationalité sénégalaise", "CERTIFICAT_NATIONALITE", acte("NAISSANCE"), null, null, null));
        Files.write(dossier.resolve("domicile.pdf"), rendre("Certificat de domicile pour élections", "CERTIFICAT_DOMICILE", acte("NAISSANCE"),
                new Declaration("Diallo", "Fatou", "F", null, null, null, null, null, null, "Commerçante", "Sicap Liberté 2, Dakar", "Awa Fall", "Modou Ba", null), null, null));
        Files.write(dossier.resolve("non-appartenance.pdf"), rendre("Certificat de non-appartenance à la nationalité",
                "CERTIFICAT_NON_APPARTENANCE", null, declaration(), null, null));
        Files.write(dossier.resolve("coutume.pdf"), rendre("Certificat de coutume pour mariage mixte", "CERTIFICAT_COUTUME", null,
                new Declaration("Diallo", "Fatou", "F", LocalDate.of(1998, 3, 14), "Dakar", null, null, null, null, null, null, null, null,
                        "droit commun sénégalais, option monogamique"), null, null));
        assertThat(Constats.pour("CERTIFICAT_NATIONALITE", acte("NAISSANCE"), null, null))
                .contains("nationalité sénégalaise par filiation").contains("Mamadou Diallo");
    }

    @Test
    void lesReglesDeDelivranceSAppliquent() {
        assertThatThrownBy(() -> rendre("Coutume", "CERTIFICAT_COUTUME", acte("NAISSANCE"), declaration(), null, null))
                .hasMessageContaining("sans acte");
        assertThatThrownBy(() -> rendre("Extrait", "EXTRAIT", null, null, null, null)).hasMessageContaining("obligatoire");
        // la validité entre dans l'empreinte ; un document expiré se reconnaît
        Complements c = new Complements("EXTRAIT", null, null, List.of(), LocalDate.of(2026, 1, 1), null, null);
        assertThat(c.expire(LocalDate.of(2026, 1, 2))).isTrue();
        assertThat(new Complements("EXTRAIT", null, null, List.of()).expire(LocalDate.of(2099, 1, 1))).isFalse();
    }
}
