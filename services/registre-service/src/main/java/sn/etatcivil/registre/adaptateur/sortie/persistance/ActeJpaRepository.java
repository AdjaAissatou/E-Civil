package sn.etatcivil.registre.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ActeJpaRepository extends JpaRepository<ActeEntite, UUID>, JpaSpecificationExecutor<ActeEntite> {

    Optional<ActeEntite> findByNumeroActe(String numeroActe);

    @Query("""
            SELECT a FROM ActeEntite a WHERE a.typeActe = :type
              AND ((a.nomNormalise = :nom AND a.prenomNormalise = :prenom)
                OR (a.nomConjointNormalise = :nom AND a.prenomConjointNormalise = :prenom))
            """)
    List<ActeEntite> candidats(@Param("type") TypeActe type, @Param("nom") String nom, @Param("prenom") String prenom);

    long countByStatut(StatutActe statut);

    long countByTypeActe(TypeActe type);
}
