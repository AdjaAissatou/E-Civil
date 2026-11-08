package sn.etatcivil.document.adaptateur.sortie.persistance;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_delivre")
class DocumentEntite {
    @Id UUID id;
    @Column(name = "demande_id") UUID demandeId;
    String reference;
    @Column(name = "demandeur_id") String demandeurId;
    @Column(name = "libelle_document") String libelleDocument;
    @Column(name = "copie_integrale") boolean copieIntegrale;
    String format;
    /** Contenu de l'acte figé, en JSON. */
    @Column(columnDefinition = "TEXT") String contenu;
    String signataire;
    @Column(name = "date_signature") Instant dateSignature;
    @Column(name = "code_verification") String codeVerification;
    String empreinte;
}
