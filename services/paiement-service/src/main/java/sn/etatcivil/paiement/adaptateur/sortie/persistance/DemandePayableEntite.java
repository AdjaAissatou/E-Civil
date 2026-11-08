package sn.etatcivil.paiement.adaptateur.sortie.persistance;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "demande_payable")
class DemandePayableEntite {
    @Id @Column(name = "demande_id") UUID demandeId;
    String reference;
    @Column(name = "demandeur_id") String demandeurId;
    @Column(name = "libelle_document") String libelleDocument;
    int montant;
    boolean payable;
}
