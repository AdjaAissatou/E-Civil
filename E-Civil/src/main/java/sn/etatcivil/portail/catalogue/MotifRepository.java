package sn.etatcivil.portail.catalogue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotifRepository extends JpaRepository<Motif, Long> {
    List<Motif> findAllByOrderByOrdreAsc();
}
