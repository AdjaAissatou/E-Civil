package sn.etatcivil.demande.application;

import org.junit.jupiter.api.Test;
import sn.etatcivil.demande.application.port.entree.DemandeADeposer;
import sn.etatcivil.demande.application.port.entree.PageDemandee;
import sn.etatcivil.demande.application.port.entree.PageResultat;
import sn.etatcivil.demande.application.port.sortie.*;
import sn.etatcivil.demande.application.service.ServiceDemandes;
import sn.etatcivil.demande.domaine.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static sn.etatcivil.demande.domaine.Fixtures.*;

/** Cas d'usage testés avec des ports en mémoire : ni base, ni réseau, ni Spring. */
class ServiceDemandesTest {

    final Map<UUID, Demande> stock = new LinkedHashMap<>();
    final List<String> evenements = new ArrayList<>();
    final List<ServiceDocuments.DocumentADelivrer> documents = new ArrayList<>();
    RegistreCivil.ResultatRecherche reponseRegistre =
            new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.NON_TROUVE, List.of());
    int compteur;

    final DemandeRepository depot = new DemandeRepository() {
        public void enregistrer(Demande d) { stock.put(d.id(), d); }
        public Optional<Demande> parId(UUID id) { return Optional.ofNullable(stock.get(id)); }
        public List<Demande> parDemandeur(String id) { return stock.values().stream().filter(d -> d.demandeur().id().equals(id)).toList(); }
        public PageResultat<Demande> rechercher(Set<StatutDemande> s, String r, PageDemandee p) { return null; }
        public List<UUID> identifiants(StatutDemande s, TypeActe t, int l) {
            return stock.values().stream().filter(d -> d.statut() == s && (t == null || d.typeActe() == t)).map(Demande::id).toList();
        }
        public long compter(StatutDemande s) { return 0; }
    };

    final CatalogueRepository catalogue = new CatalogueRepository() {
        public List<TypeDocument> documents(boolean d) { return List.of(extraitNaissance()); }
        public Optional<TypeDocument> document(Long id) { return Optional.of(extraitNaissance()); }
        public void enregistrer(TypeDocument t) {}
        public List<Motif> motifs() { return List.of(Fixtures.motif()); }
        public Optional<Motif> motif(Long id) { return Optional.of(Fixtures.motif()); }
    };

    final RegistreCivil registre = new RegistreCivil() {
        public ResultatRecherche rechercher(TypeActe t, PersonneConcernee p) { return reponseRegistre; }
        public Optional<ActeRetrouve> parNumero(String n) { return Optional.of(acte(true, TypeActe.NAISSANCE)); }
        public Optional<ActeRetrouve> parId(UUID id) { return Optional.of(acte(true, TypeActe.NAISSANCE)); }
    };

    final ServiceDemandes service = new ServiceDemandes(depot, catalogue, annee -> "DEM-" + annee + "-" + (++compteur),
            registre, doc -> { documents.add(doc); return "ABCDE-23456"; },
            (d, c) -> evenements.add(c.nouveau().name()), () -> MAINTENANT);

    Demande deposer() {
        return service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE,
                null, null, fatou()), FATOU);
    }

    @Test
    void parcoursAvecEvenementsPublies() {
        Demande d = deposer();
        assertThat(d.reference()).isEqualTo("DEM-2026-1");
        service.valider(d.id(), DELEGUE);
        assertThat(service.enregistrerPaiement(d.id(), 1000, "PayDunya")).isTrue();
        service.rechercher(d.id(), Acteur.SYSTEME);
        assertThat(stock.get(d.id()).noteRecherche()).contains("Aucun acte");

        // Un acte de naissance est validé : la demande est relancée et l'acte trouvé
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.TROUVE, List.of(acte(true, TypeActe.NAISSANCE)));
        assertThat(service.introuvables(TypeActe.NAISSANCE)).containsExactly(d.id());
        service.rechercher(d.id(), Acteur.SYSTEME);
        service.signer(d.id(), OFFICIER);

        assertThat(evenements).containsExactly("SOUMISE", "A_PAYER", "PAYEE", "ACTE_INTROUVABLE", "ACTE_TROUVE", "SIGNEE");
        assertThat(documents).singleElement().satisfies(doc -> {
            assertThat(doc.reference()).isEqualTo("DEM-2026-1");
            assertThat(doc.signataire()).isEqualTo("Mariama Sow");
            assertThat(doc.demandeurId()).isEqualTo("u-fatou");
        });
        assertThat(stock.get(d.id()).codeVerification()).isEqualTo("ABCDE-23456");
    }

    @Test
    void homonymesEtCloisonnement() {
        Demande d = deposer();
        assertThatThrownBy(() -> service.consulter(d.id(), "u-autre", false)).isInstanceOf(DemandeIntrouvableException.class);
        assertThat(service.consulter(d.id(), "u-del", true)).isNotNull();
        service.valider(d.id(), DELEGUE);
        service.enregistrerPaiement(d.id(), 1000, "ok");
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.AMBIGU,
                List.of(acte(true, TypeActe.NAISSANCE), new ActeRetrouve(UUID.randomUUID(), "N-1998-000043", TypeActe.NAISSANCE, true)));
        service.rechercher(d.id(), Acteur.SYSTEME);
        assertThat(stock.get(d.id()).noteRecherche()).contains("N-1998-000042, N-1998-000043");
        service.rattacher(d.id(), "n-1998-000042", AGENT);
        assertThat(stock.get(d.id()).statut()).isEqualTo(StatutDemande.ACTE_TROUVE);
    }
}
