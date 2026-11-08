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

    public ServiceDemandes(DemandeRepository demandes, CatalogueRepository catalogue, GenerateurReference references,
                           RegistreCivil registre, ServiceDocuments documents, EvenementsDemande evenements,
                           Horloge horloge) {
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
        Demande d = Demande.deposer(reference, demandeur, type, motif, c.precisionMotif(), c.lien(), c.format(),
                c.adresseLivraison(), c.villeLivraison(), c.personne(), maintenant);
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
    public PageResultat<Demande> rechercher(Set<StatutDemande> statuts, String reference, PageDemandee page) {
        return demandes.rechercher(statuts, reference, page);
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
        RegistreCivil.ResultatRecherche r = registre.rechercher(d.typeActe(), d.personne());
        Instant maintenant = horloge.maintenant();
        switch (r.resultat()) {
            case TROUVE -> d.rattacherActe(r.actes().get(0), auteur, "Acte retrouvé dans le registre national", maintenant);
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
        ActeRetrouve acte = registre.parId(d.acteId())
                .orElseThrow(() -> new RegleMetierException("L'acte rattaché n'existe plus dans le registre"));
        if (!acte.valide()) throw new RegleMetierException("L'acte " + acte.numero() + " n'est plus validé dans le registre");
        String code = documents.delivrer(new ServiceDocuments.DocumentADelivrer(d.id(), d.reference(), d.demandeur().id(),
                d.libelleDocument(), d.copieIntegrale(), d.acteId(), d.format().name(), officier.nomComplet()));
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
    public List<Motif> motifs() {
        return catalogue.motifs();
    }

    @Override
    public TypeDocument modifier(Long id, int tarif, int delaiJours, boolean actif) {
        TypeDocument t = document(id);
        t.modifier(tarif, delaiJours, actif);
        catalogue.enregistrer(t);
        return t;
    }

    // ---------------------------------------------------------------- interne

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
