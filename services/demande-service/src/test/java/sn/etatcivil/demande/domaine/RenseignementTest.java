package sn.etatcivil.demande.domaine;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static sn.etatcivil.demande.domaine.Fixtures.*;

class RenseignementTest {

    static Demande introuvable() {
        Demande d = demande();
        d.valider(DELEGUE, MAINTENANT);
        d.enregistrerPaiement(1000, "PayDunya", MAINTENANT);
        d.marquerIntrouvable("Aucun acte validé", Acteur.SYSTEME, MAINTENANT);
        return d;
    }

    @Test
    void consultations_puis_conclusion_non_trouve() {
        Demande d = introuvable();
        Renseignement r = Renseignement.ouvrir(d, MAINTENANT);
        assertThatThrownBy(() -> r.conclureNonTrouve("Rien", AGENT, d, MAINTENANT)).hasMessageContaining("au moins un registre");
        assertThatThrownBy(() -> r.consigner(1L, "Naissances 1998", ResultatConsultation.ABSENT, null, null, DELEGUE, d, MAINTENANT))
                .hasMessageContaining("habilitations");
        assertThatThrownBy(() -> r.consigner(2L, "Naissances 1998, greffe", ResultatConsultation.PAGE_ILLISIBLE, "34", " ", AGENT, d,
                MAINTENANT)).hasMessageContaining("Décrivez");
        r.consigner(1L, "Naissances 1998, centre", ResultatConsultation.ABSENT, null, null, AGENT, d, MAINTENANT);
        assertThat(r.agentNom()).isEqualTo("Ibrahima Fall");                      // pris en charge à la première consultation
        assertThat(r.etat(d)).isEqualTo(EtatRenseignement.EN_COURS);

        String motif = r.conclureNonTrouve("Aucune naissance à ce nom en 1997, 1998 et 1999", AGENT, d, MAINTENANT);
        assertThat(motif).startsWith("Acte introuvable dans les registres papier (1 registre consulté)");
        d.rejeter(motif, AGENT, MAINTENANT);
        assertThat(r.etat(d)).isEqualTo(EtatRenseignement.NON_TROUVE);
        assertThatThrownBy(() -> r.consigner(1L, "x", ResultatConsultation.ABSENT, null, null, AGENT, d, MAINTENANT))
                .hasMessageContaining("clos");
    }

    @Test
    void un_acte_present_empeche_de_conclure_et_le_rattachement_clot_le_dossier() {
        Demande d = introuvable();
        Renseignement r = Renseignement.ouvrir(d, MAINTENANT);
        r.consigner(1L, "Naissances 1998", ResultatConsultation.ACTE_PRESENT, "34", null, AGENT, d, MAINTENANT);
        assertThatThrownBy(() -> r.conclureNonTrouve("x", AGENT, d, MAINTENANT)).hasMessageContaining("transcrivez");
        r.definirPriorite(true, AGENT, d);
        assertThat(r.urgent()).isTrue();
        d.rattacherActe(acte(true, TypeActe.NAISSANCE), AGENT, "manuel", MAINTENANT);
        assertThat(r.etat(d)).isEqualTo(EtatRenseignement.TROUVE);
    }

    @Test
    void la_recherche_automatique_doit_avoir_echoue() {
        Demande d = demande();
        d.valider(DELEGUE, MAINTENANT);
        d.enregistrerPaiement(1000, "PayDunya", MAINTENANT);
        Renseignement r = Renseignement.ouvrir(d, MAINTENANT);
        r.consigner(1L, "Naissances 1998", ResultatConsultation.ABSENT, null, null, AGENT, d, MAINTENANT);
        assertThatThrownBy(() -> r.conclureNonTrouve("x", AGENT, d, MAINTENANT)).hasMessageContaining("recherche automatique");
        assertThatThrownBy(() -> Renseignement.ouvrir(demande(), MAINTENANT)).hasMessageContaining("pas en attente");
    }
}
