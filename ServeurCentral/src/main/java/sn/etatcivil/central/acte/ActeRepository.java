package sn.etatcivil.central.acte;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActeRepository extends JpaRepository<Acte, Long>, JpaSpecificationExecutor<Acte> {

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


    long countByStatut(StatutActe statut);

    long countByTypeActe(TypeActe typeActe);
}
