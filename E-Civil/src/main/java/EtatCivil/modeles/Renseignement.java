package EtatCivil.modeles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class Renseignement {
    private int idRenseignement;
    private int idDemandeCentral;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date dateReception;
    
    private int priorite;
    private String statut;
    
    // Informations de renseignement
    private String numeroActe;
    private String volume;
    private String folio;
    private String annee;
    private String commune;
    private String centre;
    private String observations;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date dateRenseignement;
    
    private int idAgent;
    
    // Informations de la demande
    private String nom;
    private String prenom;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateNaissance;
    
    private String lieuNaissance;
    private String nomPere;
    private String prenomPere;
    private String nomMere;
    private String prenomMere;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateEvenement;
    
    private String lieuEvenement;
    private String typeActe;

    // Getters et Setters avec annotations @JsonProperty
    @JsonProperty("id_renseignement")
    public int getIdRenseignement() { return idRenseignement; }
    
    @JsonProperty("id_renseignement")
    public void setIdRenseignement(int idRenseignement) { this.idRenseignement = idRenseignement; }
    
    @JsonProperty("id_demande_central")
    public int getIdDemandeCentral() { return idDemandeCentral; }
    
    @JsonProperty("id_demande_central")
    public void setIdDemandeCentral(int idDemandeCentral) { this.idDemandeCentral = idDemandeCentral; }
    
    @JsonProperty("date_reception")
    public Date getDateReception() { return dateReception; }
    
    @JsonProperty("date_reception")
    public void setDateReception(Date dateReception) { this.dateReception = dateReception; }
    
    @JsonProperty("priorite")
    public int getPriorite() { return priorite; }
    
    @JsonProperty("priorite")
    public void setPriorite(int priorite) { this.priorite = priorite; }
    
    @JsonProperty("statut")
    public String getStatut() { return statut; }
    
    @JsonProperty("statut")
    public void setStatut(String statut) { this.statut = statut; }
    
    @JsonProperty("numero_acte")
    public String getNumeroActe() { return numeroActe; }
    
    @JsonProperty("numero_acte")
    public void setNumeroActe(String numeroActe) { this.numeroActe = numeroActe; }
    
    @JsonProperty("volume")
    public String getVolume() { return volume; }
    
    @JsonProperty("volume")
    public void setVolume(String volume) { this.volume = volume; }
    
    @JsonProperty("folio")
    public String getFolio() { return folio; }
    
    @JsonProperty("folio")
    public void setFolio(String folio) { this.folio = folio; }
    
    @JsonProperty("annee")
    public String getAnnee() { return annee; }
    
    @JsonProperty("annee")
    public void setAnnee(String annee) { this.annee = annee; }
    
    @JsonProperty("commune")
    public String getCommune() { return commune; }
    
    @JsonProperty("commune")
    public void setCommune(String commune) { this.commune = commune; }
    
    @JsonProperty("centre")
    public String getCentre() { return centre; }
    
    @JsonProperty("centre")
    public void setCentre(String centre) { this.centre = centre; }
    
    @JsonProperty("observations")
    public String getObservations() { return observations; }
    
    @JsonProperty("observations")
    public void setObservations(String observations) { this.observations = observations; }
    
    @JsonProperty("date_renseignement")
    public Date getDateRenseignement() { return dateRenseignement; }
    
    @JsonProperty("date_renseignement")
    public void setDateRenseignement(Date dateRenseignement) { this.dateRenseignement = dateRenseignement; }
    
    @JsonProperty("id_agent")
    public int getIdAgent() { return idAgent; }
    
    @JsonProperty("id_agent")
    public void setIdAgent(int idAgent) { this.idAgent = idAgent; }
    
    @JsonProperty("nom")
    public String getNom() { return nom; }
    
    @JsonProperty("nom")
    public void setNom(String nom) { this.nom = nom; }
    
    @JsonProperty("prenom")
    public String getPrenom() { return prenom; }
    
    @JsonProperty("prenom")
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    @JsonProperty("date_naissance")
    public Date getDateNaissance() { return dateNaissance; }
    
    @JsonProperty("date_naissance")
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }
    
    @JsonProperty("lieu_naissance")
    public String getLieuNaissance() { return lieuNaissance; }
    
    @JsonProperty("lieu_naissance")
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    
    @JsonProperty("nom_pere")
    public String getNomPere() { return nomPere; }
    
    @JsonProperty("nom_pere")
    public void setNomPere(String nomPere) { this.nomPere = nomPere; }
    
    @JsonProperty("prenom_pere")
    public String getPrenomPere() { return prenomPere; }
    
    @JsonProperty("prenom_pere")
    public void setPrenomPere(String prenomPere) { this.prenomPere = prenomPere; }
    
    @JsonProperty("nom_mere")
    public String getNomMere() { return nomMere; }
    
    @JsonProperty("nom_mere")
    public void setNomMere(String nomMere) { this.nomMere = nomMere; }
    
    @JsonProperty("prenom_mere")
    public String getPrenomMere() { return prenomMere; }
    
    @JsonProperty("prenom_mere")
    public void setPrenomMere(String prenomMere) { this.prenomMere = prenomMere; }
    
    @JsonProperty("date_evenement")
    public Date getDateEvenement() { return dateEvenement; }
    
    @JsonProperty("date_evenement")
    public void setDateEvenement(Date dateEvenement) { this.dateEvenement = dateEvenement; }
    
    @JsonProperty("lieu_evenement")
    public String getLieuEvenement() { return lieuEvenement; }
    
    @JsonProperty("lieu_evenement")
    public void setLieuEvenement(String lieuEvenement) { this.lieuEvenement = lieuEvenement; }
    
    @JsonProperty("type_acte")
    public String getTypeActe() { return typeActe; }
    
    @JsonProperty("type_acte")
    public void setTypeActe(String typeActe) { this.typeActe = typeActe; }
    
    // Méthode utilitaire pour le debug
    @JsonIgnore
    public String getInfoString() {
        return String.format("Renseignement[ID=%d, Nom=%s %s, Type=%s, Statut=%s]", 
            idRenseignement, prenom, nom, typeActe, statut);
    }
}