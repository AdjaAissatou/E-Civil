package sn.etatcivil.demande.application.service;

import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.entree.RenseignerDemandes;
import sn.etatcivil.demande.application.port.sortie.DemandeRepository;
import sn.etatcivil.demande.application.port.sortie.EvenementsDemande;
import sn.etatcivil.demande.application.port.sortie.Horloge;
import sn.etatcivil.demande.application.port.sortie.RenseignementsRepository;
import sn.etatcivil.demande.domaine.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CasDUsage
public class ServiceRenseignements implements RenseignerDemandes {

    private final RenseignementsRepository renseignements;
    private final DemandeRepository demandes;
    private final EvenementsDemande evenements;
    private final Horloge horloge;

    public ServiceRenseignements(RenseignementsRepository renseignements, DemandeRepository demandes,
                                 EvenementsDemande evenements, Horloge horloge) {
        this.renseignements = renseignements;
        this.demandes = demandes;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    @Override
    public Optional<Dossier> dossier(UUID demandeId) {
        Demande d = demande(demandeId);
        return renseignements.parDemande(demandeId).map(r -> new Dossier(r, r.etat(d)));
    }

    @Override
    public Dossier prendreEnCharge(UUID demandeId, Acteur agent) {
        return modifier(demandeId, (r, d) -> r.prendreEnCharge(agent, d, horloge.maintenant()));
    }

    @Override
    public Dossier definirPriorite(UUID demandeId, boolean urgent, Acteur agent) {
        return modifier(demandeId, (r, d) -> r.definirPriorite(urgent, agent, d));
    }

    @Override
    public Dossier consigner(UUID demandeId, Long registreId, String registre, ResultatConsultation resultat, String folio,
                             String note, Acteur agent) {
        return modifier(demandeId, (r, d) -> r.consigner(registreId, registre, resultat, folio, note, agent, d,
                horloge.maintenant()));
    }

    @Override
    public Dossier conclureNonTrouve(UUID demandeId, String motif, Acteur agent) {
        return modifier(demandeId, (r, d) -> {
            String motifRejet = r.conclureNonTrouve(motif, agent, d, horloge.maintenant());
            d.rejeter(motifRejet, agent, horloge.maintenant());
            demandes.enregistrer(d);
            for (ChangementStatut c : d.extraireNouveauxChangements()) {
                if (c.ancien() != c.nouveau()) evenements.statutChange(d, c);
            }
        });
    }

    @Override
    public List<Resume> dossiersEnCours() {
        return renseignements.enCours();
    }

    @Override
    public Statistiques statistiques() {
        return renseignements.statistiques();
    }

    private interface Operation {
        void appliquer(Renseignement r, Demande d);
    }

    private Dossier modifier(UUID demandeId, Operation op) {
        Demande d = demande(demandeId);
        Renseignement r = renseignements.parDemande(demandeId).orElseGet(() -> Renseignement.ouvrir(d, horloge.maintenant()));
        op.appliquer(r, d);
        renseignements.enregistrer(r);
        return new Dossier(r, r.etat(d));
    }

    private Demande demande(UUID id) {
        return demandes.parId(id).orElseThrow(DemandeIntrouvableException::new);
    }
}
