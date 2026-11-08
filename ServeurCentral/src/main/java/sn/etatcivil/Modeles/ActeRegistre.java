package sn.etatcivil.Modeles;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "actes_registre")
public class ActeRegistre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "type_acte")
    @JsonProperty("type_acte") // ← AJOUTER
    private String typeActe;
    
    @Column(name = "numero_acte")
    @JsonProperty("numero_acte") // ← AJOUTER
    private String numeroActe;
    
    @Column(name = "numero_personne")
    @JsonProperty("numero_personne") // ← AJOUTER
    private String numeroPersonne;
    
    @Column(name = "numero_couple")
    @JsonProperty("numero_couple") // ← AJOUTER
    private String numeroCouple;
    
    @Column(name = "date_acte")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_acte") // ← AJOUTER
    private LocalDate dateActe;
    
    @Column(name = "lieu_acte")
    @JsonProperty("lieu_acte") // ← AJOUTER
    private String lieuActe;
    
    @JsonProperty("nom") // ← AJOUTER
    private String nom;
    
    @JsonProperty("prenom") // ← AJOUTER
    private String prenom;
    
    @Column(name = "date_naissance")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_naissance") // ← AJOUTER
    private LocalDate dateNaissance;
    
    @Column(name = "lieu_naissance")
    @JsonProperty("lieu_naissance") // ← AJOUTER
    private String lieuNaissance;
    
    @Column(name = "nom_pere")
    @JsonProperty("nom_pere") // ← AJOUTER
    private String nomPere;
    
    @Column(name = "prenom_pere")
    @JsonProperty("prenom_pere") // ← AJOUTER
    private String prenomPere;
    
    @Column(name = "date_naissance_pere")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_naissance_pere") // ← AJOUTER
    private LocalDate dateNaissancePere;
    
    @Column(name = "nom_mere")
    @JsonProperty("nom_mere") // ← AJOUTER
    private String nomMere;
    
    @Column(name = "prenom_mere")
    @JsonProperty("prenom_mere") // ← AJOUTER
    private String prenomMere;
    
    @Column(name = "date_naissance_mere")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_naissance_mere") // ← AJOUTER
    private LocalDate dateNaissanceMere;
    
    @Column(name = "nom_conjoint")
    @JsonProperty("nom_conjoint") // ← AJOUTER
    private String nomConjoint;
    
    @Column(name = "prenom_conjoint")
    @JsonProperty("prenom_conjoint") // ← AJOUTER
    private String prenomConjoint;
    
    @Column(name = "date_naissance_conjoint")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_naissance_conjoint") // ← AJOUTER
    private LocalDate dateNaissanceConjoint;
    
    @Column(name = "lieu_mariage")
    @JsonProperty("lieu_mariage") // ← AJOUTER
    private String lieuMariage;
    
    @Column(name = "temoin1")
    @JsonProperty("temoin1") // ← AJOUTER
    private String temoin1;
    
    @Column(name = "temoin2")
    @JsonProperty("temoin2") // ← AJOUTER
    private String temoin2;
    
    @Column(name = "date_deces")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_deces") // ← AJOUTER
    private LocalDate dateDeces;
    
    @Column(name = "heure_deces")
    @JsonProperty("heure_deces") // ← AJOUTER
    private String heureDeces;
    
    @Column(name = "cause_deces")
    @JsonProperty("cause_deces") // ← AJOUTER
    private String causeDeces;
    
    @Column(name = "lieu_deces")
    @JsonProperty("lieu_deces") // ← AJOUTER
    private String lieuDeces;
    
    @JsonProperty("volume") // ← AJOUTER
    private String volume;
    
    @JsonProperty("folio") // ← AJOUTER
    private String folio;
    
    @JsonProperty("annee") // ← AJOUTER
    private String annee;
    
    @JsonProperty("centre") // ← AJOUTER
    private String centre;
    
    @JsonProperty("observations") // ← AJOUTER
    private String observations;
    
    @JsonProperty("statut") // ← AJOUTER
    private String statut;
    
    @Column(name = "date_saisie")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_saisie") // ← AJOUTER
    private LocalDateTime dateSaisie;
    
    @Column(name = "date_modification")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("date_modification") // ← AJOUTER
    private LocalDateTime dateModification;
    
    @Column(name = "id_agent")
    @JsonProperty("id_agent") // ← AJOUTER
    private Integer idAgent;

    // Getters and Setters (inchangés)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getTypeActe() { return typeActe; }
    public void setTypeActe(String typeActe) { this.typeActe = typeActe; }
    
    public String getNumeroActe() { return numeroActe; }
    public void setNumeroActe(String numeroActe) { this.numeroActe = numeroActe; }
    
    public String getNumeroPersonne() { return numeroPersonne; }
    public void setNumeroPersonne(String numeroPersonne) { this.numeroPersonne = numeroPersonne; }
    
    public String getNumeroCouple() { return numeroCouple; }
    public void setNumeroCouple(String numeroCouple) { this.numeroCouple = numeroCouple; }
    
    public LocalDate getDateActe() { return dateActe; }
    public void setDateActe(LocalDate dateActe) { this.dateActe = dateActe; }
    
    public String getLieuActe() { return lieuActe; }
    public void setLieuActe(String lieuActe) { this.lieuActe = lieuActe; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    
    public String getNomPere() { return nomPere; }
    public void setNomPere(String nomPere) { this.nomPere = nomPere; }
    
    public String getPrenomPere() { return prenomPere; }
    public void setPrenomPere(String prenomPere) { this.prenomPere = prenomPere; }
    
    public LocalDate getDateNaissancePere() { return dateNaissancePere; }
    public void setDateNaissancePere(LocalDate dateNaissancePere) { this.dateNaissancePere = dateNaissancePere; }
    
    public String getNomMere() { return nomMere; }
    public void setNomMere(String nomMere) { this.nomMere = nomMere; }
    
    public String getPrenomMere() { return prenomMere; }
    public void setPrenomMere(String prenomMere) { this.prenomMere = prenomMere; }
    
    public LocalDate getDateNaissanceMere() { return dateNaissanceMere; }
    public void setDateNaissanceMere(LocalDate dateNaissanceMere) { this.dateNaissanceMere = dateNaissanceMere; }
    
    public String getNomConjoint() { return nomConjoint; }
    public void setNomConjoint(String nomConjoint) { this.nomConjoint = nomConjoint; }
    
    public String getPrenomConjoint() { return prenomConjoint; }
    public void setPrenomConjoint(String prenomConjoint) { this.prenomConjoint = prenomConjoint; }
    
    public LocalDate getDateNaissanceConjoint() { return dateNaissanceConjoint; }
    public void setDateNaissanceConjoint(LocalDate dateNaissanceConjoint) { this.dateNaissanceConjoint = dateNaissanceConjoint; }
    
    public String getLieuMariage() { return lieuMariage; }
    public void setLieuMariage(String lieuMariage) { this.lieuMariage = lieuMariage; }
    
    public String getTemoin1() { return temoin1; }
    public void setTemoin1(String temoin1) { this.temoin1 = temoin1; }
    
    public String getTemoin2() { return temoin2; }
    public void setTemoin2(String temoin2) { this.temoin2 = temoin2; }
    
    public LocalDate getDateDeces() { return dateDeces; }
    public void setDateDeces(LocalDate dateDeces) { this.dateDeces = dateDeces; }
    
    public String getHeureDeces() { return heureDeces; }
    public void setHeureDeces(String heureDeces) { this.heureDeces = heureDeces; }
    
    public String getCauseDeces() { return causeDeces; }
    public void setCauseDeces(String causeDeces) { this.causeDeces = causeDeces; }
    
    public String getLieuDeces() { return lieuDeces; }
    public void setLieuDeces(String lieuDeces) { this.lieuDeces = lieuDeces; }
    
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    
    public String getAnnee() { return annee; }
    public void setAnnee(String annee) { this.annee = annee; }
    
    public String getCentre() { return centre; }
    public void setCentre(String centre) { this.centre = centre; }
    
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public LocalDateTime getDateSaisie() { return dateSaisie; }
    public void setDateSaisie(LocalDateTime dateSaisie) { this.dateSaisie = dateSaisie; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    public Integer getIdAgent() { return idAgent; }
    public void setIdAgent(Integer idAgent) { this.idAgent = idAgent; }
}