package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Demande;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ConsulterDemandes {
    /** Un citoyen ne voit que ses propres demandes ; un agent les voit toutes. */
    Demande consulter(UUID id, String utilisateurId, boolean estAgent);

    /** {@code statuts} vide = tous les statuts. */
    default PageResultat<Demande> rechercher(Set<StatutDemande> statuts, String reference, PageDemandee page) {
        return rechercher(FiltreDemandes.statuts(statuts, reference), page);
    }

    PageResultat<Demande> rechercher(FiltreDemandes filtre, PageDemandee page);

    /** Nombre de demandes par statut parmi celles que retiennent les filtres (statuts exceptés). */
    Map<StatutDemande, Long> nombreParStatut(FiltreDemandes filtre);

    Map<StatutDemande, Long> nombreParStatut();
}
