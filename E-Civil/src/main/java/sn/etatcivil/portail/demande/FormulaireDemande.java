package sn.etatcivil.portail.demande;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Saisie du citoyen. Les règles propres à chaque type d'acte sont vérifiées par {@link DemandeService}. */
public class FormulaireDemande {
    @NotNull private Long typeDocumentId;
    @NotNull(message = "choisissez un motif") private Long motifId;
    @Size(max = 255) private String motifPrecision;
    @NotNull private LienDemandeur lienDemandeur;
    @NotNull private FormatDocument format;
    @Size(max = 255) private String adresseLivraison;
    @Size(max = 100) private String villeLivraison;
    @NotBlank @Size(max = 100) private String nom;
    @NotBlank @Size(max = 150) private String prenom;
    @Pattern(regexp = "^$|[MF]") private String sexe;
    @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateNaissance;
    @Size(max = 150) private String lieuNaissance;
    @Size(max = 100) private String nomPere;
    @Size(max = 150) private String prenomPere;
    @Size(max = 100) private String nomMere;
    @Size(max = 150) private String prenomMere;
    @Size(max = 100) private String nomConjoint;
    @Size(max = 150) private String prenomConjoint;
    @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateEvenement;

    public Long getTypeDocumentId() { return typeDocumentId; }
    public void setTypeDocumentId(Long v) { this.typeDocumentId = v; }
    public Long getMotifId() { return motifId; }
    public void setMotifId(Long v) { this.motifId = v; }
    public String getMotifPrecision() { return motifPrecision; }
    public void setMotifPrecision(String v) { this.motifPrecision = v; }
    public LienDemandeur getLienDemandeur() { return lienDemandeur; }
    public void setLienDemandeur(LienDemandeur v) { this.lienDemandeur = v; }
    public FormatDocument getFormat() { return format; }
    public void setFormat(FormatDocument v) { this.format = v; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String v) { this.adresseLivraison = v; }
    public String getVilleLivraison() { return villeLivraison; }
    public void setVilleLivraison(String v) { this.villeLivraison = v; }
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
}
