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
    TypeDocument typeCourant = extraitNaissance();
    SituationCivile situation = new SituationCivile(List.of(), List.of());

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
        public List<TypeDocument> documents(boolean d) { return List.of(typeCourant); }
        public Optional<TypeDocument> document(Long id) { return Optional.of(typeCourant); }
        public void enregistrer(TypeDocument t) {}
        public List<Motif> motifs() { return List.of(Fixtures.motif()); }
        public Optional<Motif> motif(Long id) { return Optional.of(Fixtures.motif()); }
    };

    final RegistreCivil registre = new RegistreCivil() {
        public ResultatRecherche rechercher(TypeActe t, PersonneConcernee p, OrigineActe o) { return reponseRegistre; }
        public SituationCivile situation(String nom, String prenom, java.time.LocalDate naissance) { return situation; }
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

    Demande payee() {
        Demande d = deposer();
        service.valider(d.id(), DELEGUE);
        service.enregistrerPaiement(d.id(), 1000, "PayDunya");
        return d;
    }

    @Test
    void unCertificatDeCelibatVerifieLesMariagesDuRegistre() {
        typeCourant = Fixtures.document(ModeleDocument.CERTIFICAT_CELIBAT, "Certificat de célibat");
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.TROUVE, List.of(acte(true, TypeActe.NAISSANCE)));

        // un mariage, même dissous, empêche le certificat de célibat : un agent doit trancher
        situation = new SituationCivile(List.of(new SituationCivile.Mariage("M-2000-000001", "Seynabou Diouf", true)), List.of());
        Demande mariee = payee();
        service.rechercher(mariee.id(), Acteur.SYSTEME);
        assertThat(stock.get(mariee.id()).statut()).isEqualTo(StatutDemande.ACTE_INTROUVABLE);
        assertThat(stock.get(mariee.id()).noteRecherche()).contains("M-2000-000001").contains("célibat");

        // sans mariage : l'acte de naissance est rattaché et le constat figure sur le document
        situation = new SituationCivile(List.of(), List.of());
        Demande celibataire = payee();
        service.rechercher(celibataire.id(), Acteur.SYSTEME);
        assertThat(stock.get(celibataire.id()).statut()).isEqualTo(StatutDemande.ACTE_TROUVE);
        service.signer(celibataire.id(), OFFICIER);
        assertThat(documents).singleElement().satisfies(doc -> {
            assertThat(doc.modele()).isEqualTo("CERTIFICAT_CELIBAT");
            assertThat(doc.constat()).contains("Aucun acte de mariage");
            assertThat(doc.motif()).isEqualTo("Constitution d'un dossier administratif");
        });
    }

    @Test
    void laCapaciteMatrimonialeToleranteAuxMariagesDissousEtLaVieAuxDeces() {
        typeCourant = Fixtures.document(ModeleDocument.CERTIFICAT_CAPACITE_MATRIMONIALE, "Certificat de capacité matrimoniale");
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.TROUVE, List.of(acte(true, TypeActe.NAISSANCE)));
        situation = new SituationCivile(List.of(new SituationCivile.Mariage("M-2000-000001", "Seynabou Diouf", true)), List.of());
        Demande divorce = payee();
        service.rechercher(divorce.id(), Acteur.SYSTEME);
        assertThat(stock.get(divorce.id()).statut()).isEqualTo(StatutDemande.ACTE_TROUVE);

        situation = new SituationCivile(List.of(new SituationCivile.Mariage("M-2010-000001", "Coumba Ndiaye", false)), List.of());
        Demande marie = payee();
        service.rechercher(marie.id(), Acteur.SYSTEME);
        assertThat(stock.get(marie.id()).noteRecherche()).contains("option matrimoniale");
        // l'agent vérifie l'option de polygamie et rattache : l'officier peut alors signer
        service.rattacher(marie.id(), "N-1972-000001", AGENT);
        service.signer(marie.id(), OFFICIER);

        typeCourant = Fixtures.document(ModeleDocument.CERTIFICAT_VIE, "Certificat de vie");
        situation = new SituationCivile(List.of(), List.of("DC-2025-000001"));
        Demande decede = payee();
        service.rechercher(decede.id(), Acteur.SYSTEME);
        assertThat(stock.get(decede.id()).noteRecherche()).contains("décès");
        service.rattacher(decede.id(), "N-1972-000001", AGENT);
        assertThatThrownBy(() -> service.signer(decede.id(), OFFICIER)).hasMessageContaining("décès");
    }
}
