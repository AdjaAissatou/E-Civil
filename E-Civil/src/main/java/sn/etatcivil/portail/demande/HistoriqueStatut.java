package sn.etatcivil.portail.demande;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demande_id", nullable = false, updatable = false)
    private Long demandeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", updatable = false)
    private StatutDemande ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false, updatable = false)
    private StatutDemande nouveauStatut;

    @Column(nullable = false, updatable = false)
    private String auteur;

    @Column(updatable = false)
    private String commentaire;

    @Column(name = "date_changement", nullable = false, updatable = false)
    private LocalDateTime dateChangement = LocalDateTime.now();

    protected HistoriqueStatut() {}

    HistoriqueStatut(Long demandeId, StatutDemande ancien, StatutDemande nouveau, String auteur, String commentaire) {
        this.demandeId = demandeId;
        this.ancienStatut = ancien;
        this.nouveauStatut = nouveau;
        this.auteur = auteur;
        this.commentaire = commentaire;
    }

    public StatutDemande getAncienStatut() { return ancienStatut; }
    public StatutDemande getNouveauStatut() { return nouveauStatut; }
    public String getAuteur() { return auteur; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateChangement() { return dateChangement; }
}
