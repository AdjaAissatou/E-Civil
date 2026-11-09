package sn.etatcivil.document.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface DocumentsJpa extends JpaRepository<DocumentEntite, UUID> {
    Optional<DocumentEntite> findByDemandeId(UUID demandeId);

    Optional<DocumentEntite> findByCodeVerification(String code);
}
