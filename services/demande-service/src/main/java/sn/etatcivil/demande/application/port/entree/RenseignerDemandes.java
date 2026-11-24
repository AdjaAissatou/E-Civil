package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Travail du chargé de renseignement : chercher dans les registres papier l'acte que le registre national n'a pas. */
public interface RenseignerDemandes {

    record Dossier(Renseignement renseignement, EtatRenseignement etat) {}

    /** Résumé d'un dossier pour la file de travail. */
    record Resume(UUID demandeId, boolean urgent, String agentNom, int consultations, Instant derniereConsultation,
                  EtatRenseignement etat) {}

    record Statistiques(long enCours, long urgents, long trouves, long nonTrouves) {}

    Optional<Dossier> dossier(UUID demandeId);

    Dossier prendreEnCharge(UUID demandeId, Acteur agent);

    Dossier definirPriorite(UUID demandeId, boolean urgent, Acteur agent);

    Dossier consigner(UUID demandeId, Long registreId, String registre, ResultatConsultation resultat, String folio,
                      String note, Acteur agent);

    /** Conclut que l'acte n'existe pas : la demande est rejetée et le demandeur prévenu. */
    Dossier conclureNonTrouve(UUID demandeId, String motif, Acteur agent);

    List<Resume> dossiersEnCours();

    Statistiques statistiques();
}
