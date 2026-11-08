package sn.etatcivil.paiement.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.paiement.domaine.StatutPaiement;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paiement")
class PaiementEntite {
    @Id UUID id;
    @Column(name = "demande_id") UUID demandeId;
    String token;
    int montant;
    @Enumerated(EnumType.STRING) StatutPaiement statut;
    @Column(name = "url_paiement") String urlPaiement;
    @Column(name = "url_recu") String urlRecu;
    @Column(name = "date_creation") Instant dateCreation;
    @Column(name = "date_confirmation") Instant dateConfirmation;
    @Version Integer version;
}
