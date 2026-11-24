package sn.etatcivil.demande.application.service;

import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.entree.*;
import sn.etatcivil.demande.application.port.sortie.*;
import sn.etatcivil.demande.domaine.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/** Implémentation des cas d'usage : orchestre le domaine et les ports, sans aucune technologie. */
@CasDUsage
public class ServiceDemandes implements GererMesDemandes, ConsulterDemandes, InstruireDemandes, TraiterPaiements,
        RechercherActes, SignerDemandes, GererCatalogue {

    private final DemandeRepository demandes;
    private final CatalogueRepository catalogue;
    private final GenerateurReference references;
    private final RegistreCivil registre;
    private final ServiceDocuments documents;
    private final EvenementsDemande evenements;
    private final Horloge horloge;
    private final ReferentielDiaspora diaspora;

    public ServiceDemandes(DemandeRepository demandes, CatalogueRepository catalogue, GenerateurReference references,
                           RegistreCivil registre, ServiceDocuments documents, EvenementsDemande evenements,
                           Horloge horloge, ReferentielDiaspora diaspora) {
        this.diaspora = diaspora;
        this.demandes = demandes;
        this.catalogue = catalogue;
        this.references = references;
        this.registre = registre;
        this.documents = documents;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    // ---------------------------------------------------------------- citoyen

    @Override
    public Demande deposer(DemandeADeposer c, Demandeur demandeur) {
        TypeDocument type = catalogue.document(c.typeDocumentId())
                .orElseThrow(() -> new RegleMetierException("Ce document n'est pas disponible"));
        Motif motif = catalogue.motif(c.motifId()).orElseThrow(() -> new RegleMetierException("Motif inconnu"));
        Instant maintenant = horloge.maintenant();
        String reference = references.suivante(maintenant.atZone(ZoneId.of("Africa/Dakar")).getYear());
        String adresse = c.adresseLivraison(), ville = c.villeLivraison();
        if (c.missionRetraitId() != null && c.format() == FormatDocument.PHYSIQUE) {
            // document papier retiré à la mission diplomatique (diaspora) : l'adresse est celle de la mission
            MissionDiplomatique m = diaspora.mission(c.missionRetraitId()).filter(MissionDiplomatique::active)
                    .orElseThrow(() -> new RegleMetierException("Mission diplomatique inconnue"));
            adresse = m.adresseDeRetrait();
            ville = m.ville() + " (" + m.pays() + ")";
        }
        Demande d = Demande.deposer(reference, demandeur, type, motif, c.precisionMotif(), c.lien(), c.format(),
                adresse, ville, c.personne(), c.complements(), maintenant);
        return sauver(d);
    }

    @Override
    public void annuler(UUID id, String demandeurId) {
        Demande d = trouver(id);
        d.annuler(demandeurId, horloge.maintenant());
        sauver(d);
    }

    @Override
    public List<Demande> mesDemandes(String demandeurId) {
        return demandes.parDemandeur(demandeurId);
    }

    @Override
    public Demande consulter(UUID id, String utilisateurId, boolean estAgent) {
        Demande d = trouver(id);
        if (!d.visiblePar(utilisateurId, estAgent)) throw new DemandeIntrouvableException();
        return d;
    }

    @Override
    public PageResultat<Demande> rechercher(FiltreDemandes filtre, PageDemandee page) {
        return demandes.rechercher(filtre, page);
    }

    @Override
    public Map<StatutDemande, Long> nombreParStatut(FiltreDemandes filtre) {
        Map<StatutDemande, Long> m = new EnumMap<>(StatutDemande.class);
        for (StatutDemande s : StatutDemande.values()) m.put(s, 0L);
        m.putAll(demandes.compterParStatut(filtre));
        return m;
    }

    @Override
    public Map<StatutDemande, Long> nombreParStatut() {
        Map<StatutDemande, Long> m = new EnumMap<>(StatutDemande.class);
        for (StatutDemande s : StatutDemande.values()) m.put(s, demandes.compter(s));
        return m;
    }

    // ---------------------------------------------------------------- délégué

    @Override
    public void valider(UUID id, Acteur delegue) {
        Demande d = trouver(id);
        d.valider(delegue, horloge.maintenant());
        sauver(d);
    }

    @Override
    public void rejeter(UUID id, String motif, Acteur agent) {
        Demande d = trouver(id);
        d.rejeter(motif, agent, horloge.maintenant());
        sauver(d);
    }

    @Override
    public void marquerLivree(UUID id, Acteur delegue) {
        Demande d = trouver(id);
        d.marquerLivree(delegue, horloge.maintenant());
        sauver(d);
    }

    // ---------------------------------------------------------------- paiement

    @Override
    public boolean enregistrerPaiement(UUID demandeId, int montant, String detail) {
        Demande d = trouver(demandeId);
        boolean nouveau = d.enregistrerPaiement(montant, detail, horloge.maintenant());
        if (nouveau) sauver(d);
        return nouveau;
    }

    // ---------------------------------------------------------------- registre

    @Override
    public Demande rechercher(UUID id, Acteur auteur) {
        Demande d = trouver(id);
        if (!d.enAttenteDeRecherche()) throw new RegleMetierException("Cette demande n'est pas en attente de recherche");
        Instant maintenant = horloge.maintenant();
        if (!d.modele().exigeActe()) {
            d.dispenserDActe(auteur, maintenant);
            return sauver(d);
        }
        RegistreCivil.ResultatRecherche r = registre.rechercher(d.typeActe(), d.personne(), d.origine());
        switch (r.resultat()) {
            case TROUVE -> {
                Optional<String> obstacle = obstacle(d).or(() -> mentionAbsente(d, r.actes().get(0)));
                if (obstacle.isPresent()) d.marquerIntrouvable(obstacle.get(), auteur, maintenant);
                else d.rattacherActe(r.actes().get(0), auteur, d.modele().estCertificat()
                        ? "Acte de naissance retrouvé ; " + situation(d).constat(d.modele()).toLowerCase()
                        : "Acte retrouvé dans le registre national", maintenant);
            }
            case AMBIGU -> d.marquerIntrouvable("Plusieurs actes correspondent : "
                    + r.actes().stream().map(ActeRetrouve::numero).collect(Collectors.joining(", "))
                    + ". Un agent doit choisir le bon.", auteur, maintenant);
            case NON_TROUVE -> d.marquerIntrouvable("Aucun acte validé ne correspond dans le registre national.", auteur, maintenant);
        }
        return sauver(d);
    }

    @Override
    public Demande rattacher(UUID id, String numeroActe, Acteur agent) {
        if (numeroActe == null || numeroActe.isBlank()) throw new RegleMetierException("Indiquez le numéro de l'acte");
        Demande d = trouver(id);
        if (d.statut() != StatutDemande.ACTE_INTROUVABLE) {
            throw new RegleMetierException("Seule une demande dont l'acte n'a pas été retrouvé peut être rattachée");
        }
        ActeRetrouve acte = registre.parNumero(numeroActe.trim().toUpperCase())
                .orElseThrow(() -> new RegleMetierException("Aucun acte ne porte le numéro " + numeroActe.trim()));
        d.rattacherActe(acte, agent, "Acte " + acte.numero() + " rattaché manuellement", horloge.maintenant());
        return sauver(d);
    }

    @Override
    public void contester(UUID id, String raison, Acteur officier) {
        Demande d = trouver(id);
        d.contesterActe(raison, officier, horloge.maintenant());
        sauver(d);
    }

    @Override
    public List<UUID> enAttenteDeRecherche() {
        return demandes.identifiants(StatutDemande.PAYEE, null, 50);
    }

    @Override
    public List<UUID> introuvables(TypeActe type) {
        return demandes.identifiants(StatutDemande.ACTE_INTROUVABLE, type, 200);
    }

    // ---------------------------------------------------------------- officier

    /**
     * L'acte est relu dans le registre au moment de signer, puis document-service le fige et le
     * signe (empreinte, code de vérification). La demande ne passe « signée » qu'une fois le document produit.
     */
    @Override
    public Demande signer(UUID id, Acteur officier) {
        Demande d = trouver(id);
        d.verifierSignable(officier);
        if (d.modele().exigeActe()) {
            ActeRetrouve acte = registre.parId(d.acteId())
                    .orElseThrow(() -> new RegleMetierException("L'acte rattaché n'existe plus dans le registre"));
            if (!acte.valide()) throw new RegleMetierException("L'acte " + acte.numero() + " n'est plus validé dans le registre");
            Optional<String> sansMention = mentionAbsente(d, acte);
            if (sansMention.isPresent()) throw new RegleMetierException(sansMention.get());
        }
        String constat = null;
        if (d.modele().estCertificat()) {
            SituationCivile situation = situation(d);
            // le célibat et la vie sont des constats stricts ; la capacité matrimoniale relève de l'officier
            Optional<String> obstacle = situation.obstacle(d.modele());
            if (obstacle.isPresent() && d.modele() != ModeleDocument.CERTIFICAT_CAPACITE_MATRIMONIALE) {
                throw new RegleMetierException(obstacle.get());
            }
            constat = situation.constat(d.modele());
        }
        String code = documents.delivrer(new ServiceDocuments.DocumentADelivrer(d.id(), d.reference(), d.demandeur().id(),
                d.libelleDocument(), d.modele().name(), d.acteId(), d.format().name(), officier.nomComplet(),
                d.libelleMotif(), constat, d.personne(), d.complements(), d.dureeValiditeJours(),
                d.complements().typeMention()));
        d.signer(code, officier, horloge.maintenant());
        return sauver(d);
    }

    // ---------------------------------------------------------------- catalogue

    @Override
    public List<TypeDocument> documents(boolean disponiblesSeulement) {
        return catalogue.documents(disponiblesSeulement);
    }

    @Override
    public TypeDocument document(Long id) {
        return catalogue.document(id).orElseThrow(() -> new RegleMetierException("Document inconnu"));
    }

    @Override
    public List<Motif> motifs(Long typeDocumentId) {
        return catalogue.motifs().stream()
                .filter(m -> typeDocumentId == null ? m.typeDocumentId() == null : m.concerne(typeDocumentId))
                .toList();
    }

    @Override
    public List<Motif> tousLesMotifs() {
        return catalogue.motifs();
    }

    @Override
    public TypeDocument modifier(Long id, int tarif, int delaiJours, boolean actif) {
        TypeDocument t = document(id);
        t.modifier(tarif, delaiJours, actif);
        catalogue.enregistrer(t);
        return t;
    }

    @Override
    public TypeDocument creerDocument(String code, TypeDocument.Caracteristiques c) {
        TypeDocument t = TypeDocument.creer(code, c);
        if (catalogue.documentParCode(t.code()).isPresent()) {
            throw new RegleMetierException("Un document porte déjà le code " + t.code());
        }
        verifierReferences(t, c);
        return document(catalogue.enregistrer(t));
    }

    @Override
    public TypeDocument modifierDocument(Long id, TypeDocument.Caracteristiques c) {
        TypeDocument t = document(id);
        t.modifier(c);
        verifierReferences(t, c);
        catalogue.enregistrer(t);
        return document(id);
    }

    /** La nature existe ; la copie désigne un original existant, de même type d'acte. */
    private void verifierReferences(TypeDocument t, TypeDocument.Caracteristiques c) {
        catalogue.nature(c.natureId()).orElseThrow(() -> new RegleMetierException("Nature inconnue"));
        if (c.copieDeId() != null) {
            TypeDocument original = catalogue.document(c.copieDeId())
                    .orElseThrow(() -> new RegleMetierException("Le document original désigné n'existe pas"));
            if (!original.estOriginal()) {
                throw new RegleMetierException("« " + original.libelle() + " » n'est pas un original : une copie désigne un original");
            }
            if (original.typeActe() != t.typeActe()) {
                throw new RegleMetierException("La copie et son original doivent porter sur le même type d'acte");
            }
        }
    }

    @Override
    public List<Nature> natures(boolean activesSeulement) {
        return catalogue.natures().stream().filter(n -> !activesSeulement || n.active())
                .sorted(Comparator.comparingInt(Nature::ordre).thenComparing(Nature::libelle)).toList();
    }

    @Override
    public Nature creerNature(String code, String libelle, String description, int ordre) {
        Nature n = Nature.creer(code, libelle, description, ordre);
        if (catalogue.natureParCode(n.code()).isPresent()) throw new RegleMetierException("Une nature porte déjà le code " + n.code());
        Long id = catalogue.enregistrer(n);
        return catalogue.nature(id).orElseThrow();
    }

    @Override
    public Nature modifierNature(Long id, String libelle, String description, int ordre, boolean active) {
        Nature n = catalogue.nature(id).orElseThrow(() -> new RegleMetierException("Nature inconnue"));
        n.modifier(libelle, description, ordre, active);
        catalogue.enregistrer(n);
        return n;
    }

    @Override
    public Motif creerMotif(String libelle, int ordre, String piecesRequises, Long typeDocumentId) {
        if (typeDocumentId != null) document(typeDocumentId);
        Motif m = new Motif(null, libelle.trim(), ordre, vide(piecesRequises), typeDocumentId);
        return catalogue.motif(catalogue.enregistrer(m)).orElseThrow();
    }

    @Override
    public Motif modifierMotif(Long id, String libelle, int ordre, String piecesRequises, Long typeDocumentId) {
        catalogue.motif(id).orElseThrow(() -> new RegleMetierException("Motif inconnu"));
        if (typeDocumentId != null) document(typeDocumentId);
        Motif m = new Motif(id, libelle == null ? null : libelle.trim(), ordre, vide(piecesRequises), typeDocumentId);
        catalogue.enregistrer(m);
        return m;
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Extrait de mention marginale : la mention demandée doit figurer en marge de l'acte. */
    private Optional<String> mentionAbsente(Demande d, ActeRetrouve acte) {
        String type = d.complements().typeMention();
        if (d.modele() != ModeleDocument.EXTRAIT_MENTION || type == null || acte.porteMention(type)) return Optional.empty();
        return Optional.of("L'acte " + acte.numero() + " a été retrouvé, mais aucune mention « " + type.toLowerCase()
                + " » n'est portée en marge : un agent doit vérifier avant toute délivrance.");
    }

    // ---------------------------------------------------------------- interne

    /** Pour un certificat : ce que la situation de la personne dans le registre empêche de délivrer. */
    private Optional<String> obstacle(Demande d) {
        return d.modele().estCertificat() ? situation(d).obstacle(d.modele()) : Optional.empty();
    }

    private SituationCivile situation(Demande d) {
        PersonneConcernee p = d.personne();
        return registre.situation(p.nom(), p.prenom(), p.dateNaissance());
    }

    private Demande trouver(UUID id) {
        return demandes.parId(id).orElseThrow(DemandeIntrouvableException::new);
    }

    /** Enregistre la demande puis publie chaque changement de statut (dans la même transaction, via l'outbox). */
    private Demande sauver(Demande d) {
        demandes.enregistrer(d);
        for (ChangementStatut c : d.extraireNouveauxChangements()) {
            if (c.ancien() != c.nouveau()) evenements.statutChange(d, c);
        }
        return d;
    }
}
