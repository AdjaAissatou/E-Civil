package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.application.port.entree.RenseignerDemandes;
import sn.etatcivil.demande.domaine.Renseignement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RenseignementsRepository {

    Optional<Renseignement> parDemande(UUID demandeId);

    /** Enregistre le dossier et ses nouvelles consultations (les consultations ne sont jamais modifiées). */
    void enregistrer(Renseignement renseignement);

    /** Dossiers des demandes qui attendent encore leur acte. */
    List<RenseignerDemandes.Resume> enCours();

    RenseignerDemandes.Statistiques statistiques();
}
