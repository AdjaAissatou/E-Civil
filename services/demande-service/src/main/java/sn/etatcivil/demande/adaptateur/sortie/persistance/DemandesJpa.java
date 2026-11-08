package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.etatcivil.demande.domaine.StatutDemande;
import sn.etatcivil.demande.domaine.TypeActe;

import java.util.List;
import java.util.UUID;

interface DemandesJpa extends JpaRepository<DemandeEntite, UUID>, JpaSpecificationExecutor<DemandeEntite> {
    List<DemandeEntite> findByDemandeurIdOrderByDateCreationDesc(String demandeurId);

    @Query("SELECT d.id FROM DemandeEntite d WHERE d.statut = :statut ORDER BY d.dateModification")
    List<UUID> identifiants(@Param("statut") StatutDemande statut, Pageable limite);

    @Query("SELECT d.id FROM DemandeEntite d WHERE d.statut = :statut AND d.typeActe = :type ORDER BY d.dateModification")
    List<UUID> identifiants(@Param("statut") StatutDemande statut, @Param("type") TypeActe type, Pageable limite);

    long countByStatut(StatutDemande statut);
}
