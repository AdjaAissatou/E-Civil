package sn.etatcivil.paiement.application.service;

import sn.etatcivil.paiement.application.CasDUsage;
import sn.etatcivil.paiement.application.port.entree.PayerDemande;
import sn.etatcivil.paiement.application.port.entree.SuivreDemandes;
import sn.etatcivil.paiement.application.port.sortie.*;
import sn.etatcivil.paiement.domaine.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Une demande n'est déclarée payée que lorsque l'opérateur confirme le paiement et le montant exact.
 * Ni le retour du navigateur ni une notification ne suffisent seuls : tous deux déclenchent une
 * interrogation de l'opérateur.
 */
@CasDUsage
public class ServicePaiements implements PayerDemande, SuivreDemandes {

    private final PaiementRepository paiements;
    private final DemandePayableRepository demandes;
    private final PasserellePaiement passerelle;
    private final AdressesRetour adresses;
    private final EvenementsPaiement evenements;
    private final Horloge horloge;

    public ServicePaiements(PaiementRepository paiements, DemandePayableRepository demandes, PasserellePaiement passerelle,
                            AdressesRetour adresses, EvenementsPaiement evenements, Horloge horloge) {
        this.paiements = paiements;
        this.demandes = demandes;
        this.passerelle = passerelle;
        this.adresses = adresses;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    @Override
    public String initier(UUID demandeId, String demandeurId) {
        if (!passerelle.configuree()) {
            throw new RegleMetierException("Le paiement en ligne n'est pas encore configuré. Contactez l'administration.");
        }
        DemandePayable d = demandes.parId(demandeId).orElseThrow(() -> new RegleMetierException(
                "Cette demande n'est pas en attente de paiement"));
        d.verifierPayablePar(demandeurId);
        Optional<Paiement> enCours = paiements.deLaDemande(demandeId).stream()
                .filter(p -> p.reutilisable(horloge.maintenant())).findFirst();
        if (enCours.isPresent()) return enCours.get().urlPaiement();

        var facture = passerelle.creerFacture(d.montant(), d.libelleDocument() + " - demande " + d.reference(),
                d.reference(), adresses.pour(demandeId));
        paiements.enregistrer(Paiement.initier(demandeId, facture.token(), d.montant(), facture.urlPaiement(), horloge.maintenant()));
        return facture.urlPaiement();
    }

    @Override
    public Paiement confirmer(String token) {
        Paiement p = paiements.parToken(token).orElseThrow(() -> new RegleMetierException("Paiement inconnu"));
        if (p.statut() != StatutPaiement.INITIE) return p;
        boolean confirme = p.appliquer(passerelle.consulter(token), horloge.maintenant());
        paiements.enregistrer(p);
        if (confirme) {
            DemandePayable d = demandes.parId(p.demandeId()).orElseThrow();
            demandes.enregistrer(new DemandePayable(d.demandeId(), d.reference(), d.demandeurId(), d.libelleDocument(),
                    d.montant(), false));
            evenements.paiementConfirme(p, d);
        }
        return p;
    }

    @Override
    public void recevoirNotification(String signature, String token) {
        if (signature == null || token == null || !passerelle.notificationAuthentique(signature)) {
            throw new RegleMetierException("Notification non authentifiée");
        }
        confirmer(token);
    }

    @Override
    public List<Paiement> paiements(UUID demandeId, String utilisateurId, boolean estAgent) {
        if (!estAgent) {
            boolean proprietaire = demandes.parId(demandeId).map(d -> d.demandeurId().equals(utilisateurId)).orElse(false);
            if (!proprietaire) return List.of();
        }
        return paiements.deLaDemande(demandeId);
    }

    @Override
    public boolean disponible() {
        return passerelle.configuree();
    }

    @Override
    public void statutChange(UUID demandeId, String reference, String demandeurId, String libelleDocument, int montant,
                             String nouveauStatut) {
        demandes.enregistrer(new DemandePayable(demandeId, reference, demandeurId, libelleDocument, montant,
                "A_PAYER".equals(nouveauStatut)));
    }
}
