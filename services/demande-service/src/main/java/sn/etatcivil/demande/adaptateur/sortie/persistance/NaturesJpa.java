package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface NaturesJpa extends JpaRepository<NatureEntite, Long> {
    List<NatureEntite> findAllByOrderByOrdreAscLibelleAsc();

    Optional<NatureEntite> findByCode(String code);
}
