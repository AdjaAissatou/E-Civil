package sn.etatcivil.paiement.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface DemandesPayablesJpa extends JpaRepository<DemandePayableEntite, UUID> {
}
