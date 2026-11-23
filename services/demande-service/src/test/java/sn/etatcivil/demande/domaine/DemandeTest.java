package sn.etatcivil.demande.domaine;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static sn.etatcivil.demande.domaine.Fixtures.*;
import static sn.etatcivil.demande.domaine.StatutDemande.*;

/** Règles de gestion d'une demande, en Java pur. */
class DemandeTest {

    @Test
    void depotFigeLeTarifEtNettoieLaPersonne() {
        Demande d = demande();
        assertThat(d.statut()).isEqualTo(SOUMISE);
        assertThat(d.montant()).isEqualTo(1000);
        assertThat(d.personne().nom()).isEqualTo("Diallo");
        assertThat(d.personne().nomConjoint()).isNull();   // sans objet pour une naissance
        assertThat(d.personne().dateEvenement()).isNull();
        assertThat(d.nouveauxChangements()).extracting(ChangementStatut::nouveau).containsExactly(SOUMISE);
    }

    @Test
    void reglesDeDepot() {
        PersonneConcernee sansLieu = new PersonneConcernee("Diallo", "Fatou", "F", LocalDate.of(1998, 3, 14), " ",
                null, null, null, null, null, null, null);
        assertThatThrownBy(() -> Demande.deposer("R", FATOU, extraitNaissance(), motif(), null, LienDemandeur.MOI_MEME,
                FormatDocument.NUMERIQUE, null, null, sansLieu, MAINTENANT)).hasMessageContaining("lieu de naissance");
        assertThatThrownBy(() -> Demande.deposer("R", FATOU, extraitNaissance(), new Motif(9L, "Autre", 99), " ",
                LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE, null, null, fatou(), MAINTENANT)).hasMessageContaining("Précisez");
        assertThatThrownBy(() -> Demande.deposer("R", FATOU, extraitNaissance(), motif(), null, LienDemandeur.MOI_MEME,
                FormatDocument.PHYSIQUE, "", "Dakar", fatou(), MAINTENANT)).hasMessageContaining("livraison");
    }

    @Test
    void parcoursCompletEtHistorique() {
        Demande d = demande();
        d.valider(DELEGUE, MAINTENANT);
        assertThat(d.enregistrerPaiement(1000, "PayDunya", MAINTENANT)).isTrue();
        assertThat(d.enregistrerPaiement(1000, "doublon", MAINTENANT)).isFalse(); // idempotent
        d.marquerIntrouvable("rien", Acteur.SYSTEME, MAINTENANT);
        d.marquerIntrouvable("toujours rien", AGENT, MAINTENANT);           // nouvelle tentative tracée
        d.rattacherActe(acte(true, TypeActe.NAISSANCE), AGENT, "manuel", MAINTENANT);
        d.signer("ABCDE-23456", OFFICIER, MAINTENANT);
        assertThat(d.statut()).isEqualTo(SIGNEE);
        assertThat(d.signataire()).isEqualTo("Mariama Sow");
        assertThat(d.historique()).extracting(ChangementStatut::nouveau)
                .containsExactly(SOUMISE, A_PAYER, PAYEE, ACTE_INTROUVABLE, ACTE_INTROUVABLE, ACTE_TROUVE, SIGNEE);
    }

    @Test
    void transitionsEtRolesControles() {
        Demande d = demande();
        assertThatThrownBy(() -> d.signer("X", OFFICIER, MAINTENANT)).isInstanceOf(RegleMetierException.class);
        assertThatThrownBy(() -> d.valider(AGENT, MAINTENANT)).hasMessageContaining("rôle");
        assertThatThrownBy(() -> d.rejeter("non", AGENT, MAINTENANT)).hasMessageContaining("rôle");
        assertThat(d.enregistrerPaiement(1000, "trop tôt", MAINTENANT)).isFalse();
        d.valider(DELEGUE, MAINTENANT);
        assertThatThrownBy(() -> d.enregistrerPaiement(200, "PayDunya", MAINTENANT)).hasMessageContaining("Montant");
        assertThatThrownBy(() -> d.annuler("u-autre", MAINTENANT)).isInstanceOf(DemandeIntrouvableException.class);
        d.annuler("u-fatou", MAINTENANT);
        assertThat(d.statut().estTerminal()).isTrue();
    }

    @Test
    void unActeNonValideOuDUnAutreTypeEstRefuse() {
        Demande d = demande();
        d.valider(DELEGUE, MAINTENANT);
        d.enregistrerPaiement(1000, "ok", MAINTENANT);
        assertThatThrownBy(() -> d.rattacherActe(acte(false, TypeActe.NAISSANCE), AGENT, null, MAINTENANT))
                .hasMessageContaining("pas encore validé");
        assertThatThrownBy(() -> d.rattacherActe(acte(true, TypeActe.MARIAGE), AGENT, null, MAINTENANT))
                .hasMessageContaining("mariage");
        d.rattacherActe(acte(true, TypeActe.NAISSANCE), Acteur.SYSTEME, null, MAINTENANT);
        d.contesterActe("homonyme", OFFICIER, MAINTENANT);
        assertThat(d.statut()).isEqualTo(ACTE_INTROUVABLE);
        assertThat(d.acteId()).isNull();
        d.rejeter("acte inexistant", AGENT, MAINTENANT);
        assertThat(d.motifRejet()).isEqualTo("acte inexistant");
    }
}
