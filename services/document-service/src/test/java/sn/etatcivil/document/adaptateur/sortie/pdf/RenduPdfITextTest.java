package sn.etatcivil.document.adaptateur.sortie.pdf;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sn.etatcivil.document.domaine.Complements;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;
import sn.etatcivil.document.domaine.DocumentDelivreTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Chaque modèle de document produit un PDF. */
class RenduPdfITextTest {

    final RenduPdfIText rendu = new RenduPdfIText("http://localhost:8080");

    @ParameterizedTest
    @ValueSource(strings = {"EXTRAIT", "EXTRAIT_SANS_FILIATION", "COPIE_INTEGRALE", "LIVRET_FAMILLE",
            "CERTIFICAT_CELIBAT", "CERTIFICAT_CAPACITE_MATRIMONIALE", "CERTIFICAT_VIE"})
    void chaqueModeleSeRend(String modele) {
        ContenuActe acte = modele.equals("LIVRET_FAMILLE") ? DocumentDelivreTest.mariage() : DocumentDelivreTest.acte("VALIDE");
        List<Complements.Enfant> enfants = modele.equals("LIVRET_FAMILLE")
                ? List.of(new Complements.Enfant("N-2002-000001", "Awa", "Mbaye", "F", LocalDate.of(2002, 5, 30), "Kaolack"))
                : List.of();
        String constat = modele.startsWith("CERTIFICAT_") ? "Aucun acte de mariage n'est inscrit." : null;
        DocumentDelivre d = DocumentDelivre.delivrer(UUID.randomUUID(), "DEM-2026-000001", "u", "Document", "NUMERIQUE",
                acte, new Complements(modele, "Mariage", constat, enfants), "Mariama Sow", "ABCDE-23456", Instant.now());
        assertThat(new String(rendu.rendre(d), 0, 5)).isEqualTo("%PDF-");
    }

    @ParameterizedTest
    @ValueSource(strings = {"JUGEMENT_SUPPLETIF", "TRANSCRIPTION"})
    void uneOrigineParticuliereEstImprimee(String origine) {
        ContenuActe a = DocumentDelivreTest.acte("VALIDE");
        ContenuActe special = new ContenuActe(a.typeActe(), a.numeroActe(), a.annee(), a.statut(), a.centre(), a.commune(),
                a.volume(), a.folio(), a.dateEvenement(), a.lieuEvenement(), a.nom(), a.prenom(), a.sexe(), a.dateNaissance(),
                a.lieuNaissance(), a.nomPere(), a.prenomPere(), a.nomMere(), a.prenomMere(), null, null, null, null, null,
                null, null, null, null, null, origine, "Jugement n° 238 du 3 mars 2010");
        DocumentDelivre d = DocumentDelivre.delivrer(UUID.randomUUID(), "DEM-2026-000001", "u", "Document", "NUMERIQUE",
                special, new Complements("COPIE_INTEGRALE", null, null, null), "Mariama Sow", "ABCDE-23456", Instant.now());
        assertThat(rendu.rendre(d).length).isGreaterThan(1000);
    }
}
