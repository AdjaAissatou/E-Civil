package sn.etatcivil.portail.demande;

import jakarta.persistence.*;
import sn.etatcivil.portail.catalogue.Motif;
import sn.etatcivil.portail.catalogue.TypeDocument;
import sn.etatcivil.portail.utilisateur.Utilisateur;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "demandeur_id", updatable = false)
    private Utilisateur demandeur;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_document_id", updatable = false)
    private TypeDocument typeDocument;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "motif_id")
    private Motif motif;

    @Column(name = "motif_precision")
    private String motifPrecision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut = StatutDemande.SOUMISE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormatDocument format;

    @Column(name = "adresse_livraison")
    private String adresseLivraison;

    @Column(name = "ville_livraison")
    private String villeLivraison;

    @Enumerated(EnumType.STRING)
    @Column(name = "lien_demandeur", nullable = false)
    private LienDemandeur lienDemandeur;

    /** Tarif figé au moment de la demande, en francs CFA. */
    @Column(nullable = false, updatable = false)
    private int montant;

    @Column(name = "nom", nullable = false)
    private String nom;
    @Column(name = "prenom", nullable = false)
    private String prenom;
    @Column(name = "sexe")
    private String sexe;
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    @Column(name = "lieu_naissance")
    private String lieuNaissance;
    @Column(name = "nom_pere")
    private String nomPere;
    @Column(name = "prenom_pere")
    private String prenomPere;
    @Column(name = "nom_mere")
    private String nomMere;
    @Column(name = "prenom_mere")
    private String prenomMere;
    @Column(name = "nom_conjoint")
    private String nomConjoint;
    @Column(name = "prenom_conjoint")
    private String prenomConjoint;
    @Column(name = "date_evenement")
    private LocalDate dateEvenement;

    @Column(name = "acte_central_id")
    private Long acteCentralId;

    @Column(name = "numero_acte")
    private String numeroActe;

    @Column(name = "note_recherche")
    private String noteRecherche;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signataire_id")
    private Utilisateur signataire;

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    @Column(name = "code_verification", unique = true)
    private String codeVerification;

    /** Copie JSON de l'acte au moment de la signature : le document délivré ne change plus ensuite. */
    @Column(name = "contenu_signe", columnDefinition = "TEXT")
    private String contenuSigne;

    /** SHA-256 du contenu signé, imprimé sur le document. */
    private String empreinte;

    @Column(name = "motif_rejet")
    private String motifRejet;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @Version
    private Integer version;

    @PrePersist
    @PreUpdate
    void horodater() {
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateCreation == null) dateCreation = maintenant;
        dateModification = maintenant;
    }

    public boolean appartientA(Long utilisateurId) { return demandeur.getId().equals(utilisateurId); }

    public String getLibelleMotif() {
        if (motif == null) return motifPrecision;
        return motif.estAutre() && motifPrecision != null ? motifPrecision : motif.getLibelle();
    }

    public String getPersonneConcernee() { return prenom + " " + nom; }

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Utilisateur getDemandeur() { return demandeur; }
    public void setDemandeur(Utilisateur demandeur) { this.demandeur = demandeur; }
    public TypeDocument getTypeDocument() { return typeDocument; }
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }
    public Motif getMotif() { return motif; }
    public void setMotif(Motif motif) { this.motif = motif; }
    public String getMotifPrecision() { return motifPrecision; }
    public void setMotifPrecision(String motifPrecision) { this.motifPrecision = motifPrecision; }
    public StatutDemande getStatut() { return statut; }
    void setStatut(StatutDemande statut) { this.statut = statut; }
    public FormatDocument getFormat() { return format; }
    public void setFormat(FormatDocument format) { this.format = format; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public String getVilleLivraison() { return villeLivraison; }
    public void setVilleLivraison(String villeLivraison) { this.villeLivraison = villeLivraison; }
    public LienDemandeur getLienDemandeur() { return lienDemandeur; }
    public void setLienDemandeur(LienDemandeur lienDemandeur) { this.lienDemandeur = lienDemandeur; }
    public int getMontant() { return montant; }
    public void setMontant(int montant) { this.montant = montant; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String v) { this.prenom = v; }
    public String getSexe() { return sexe; }
    public void setSexe(String v) { this.sexe = v; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate v) { this.dateNaissance = v; }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String v) { this.lieuNaissance = v; }
    public String getNomPere() { return nomPere; }
    public void setNomPere(String v) { this.nomPere = v; }
    public String getPrenomPere() { return prenomPere; }
    public void setPrenomPere(String v) { this.prenomPere = v; }
    public String getNomMere() { return nomMere; }
    public void setNomMere(String v) { this.nomMere = v; }
    public String getPrenomMere() { return prenomMere; }
    public void setPrenomMere(String v) { this.prenomMere = v; }
    public String getNomConjoint() { return nomConjoint; }
    public void setNomConjoint(String v) { this.nomConjoint = v; }
    public String getPrenomConjoint() { return prenomConjoint; }
    public void setPrenomConjoint(String v) { this.prenomConjoint = v; }
    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate v) { this.dateEvenement = v; }
    public Long getActeCentralId() { return acteCentralId; }
    void setActeCentralId(Long acteCentralId) { this.acteCentralId = acteCentralId; }
    public String getNumeroActe() { return numeroActe; }
    void setNumeroActe(String numeroActe) { this.numeroActe = numeroActe; }
    public String getNoteRecherche() { return noteRecherche; }
    void setNoteRecherche(String noteRecherche) { this.noteRecherche = noteRecherche; }
    public Utilisateur getSignataire() { return signataire; }
    void setSignataire(Utilisateur signataire) { this.signataire = signataire; }
    public LocalDateTime getDateSignature() { return dateSignature; }
    void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }
    public String getCodeVerification() { return codeVerification; }
    void setCodeVerification(String codeVerification) { this.codeVerification = codeVerification; }
    public String getContenuSigne() { return contenuSigne; }
    void setContenuSigne(String contenuSigne) { this.contenuSigne = contenuSigne; }
    public String getEmpreinte() { return empreinte; }
    void setEmpreinte(String empreinte) { this.empreinte = empreinte; }
    public String getMotifRejet() { return motifRejet; }
    void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
