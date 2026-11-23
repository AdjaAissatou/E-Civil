package sn.etatcivil.paiement.adaptateur.sortie.persistance;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import sn.etatcivil.paiement.application.port.sortie.DemandePayableRepository;
import sn.etatcivil.paiement.application.port.sortie.PaiementRepository;
import sn.etatcivil.paiement.domaine.DemandePayable;
import sn.etatcivil.paiement.domaine.Paiement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class AdaptateurPersistance implements PaiementRepository, DemandePayableRepository {

    private final PaiementsJpa paiements;
    private final DemandesPayablesJpa demandes;

    AdaptateurPersistance(PaiementsJpa paiements, DemandesPayablesJpa demandes) {
        this.paiements = paiements;
        this.demandes = demandes;
    }

    @Override
    public void enregistrer(Paiement p) {
        PaiementEntite e = paiements.findById(p.id()).orElseGet(PaiementEntite::new);
        if (e.version != null && p.version() != null && !e.version.equals(p.version())) {
            throw new ObjectOptimisticLockingFailureException(PaiementEntite.class, p.id());
        }
        e.id = p.id();
        e.demandeId = p.demandeId();
        e.token = p.token();
        e.montant = p.montant();
        e.statut = p.statut();
        e.urlPaiement = p.urlPaiement();
        e.urlRecu = p.urlRecu();
        e.dateCreation = p.dateCreation();
        e.dateConfirmation = p.dateConfirmation();
        paiements.saveAndFlush(e);
    }

    @Override
    public Optional<Paiement> parToken(String token) {
        return paiements.findByToken(token).map(AdaptateurPersistance::versDomaine);
    }

    @Override
    public List<Paiement> deLaDemande(UUID demandeId) {
        return paiements.findByDemandeIdOrderByDateCreationDesc(demandeId).stream().map(AdaptateurPersistance::versDomaine).toList();
    }

    @Override
    public void enregistrer(DemandePayable d) {
        DemandePayableEntite e = demandes.findById(d.demandeId()).orElseGet(DemandePayableEntite::new);
        e.demandeId = d.demandeId();
        e.reference = d.reference();
        e.demandeurId = d.demandeurId();
        e.libelleDocument = d.libelleDocument();
        e.montant = d.montant();
        e.payable = d.payable();
        demandes.save(e);
    }

    @Override
    public Optional<DemandePayable> parId(UUID demandeId) {
        return demandes.findById(demandeId).map(e -> new DemandePayable(e.demandeId, e.reference, e.demandeurId,
                e.libelleDocument, e.montant, e.payable));
    }

    private static Paiement versDomaine(PaiementEntite e) {
        return new Paiement(e.id, e.demandeId, e.token, e.montant, e.urlPaiement, e.dateCreation, e.statut, e.urlRecu,
                e.dateConfirmation, e.version);
    }
}
