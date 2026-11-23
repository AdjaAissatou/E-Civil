package sn.etatcivil.paiement.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface PaiementsJpa extends JpaRepository<PaiementEntite, UUID> {
    Optional<PaiementEntite> findByToken(String token);

    List<PaiementEntite> findByDemandeIdOrderByDateCreationDesc(UUID demandeId);
}
