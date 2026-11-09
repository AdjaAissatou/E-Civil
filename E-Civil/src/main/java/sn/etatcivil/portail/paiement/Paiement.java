package sn.etatcivil.portail.paiement;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "demande_id", nullable = false, updatable = false)
    private Long demandeId;

    /** Identifiant de la facture chez PayDunya. */
    @Column(nullable = false, unique = true, updatable = false)
    private String token;

    @Column(nullable = false, updatable = false)
    private int montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statut = StatutPaiement.INITIE;

    @Column(name = "url_paiement", nullable = false, updatable = false)
    private String urlPaiement;

    @Column(name = "url_recu")
    private String urlRecu;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_confirmation")
    private LocalDateTime dateConfirmation;

    @Version
    private Integer version;

    protected Paiement() {}

    public Paiement(Long demandeId, String token, int montant, String urlPaiement) {
        this.demandeId = demandeId;
        this.token = token;
        this.montant = montant;
        this.urlPaiement = urlPaiement;
    }

    void confirmer(String urlRecu) {
        this.statut = StatutPaiement.PAYE;
        this.urlRecu = urlRecu;
        this.dateConfirmation = LocalDateTime.now();
    }

    void annuler() {
        this.statut = StatutPaiement.ANNULE;
    }

    public Long getId() { return id; }
    public Long getDemandeId() { return demandeId; }
    public String getToken() { return token; }
    public int getMontant() { return montant; }
    public StatutPaiement getStatut() { return statut; }
    public String getUrlPaiement() { return urlPaiement; }
    public String getUrlRecu() { return urlRecu; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateConfirmation() { return dateConfirmation; }
}
