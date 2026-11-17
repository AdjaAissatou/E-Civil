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

interface HistoriquesJpa extends JpaRepository<HistoriqueEntite, UUID> {
    List<HistoriqueEntite> findByDemandeIdOrderByDateChangementAsc(UUID demandeId);
}
