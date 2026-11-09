package sn.etatcivil.portail.paiement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByToken(String token);
    List<Paiement> findByDemandeIdOrderByDateCreationDesc(Long demandeId);
    Optional<Paiement> findFirstByDemandeIdAndStatut(Long demandeId, StatutPaiement statut);
}
