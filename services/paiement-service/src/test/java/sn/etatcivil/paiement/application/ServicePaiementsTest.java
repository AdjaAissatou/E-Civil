package sn.etatcivil.paiement.application;

import org.junit.jupiter.api.Test;
import sn.etatcivil.paiement.application.port.sortie.*;
import sn.etatcivil.paiement.application.service.ServicePaiements;
import sn.etatcivil.paiement.domaine.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class ServicePaiementsTest {

    final Map<String, Paiement> paiements = new LinkedHashMap<>();
    final Map<UUID, DemandePayable> demandes = new HashMap<>();
    final List<String> publies = new ArrayList<>();
    EtatFacture etat = new EtatFacture(EtatFacture.Etat.EN_ATTENTE, 0, null);
    int factures;

    final PasserellePaiement passerelle = new PasserellePaiement() {
        public Facture creerFacture(int m, String d, String r, Retours ret) { factures++; return new Facture("tok" + factures, "https://pay/" + factures); }
        public EtatFacture consulter(String token) { return etat; }
        public boolean notificationAuthentique(String s) { return "signature-valide".equals(s); }
        public boolean configuree() { return true; }
    };

    final ServicePaiements service = new ServicePaiements(
            new PaiementRepository() {
                public void enregistrer(Paiement p) { paiements.put(p.token(), p); }
                public Optional<Paiement> parToken(String t) { return Optional.ofNullable(paiements.get(t)); }
                public List<Paiement> deLaDemande(UUID id) { return paiements.values().stream().filter(p -> p.demandeId().equals(id)).toList(); }
            },
            new DemandePayableRepository() {
                public void enregistrer(DemandePayable d) { demandes.put(d.demandeId(), d); }
                public Optional<DemandePayable> parId(UUID id) { return Optional.ofNullable(demandes.get(id)); }
            },
            passerelle, id -> new PasserellePaiement.Retours("r", "a", "n"),
            (p, d) -> publies.add(d.reference()), () -> Instant.parse("2026-11-08T10:00:00Z"));

    @Test
    void parcoursDePaiement() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> service.initier(id, "u-fatou")).hasMessageContaining("pas en attente");

        service.statutChange(id, "DEM-2026-000001", "u-fatou", "Extrait", 1000, "A_PAYER");
        String url = service.initier(id, "u-fatou");
        assertThat(service.initier(id, "u-fatou")).isEqualTo(url);   // facture réutilisée
        assertThat(factures).isEqualTo(1);

        assertThatThrownBy(() -> service.recevoirNotification("fausse", "tok1")).hasMessageContaining("non authentifiée");
        service.confirmer("tok1");
        assertThat(publies).isEmpty();                                // PayDunya : en attente

        etat = new EtatFacture(EtatFacture.Etat.PAYEE, 1000, "recu");
        service.recevoirNotification("signature-valide", "tok1");
        service.confirmer("tok1");                                     // retour navigateur après l'IPN
        assertThat(publies).containsExactly("DEM-2026-000001");      // publié une seule fois
        assertThat(demandes.get(id).payable()).isFalse();
        assertThat(service.paiements(id, "u-autre", false)).isEmpty();
        assertThat(service.paiements(id, "u-fatou", false)).hasSize(1);
    }
}
