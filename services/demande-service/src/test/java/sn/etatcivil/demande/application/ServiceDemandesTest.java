package sn.etatcivil.demande.application;

import org.junit.jupiter.api.Test;
import sn.etatcivil.demande.application.port.entree.DemandeADeposer;
import sn.etatcivil.demande.application.port.entree.PageDemandee;
import sn.etatcivil.demande.application.port.entree.FiltreDemandes;
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
        public PageResultat<Demande> rechercher(FiltreDemandes f, PageDemandee p) { return null; }
        public Map<StatutDemande, Long> compterParStatut(FiltreDemandes f) { return Map.of(); }
        public List<UUID> identifiants(StatutDemande s, TypeActe t, int l) {
            return stock.values().stream().filter(d -> d.statut() == s && (t == null || d.typeActe() == t)).map(Demande::id).toList();
        }
        public long compter(StatutDemande s) { return 0; }
    };

    final CatalogueRepository catalogue = new CatalogueRepository() {
        public List<TypeDocument> documents(boolean d) { return List.of(typeCourant); }
        public Optional<TypeDocument> document(Long id) { return Optional.of(typeCourant); }
        public Optional<TypeDocument> documentParCode(String code) { return Optional.empty(); }
        public Long enregistrer(TypeDocument t) { return 1L; }
        public List<Nature> natures() { return List.of(); }
        public Optional<Nature> nature(Long id) { return Optional.empty(); }
        public Optional<Nature> natureParCode(String code) { return Optional.empty(); }
        public Long enregistrer(Nature n) { return 1L; }
        public List<Motif> motifs() { return List.of(Fixtures.motif()); }
        public Optional<Motif> motif(Long id) { return Optional.of(Fixtures.motif()); }
        public Long enregistrer(Motif m) { return 1L; }
    };

    final RegistreCivil registre = new RegistreCivil() {
        public ResultatRecherche rechercher(TypeActe t, PersonneConcernee p, OrigineActe o) { return reponseRegistre; }
        public SituationCivile situation(String nom, String prenom, java.time.LocalDate naissance) { return situation; }
        public Optional<ActeRetrouve> parNumero(String n) { return Optional.of(acte(true, TypeActe.NAISSANCE)); }
        public Optional<ActeRetrouve> parId(UUID id) { return Optional.of(acte(true, TypeActe.NAISSANCE)); }
    };

    final Map<UUID, byte[]> photos = new HashMap<>();
    final List<PieceIdentite> piecesStockees = new ArrayList<>();
    final PiecesIdentiteRepository piecesEnMemoire = new PiecesIdentiteRepository() {
        public void enregistrer(PieceIdentite p, byte[] c) { piecesStockees.add(p); photos.put(p.id(), c); }
        public List<PieceIdentite> pourDemande(UUID d) { return piecesStockees.stream().filter(p -> p.demandeId().equals(d)).toList(); }
        public Optional<byte[]> contenu(UUID d, UUID p) { return Optional.ofNullable(photos.get(p)); }
        public int purger(Set<StatutDemande> s, java.time.Instant a, java.time.Instant m) { return 0; }
    };

    final ServiceDemandes service = new ServiceDemandes(depot, catalogue, annee -> "DEM-" + annee + "-" + (++compteur),
            registre, doc -> { documents.add(doc); return "ABCDE-23456"; },
            (d, c) -> evenements.add(c.nouveau().name()), () -> MAINTENANT, diaspora(), piecesEnMemoire,
            (contenu, libelle) -> {
                if (contenu[0] != 'J') throw new RegleMetierException(libelle + " : le fichier n'est pas une photo");
                return new TraitementImages.Image(contenu, "image/jpeg");
            });


    static ReferentielDiaspora diaspora() {
        MissionDiplomatique paris = new MissionDiplomatique(7L, "Consulat", "Consulat du Sénégal à Paris", 5L, "France", "Paris",
                "75008", null, "12 Rue de Tilsitt, 75008 Paris", null, null, List.of(), true);
        return new ReferentielDiaspora() {
            public List<Pays> pays() { return List.of(); }
            public List<String> typesMission() { return List.of("Consulat"); }
            public List<String> zones() { return List.of(); }
            public List<MissionDiplomatique> missions() { return List.of(paris); }
            public Optional<MissionDiplomatique> mission(Long id) { return id == 7L ? Optional.of(paris) : Optional.empty(); }
            public Long enregistrer(MissionDiplomatique m) { return 7L; }
            public Long ajouterPays(String n, String c, String co, String ca, String z) { return 1L; }
        };
    }

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

    Demande deposerAvec(ComplementsDemande c) {
        return service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE,
                null, null, fatou(), c), FATOU);
    }

    static ComplementsDemande complements(String residence, String temoin, String precisions) {
        return new ComplementsDemande(null, residence, null, null, null, null, null, null, null, null, temoin, null,
                null, null, null, null, null, null, precisions);
    }

    @Test
    void unDocumentSurComparutionNeChercheAucunActe() {
        typeCourant = new TypeDocument(1L, "CERTIFICAT_COUTUME", "Certificat de coutume pour mariage mixte", "", 15L,
                "Coutume", TypeActe.NAISSANCE, ModeleDocument.CERTIFICAT_COUTUME, null, null,
                java.util.EnumSet.of(Champ.PRECISIONS), 500, 1, 30, null, null, false, null, true);
        assertThatThrownBy(this::deposer).hasMessageContaining("précisions");
        Demande d = deposerAvec(complements(null, null, "Coutume wolof ; futur époux de nationalité française"));
        assertThat(d.dureeValiditeJours()).isEqualTo(30);
        service.valider(d.id(), DELEGUE);
        service.enregistrerPaiement(d.id(), 500, "PayDunya");
        service.rechercher(d.id(), Acteur.SYSTEME);
        assertThat(stock.get(d.id()).statut()).isEqualTo(StatutDemande.ACTE_TROUVE);
        assertThat(stock.get(d.id()).acteId()).isNull();
        service.signer(d.id(), OFFICIER);
        assertThat(documents).singleElement().satisfies(doc -> {
            assertThat(doc.acteId()).isNull();
            assertThat(doc.modele()).isEqualTo("CERTIFICAT_COUTUME");
            assertThat(doc.complements().precisions()).startsWith("Coutume wolof");
            assertThat(doc.dureeValiditeJours()).isEqualTo(30);
        });
    }

    @Test
    void unCertificatDeDomicileExigeLAdresseEtUnTemoin() {
        typeCourant = new TypeDocument(1L, "CERTIFICAT_DOMICILE_ELECTIONS", "Certificat de domicile pour élections", "", 14L,
                "Domicile", TypeActe.NAISSANCE, ModeleDocument.CERTIFICAT_DOMICILE, null, null,
                java.util.EnumSet.of(Champ.DOMICILE, Champ.TEMOINS), 500, 1, 30, null, null, false, null, true);
        assertThatThrownBy(() -> deposerAvec(complements(null, "Awa Ndiaye", null))).hasMessageContaining("domicile");
        assertThatThrownBy(() -> deposerAvec(complements("Sicap Liberté 2, Dakar", null, null))).hasMessageContaining("témoin");
        Demande d = deposerAvec(complements("Sicap Liberté 2, Dakar", "Awa Ndiaye", "ignoré"));
        assertThat(d.complements().residence()).isEqualTo("Sicap Liberté 2, Dakar");
        assertThat(d.complements().precisions()).as("rubrique non demandée").isNull();
    }

    @Test
    void unExtraitDeMentionExigeLaMentionEnMargeDeLActe() {
        typeCourant = new TypeDocument(1L, "MENTION_DIVORCE", "Extrait de mention de divorce", "", 9L, "Mention marginale",
                TypeActe.MARIAGE, ModeleDocument.EXTRAIT_MENTION, null, "DIVORCE", java.util.EnumSet.of(Champ.CONJOINT),
                500, 1, 90, null, null, false, null, true);
        PersonneConcernee couple = new PersonneConcernee("Mbaye", "Babacar", "M", null, null, null, null, null, null,
                "Diouf", "Seynabou", java.time.LocalDate.of(2000, 8, 12));
        Demande d = service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE,
                null, null, couple), FATOU);
        assertThat(d.complements().typeMention()).isEqualTo("DIVORCE");
        service.valider(d.id(), DELEGUE);
        service.enregistrerPaiement(d.id(), 500, "PayDunya");
        ActeRetrouve mariage = new ActeRetrouve(java.util.UUID.randomUUID(), "M-2000-000001", TypeActe.MARIAGE, true, Set.of());
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.TROUVE, List.of(mariage));
        service.rechercher(d.id(), Acteur.SYSTEME);
        assertThat(stock.get(d.id()).statut()).isEqualTo(StatutDemande.ACTE_INTROUVABLE);
        assertThat(stock.get(d.id()).noteRecherche()).contains("aucune mention « divorce »");

        ActeRetrouve divorce = new ActeRetrouve(mariage.id(), "M-2000-000001", TypeActe.MARIAGE, true, Set.of("DIVORCE"));
        reponseRegistre = new RegistreCivil.ResultatRecherche(RegistreCivil.Resultat.TROUVE, List.of(divorce));
        service.rechercher(d.id(), Acteur.SYSTEME);
        assertThat(stock.get(d.id()).statut()).isEqualTo(StatutDemande.ACTE_TROUVE);
    }

    @Test
    void unDocumentPapierSeRetireALaMissionDiplomatique() {
        Demande d = service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.PHYSIQUE,
                null, null, fatou(), ComplementsDemande.AUCUN, 7L), FATOU);
        assertThat(d.adresseLivraison()).isEqualTo("Retrait à la mission : Consulat du Sénégal à Paris, 12 Rue de Tilsitt, 75008 Paris");
        assertThat(d.villeLivraison()).isEqualTo("Paris (France)");
        assertThatThrownBy(() -> service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.PHYSIQUE,
                null, null, fatou(), ComplementsDemande.AUCUN, 99L), FATOU)).hasMessageContaining("Mission");
    }

    static PiecesDeposees.Photo photo() { return new PiecesDeposees.Photo("JPEG...".getBytes(), "image/jpeg"); }

    @Test
    void lesDocumentsSensiblesExigentLaPieceDIdentiteEtUnSelfieVerifiesParLeDelegue() {
        typeCourant = new TypeDocument(1L, "COPIE_NAISSANCE", "Copie littérale d'acte de naissance", "", 6L, "Copie",
                TypeActe.NAISSANCE, ModeleDocument.COPIE_INTEGRALE, null, null, java.util.EnumSet.of(Champ.PARENTS),
                2000, 3, 365, null, null, false, null, true, true);
        DemandeADeposer commande = new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME, FormatDocument.NUMERIQUE,
                null, null, fatou());
        assertThatThrownBy(() -> service.deposer(commande, FATOU)).hasMessageContaining("pièce d'identité");
        // carte d'identité : recto et verso exigés, plus le selfie
        Map<PieceIdentite.Nature, PiecesDeposees.Photo> sansVerso = new EnumMap<>(PieceIdentite.Nature.class);
        sansVerso.put(PieceIdentite.Nature.RECTO, photo());
        sansVerso.put(PieceIdentite.Nature.SELFIE, photo());
        assertThatThrownBy(() -> service.deposer(commande, FATOU, new PiecesDeposees(PieceIdentite.TypePiece.CNI, sansVerso)))
                .hasMessageContaining("verso");
        Map<PieceIdentite.Nature, PiecesDeposees.Photo> sansSelfie = new EnumMap<>(PieceIdentite.Nature.class);
        sansSelfie.put(PieceIdentite.Nature.RECTO, photo());
        assertThatThrownBy(() -> service.deposer(commande, FATOU, new PiecesDeposees(PieceIdentite.TypePiece.PASSEPORT, sansSelfie)))
                .hasMessageContaining("selfie");
        Map<PieceIdentite.Nature, PiecesDeposees.Photo> pdf = new EnumMap<>(sansVerso);
        pdf.put(PieceIdentite.Nature.RECTO, new PiecesDeposees.Photo("%PDF".getBytes(), "application/pdf"));
        assertThatThrownBy(() -> service.deposer(commande, FATOU, new PiecesDeposees(PieceIdentite.TypePiece.PASSEPORT, pdf)))
                .hasMessageContaining("JPEG ou PNG");
        Map<PieceIdentite.Nature, PiecesDeposees.Photo> fausse = new EnumMap<>(sansVerso);
        fausse.put(PieceIdentite.Nature.SELFIE, new PiecesDeposees.Photo("<script>".getBytes(), "image/jpeg"));
        assertThatThrownBy(() -> service.deposer(commande, FATOU, new PiecesDeposees(PieceIdentite.TypePiece.PASSEPORT, fausse)))
                .hasMessageContaining("pas une photo");

        Demande d = service.deposer(commande, FATOU, new PiecesDeposees(PieceIdentite.TypePiece.PASSEPORT, sansVerso));
        assertThat(d.verificationIdentite()).isTrue();
        assertThat(piecesStockees).extracting(PieceIdentite::nature)
                .containsExactlyInAnyOrder(PieceIdentite.Nature.RECTO, PieceIdentite.Nature.SELFIE);
        assertThat(piecesStockees).allSatisfy(p -> assertThat(p.empreinte()).hasSize(64));

        // le délégué doit attester avoir comparé le selfie à la pièce
        assertThatThrownBy(() -> service.valider(d.id(), DELEGUE)).hasMessageContaining("Comparez le selfie");
        service.valider(d.id(), DELEGUE, true);
        assertThat(stock.get(d.id()).historique()).last().extracting(ChangementStatut::commentaire).asString()
                .contains("Identité vérifiée");
    }

    @Test
    void unExtraitNeDemandeAucunePiece() {
        Map<PieceIdentite.Nature, PiecesDeposees.Photo> m = new EnumMap<>(PieceIdentite.Nature.class);
        m.put(PieceIdentite.Nature.SELFIE, photo());
        assertThatThrownBy(() -> service.deposer(new DemandeADeposer(1L, 1L, null, LienDemandeur.MOI_MEME,
                FormatDocument.NUMERIQUE, null, null, fatou()), FATOU, new PiecesDeposees(PieceIdentite.TypePiece.CNI, m)))
                .hasMessageContaining("aucune pièce");
        Demande d = deposer();
        service.valider(d.id(), DELEGUE);
        assertThat(stock.get(d.id()).statut()).isEqualTo(StatutDemande.A_PAYER);
    }
}
