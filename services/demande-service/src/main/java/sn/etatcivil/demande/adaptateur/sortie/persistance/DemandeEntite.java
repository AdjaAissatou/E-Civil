package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.demande.domaine.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "demande")
class DemandeEntite {

    @Id
    UUID id;

    @Column(name = "reference")
    String reference;
    @Column(name = "demandeur_id")
    String demandeurId;
    @Column(name = "demandeur_email")
    String demandeurEmail;
    @Column(name = "demandeur_prenom")
    String demandeurPrenom;
    @Column(name = "demandeur_nom")
    String demandeurNom;
    @Column(name = "type_document_id")
    Long typeDocumentId;
    @Column(name = "libelle_document")
    String libelleDocument;
    @Enumerated(EnumType.STRING)
    @Column(name = "type_acte")
    TypeActe typeActe;
    @Column(name = "copie_integrale")
    boolean copieIntegrale;
    @Column(name = "montant")
    int montant;
    @Column(name = "motif_id")
    Long motifId;
    @Column(name = "libelle_motif")
    String libelleMotif;
    @Enumerated(EnumType.STRING)
    @Column(name = "lien")
    LienDemandeur lien;
    @Enumerated(EnumType.STRING)
    @Column(name = "format")
    FormatDocument format;
    @Column(name = "adresse_livraison")
    String adresseLivraison;
    @Column(name = "ville_livraison")
    String villeLivraison;
    @Column(name = "nom")
    String nom;
    @Column(name = "prenom")
    String prenom;
    @Column(name = "sexe")
    String sexe;
    @Column(name = "date_naissance")
    LocalDate dateNaissance;
    @Column(name = "lieu_naissance")
    String lieuNaissance;
    @Column(name = "nom_pere")
    String nomPere;
    @Column(name = "prenom_pere")
    String prenomPere;
    @Column(name = "nom_mere")
    String nomMere;
    @Column(name = "prenom_mere")
    String prenomMere;
    @Column(name = "nom_conjoint")
    String nomConjoint;
    @Column(name = "prenom_conjoint")
    String prenomConjoint;
    @Column(name = "date_evenement")
    LocalDate dateEvenement;
    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    StatutDemande statut;
    @Column(name = "acte_id")
    UUID acteId;
    @Column(name = "numero_acte")
    String numeroActe;
    @Column(name = "note_recherche")
    String noteRecherche;
    @Column(name = "signataire")
    String signataire;
    @Column(name = "date_signature")
    Instant dateSignature;
    @Column(name = "code_verification")
    String codeVerification;
    @Column(name = "motif_rejet")
    String motifRejet;
    @Column(name = "date_creation")
    Instant dateCreation;
    @Column(name = "date_modification")
    Instant dateModification;

    @Version
    Integer version;
}
