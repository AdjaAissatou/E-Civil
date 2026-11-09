package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.application.port.entree.PageDemandee;
import sn.etatcivil.demande.application.port.entree.PageResultat;
import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.demande.domaine.StatutDemande;
import sn.etatcivil.demande.domaine.TypeActe;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DemandeRepository {
    /** Enregistre la demande et les nouvelles lignes de son historique. */
    void enregistrer(Demande demande);

    Optional<Demande> parId(UUID id);

    List<Demande> parDemandeur(String demandeurId);

    PageResultat<Demande> rechercher(Set<StatutDemande> statuts, String reference, PageDemandee page);

    List<UUID> identifiants(StatutDemande statut, TypeActe type, int limite);

    long compter(StatutDemande statut);
}
