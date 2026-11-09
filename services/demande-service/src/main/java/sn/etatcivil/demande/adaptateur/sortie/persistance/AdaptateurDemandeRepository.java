package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.entree.PageDemandee;
import sn.etatcivil.demande.application.port.entree.PageResultat;
import sn.etatcivil.demande.application.port.sortie.DemandeRepository;
import sn.etatcivil.demande.domaine.*;

import java.util.*;

@Component
class AdaptateurDemandeRepository implements DemandeRepository {

    private final DemandesJpa demandes;
    private final HistoriquesJpa historiques;

    AdaptateurDemandeRepository(DemandesJpa demandes, HistoriquesJpa historiques) {
        this.demandes = demandes;
        this.historiques = historiques;
    }

    @Override
    public void enregistrer(Demande d) {
        DemandeEntite e = demandes.findById(d.id()).orElseGet(DemandeEntite::new);
        if (e.version != null && d.version() != null && !e.version.equals(d.version())) {
            throw new ObjectOptimisticLockingFailureException(DemandeEntite.class, d.id());
        }
        copier(d, e);
        demandes.saveAndFlush(e);
        for (ChangementStatut c : d.nouveauxChangements()) {
            HistoriqueEntite h = new HistoriqueEntite();
            h.id = c.id();
            h.demandeId = d.id();
            h.ancienStatut = c.ancien();
            h.nouveauStatut = c.nouveau();
            h.auteur = c.auteur();
            h.commentaire = c.commentaire();
            h.dateChangement = c.date();
            historiques.save(h);
        }
    }

    @Override
    public Optional<Demande> parId(UUID id) {
        return demandes.findById(id).map(e -> versDomaine(e, historiques.findByDemandeIdOrderByDateChangementAsc(id)));
    }

    @Override
    public List<Demande> parDemandeur(String demandeurId) {
        return demandes.findByDemandeurIdOrderByDateCreationDesc(demandeurId).stream()
                .map(e -> versDomaine(e, List.of())).toList();
    }

    @Override
    public PageResultat<Demande> rechercher(Set<StatutDemande> statuts, String reference, PageDemandee page) {
        Specification<DemandeEntite> spec = (r, q, cb) -> cb.conjunction();
        if (statuts != null && !statuts.isEmpty()) spec = spec.and((r, q, cb) -> r.get("statut").in(statuts));
        if (reference != null && !reference.isBlank()) {
            String motif = "%" + reference.trim().toUpperCase() + "%";
            spec = spec.and((r, q, cb) -> cb.like(r.get("reference"), motif));
        }
        Page<DemandeEntite> p = demandes.findAll(spec, PageRequest.of(page.numero(), page.taille(),
                Sort.by(Sort.Direction.ASC, "dateCreation")));
        return new PageResultat<>(p.getContent().stream().map(e -> versDomaine(e, List.of())).toList(),
                page.numero(), page.taille(), p.getTotalElements());
    }

    @Override
    public List<UUID> identifiants(StatutDemande statut, TypeActe type, int limite) {
        return type == null
                ? demandes.identifiants(statut, PageRequest.of(0, limite))
                : demandes.identifiants(statut, type, PageRequest.of(0, limite));
    }

    @Override
    public long compter(StatutDemande statut) {
        return demandes.countByStatut(statut);
    }

    private static void copier(Demande d, DemandeEntite e) {
        PersonneConcernee p = d.personne();
        e.id = d.id();
        e.reference = d.reference();
        e.demandeurId = d.demandeur().id();
        e.demandeurEmail = d.demandeur().email();
        e.demandeurPrenom = d.demandeur().prenom();
        e.demandeurNom = d.demandeur().nom();
        e.typeDocumentId = d.typeDocumentId();
        e.libelleDocument = d.libelleDocument();
        e.typeActe = d.typeActe();
        e.copieIntegrale = d.copieIntegrale();
        e.montant = d.montant();
        e.motifId = d.motifId();
        e.libelleMotif = d.libelleMotif();
        e.lien = d.lien();
        e.format = d.format();
        e.adresseLivraison = d.adresseLivraison();
        e.villeLivraison = d.villeLivraison();
        e.nom = p.nom();
        e.prenom = p.prenom();
        e.sexe = p.sexe();
        e.dateNaissance = p.dateNaissance();
        e.lieuNaissance = p.lieuNaissance();
        e.nomPere = p.nomPere();
        e.prenomPere = p.prenomPere();
        e.nomMere = p.nomMere();
        e.prenomMere = p.prenomMere();
        e.nomConjoint = p.nomConjoint();
        e.prenomConjoint = p.prenomConjoint();
        e.dateEvenement = p.dateEvenement();
        e.statut = d.statut();
        e.acteId = d.acteId();
        e.numeroActe = d.numeroActe();
        e.noteRecherche = d.noteRecherche();
        e.signataire = d.signataire();
        e.dateSignature = d.dateSignature();
        e.codeVerification = d.codeVerification();
        e.motifRejet = d.motifRejet();
        e.dateCreation = d.dateCreation();
        e.dateModification = d.dateModification();
    }

    private static Demande versDomaine(DemandeEntite e, List<HistoriqueEntite> h) {
        PersonneConcernee p = new PersonneConcernee(e.nom, e.prenom, e.sexe, e.dateNaissance, e.lieuNaissance,
                e.nomPere, e.prenomPere, e.nomMere, e.prenomMere, e.nomConjoint, e.prenomConjoint, e.dateEvenement);
        List<ChangementStatut> historique = h.stream().map(x -> new ChangementStatut(x.id, x.ancienStatut,
                x.nouveauStatut, x.auteur, x.commentaire, x.dateChangement)).toList();
        return new Demande(e.id, e.reference, new Demandeur(e.demandeurId, e.demandeurEmail, e.demandeurPrenom, e.demandeurNom),
                e.typeDocumentId, e.libelleDocument, e.typeActe, e.copieIntegrale, e.montant, e.motifId, e.libelleMotif,
                e.lien, e.format, e.adresseLivraison, e.villeLivraison, p, e.dateCreation, e.statut, e.acteId,
                e.numeroActe, e.noteRecherche, e.signataire, e.dateSignature, e.codeVerification, e.motifRejet,
                e.dateModification, e.version, historique);
    }
}
