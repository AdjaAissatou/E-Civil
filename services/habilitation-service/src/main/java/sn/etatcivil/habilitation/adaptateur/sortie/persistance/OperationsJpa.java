package sn.etatcivil.habilitation.adaptateur.sortie.persistance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface OperationsJpa extends JpaRepository<OperationEntite, Long> {

    Page<OperationEntite> findAllByOrderByDateDescIdDesc(Pageable page);

    @Query("""
            SELECT o FROM OperationEntite o
            WHERE LOWER(o.cible) LIKE :motif OR LOWER(o.auteur) LIKE :motif OR LOWER(o.detail) LIKE :motif
            ORDER BY o.date DESC, o.id DESC
            """)
    Page<OperationEntite> rechercher(@Param("motif") String motif, Pageable page);

    List<OperationEntite> findTop20ByCibleOrderByDateDescIdDesc(String cible);
}
