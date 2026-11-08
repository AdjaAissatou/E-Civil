package sn.etatcivil.portail.demande;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {
    List<HistoriqueStatut> findByDemandeIdOrderByDateChangementAsc(Long demandeId);
}
