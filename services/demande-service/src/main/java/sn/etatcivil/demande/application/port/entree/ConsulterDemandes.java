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
    PageResultat<Demande> rechercher(Set<StatutDemande> statuts, String reference, PageDemandee page);

    Map<StatutDemande, Long> nombreParStatut();
}
