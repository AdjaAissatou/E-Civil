package sn.etatcivil.central.acte;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActeRepository extends JpaRepository<Acte, Long> {

    Optional<Acte> findByNumeroActe(String numeroActe);

    /** Actes validés où la personne recherchée est le titulaire, ou le conjoint pour un mariage/divorce. */
    @Query("""
            SELECT a FROM Acte a
            WHERE a.typeActe = :type AND a.statut = sn.etatcivil.central.acte.StatutActe.VALIDE
              AND ((a.nomNormalise = :nom AND a.prenomNormalise = :prenom)
                OR (a.nomConjointNormalise = :nom AND a.prenomConjointNormalise = :prenom))
            """)
    List<Acte> candidats(@Param("type") TypeActe type, @Param("nom") String nomNormalise,
                         @Param("prenom") String prenomNormalise);

    @Query("""
            SELECT a FROM Acte a
            WHERE (:type IS NULL OR a.typeActe = :type)
              AND (:statut IS NULL OR a.statut = :statut)
              AND (:nom IS NULL OR a.nomNormalise LIKE CONCAT('%', :nom, '%'))
            """)
    Page<Acte> lister(@Param("type") TypeActe type, @Param("statut") StatutActe statut,
                      @Param("nom") String nomNormalise, Pageable pageable);

    long countByStatut(StatutActe statut);

    long countByTypeActe(TypeActe typeActe);
}
