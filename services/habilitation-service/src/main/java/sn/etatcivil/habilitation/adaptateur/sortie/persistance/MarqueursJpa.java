package sn.etatcivil.habilitation.adaptateur.sortie.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

interface MarqueursJpa extends JpaRepository<MarqueurEntite, String> {}
