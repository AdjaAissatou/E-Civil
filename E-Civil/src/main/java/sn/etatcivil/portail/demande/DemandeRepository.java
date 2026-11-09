package sn.etatcivil.portail.demande;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DemandeRepository extends JpaRepository<Demande, Long>, JpaSpecificationExecutor<Demande> {

    List<Demande> findByDemandeurIdOrderByDateCreationDesc(Long demandeurId);

    Page<Demande> findByStatutInOrderByDateCreationAsc(Collection<StatutDemande> statuts, Pageable pageable);


    List<Demande> findTop50ByStatutOrderByDateModificationAsc(StatutDemande statut);

    Optional<Demande> findByCodeVerification(String code);

    @Query("SELECT d FROM Demande d JOIN FETCH d.demandeur WHERE d.id = :id")
    Optional<Demande> avecDemandeur(@Param("id") Long id);

    long countByStatut(StatutDemande statut);
}
