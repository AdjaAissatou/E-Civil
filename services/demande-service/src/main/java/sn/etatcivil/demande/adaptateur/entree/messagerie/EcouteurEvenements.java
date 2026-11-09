package sn.etatcivil.demande.adaptateur.entree.messagerie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.entree.RechercherActes;
import sn.etatcivil.demande.application.port.entree.TraiterPaiements;
import sn.etatcivil.demande.application.port.sortie.RegistreIndisponibleException;
import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.TypeActe;
import sn.etatcivil.evenements.ActeValide;
import sn.etatcivil.evenements.PaiementConfirme;
import sn.etatcivil.evenements.Routages;
import sn.etatcivil.socle.messagerie.EnveloppeEvenement;
import sn.etatcivil.socle.messagerie.TraitementIdempotent;

import java.util.UUID;

/** Adaptateur entrant : réagit aux événements des autres services. */
@Component
class EcouteurEvenements {

    static final String FILE = "demande-service";
    private static final Logger log = LoggerFactory.getLogger(EcouteurEvenements.class);

    private final TraitementIdempotent idempotence;
    private final TraiterPaiements paiements;
    private final RechercherActes recherche;

    EcouteurEvenements(TraitementIdempotent idempotence, TraiterPaiements paiements, RechercherActes recherche) {
        this.idempotence = idempotence;
        this.paiements = paiements;
        this.recherche = recherche;
    }

    @RabbitListener(queues = FILE)
    void recevoir(EnveloppeEvenement e) {
        switch (e.type()) {
            case Routages.PAIEMENT_CONFIRME -> paiementConfirme(e);
            case Routages.ACTE_VALIDE -> acteValide(e);
            default -> log.debug("Événement {} ignoré", e.type());
        }
    }

    /** Paiement confirmé : la demande passe « payée », puis l'acte est aussitôt recherché dans le registre. */
    private void paiementConfirme(EnveloppeEvenement e) {
        UUID[] demande = new UUID[1];
        boolean[] nouveau = new boolean[1];
        idempotence.traiter(e, PaiementConfirme.class, p -> {
            demande[0] = UUID.fromString(p.demandeId());
            nouveau[0] = paiements.enregistrerPaiement(demande[0], p.montant(),
                    "Paiement PayDunya confirmé (facture " + p.tokenFacture() + ")");
        });
        if (nouveau[0]) rechercherSansBloquer(demande[0]);
    }

    /** Un acte vient d'être validé : on retente les demandes de ce type restées sans acte. */
    private void acteValide(EnveloppeEvenement e) {
        TypeActe[] type = new TypeActe[1];
        if (idempotence.traiter(e, ActeValide.class, a -> type[0] = TypeActe.valueOf(a.typeActe()))) {
            recherche.introuvables(type[0]).forEach(this::rechercherSansBloquer);
        }
    }

    private void rechercherSansBloquer(UUID id) {
        try {
            recherche.rechercher(id, Acteur.SYSTEME);
        } catch (RegistreIndisponibleException ex) {
            log.warn("Registre injoignable : la demande {} sera recherchée plus tard", id);
        } catch (RuntimeException ex) {
            log.warn("Recherche automatique de la demande {} impossible : {}", id, ex.getMessage());
        }
    }
}
