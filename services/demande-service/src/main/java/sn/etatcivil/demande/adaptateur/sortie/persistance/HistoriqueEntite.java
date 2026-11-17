package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "historique_statut")
class HistoriqueEntite {
    @Id UUID id;
    @Column(name = "demande_id") UUID demandeId;
    @Enumerated(EnumType.STRING) @Column(name = "ancien_statut") StatutDemande ancienStatut;
    @Enumerated(EnumType.STRING) @Column(name = "nouveau_statut") StatutDemande nouveauStatut;
    String auteur;
    String commentaire;
    @Column(name = "date_changement") Instant dateChangement;
}
