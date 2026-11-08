package EtatCivil.modeles;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class ActeRegistre {
    private int idActe;
    private String typeActe;
    private String numeroActe;
    private String numeroPersonne;
    private String numeroCouple;
    private Date dateActe;
    private String lieuActe;

    // --- Informations personnelles ---
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private String lieuNaissance;

    // --- Parents (naissance) ---
    private String nomPere;
    private String prenomPere;
    private Date dateNaissancePere;
    private String nomMere;
    private String prenomMere;
    private Date dateNaissanceMere;

    // --- Conjoint (mariage/divorce) ---
    private String nomConjoint;
    private String prenomConjoint;
    private Date dateNaissanceConjoint;

    // --- Mariage ---
    private String lieuMariage;
    private String temoin1;
    private String temoin2;

    // --- Décès ---
    private Date dateDeces;
    private String heureDeces;
    private String causeDeces;
    private String lieuDeces;

    // --- Registre ---
    private String volume;
    private String folio;
    private String annee;
    private String centre;
    private String observations;

    // --- Commun ---
    private int idAgent;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateRenseignement;

    // Getters / Setters avec annotations JSON
    @JsonProperty("id_acte")
    public int getIdActe() { return idActe; }
    public void setIdActe(int idActe) { this.idActe = idActe; }

    @JsonProperty("type_acte")
    public String getTypeActe() { return typeActe; }
    public void setTypeActe(String typeActe) { this.typeActe = typeActe; }

    @JsonProperty("numero_acte")
    public String getNumeroActe() { return numeroActe; }
    public void setNumeroActe(String numeroActe) { this.numeroActe = numeroActe; }

    @JsonProperty("numero_personne")
    public String getNumeroPersonne() { return numeroPersonne; }
    public void setNumeroPersonne(String numeroPersonne) { this.numeroPersonne = numeroPersonne; }

    @JsonProperty("numero_couple")
    public String getNumeroCouple() { return numeroCouple; }
    public void setNumeroCouple(String numeroCouple) { this.numeroCouple = numeroCouple; }

    @JsonProperty("date_acte")
    public Date getDateActe() { return dateActe; }
    public void setDateActe(Date dateActe) { this.dateActe = dateActe; }

    @JsonProperty("lieu_acte")
    public String getLieuActe() { return lieuActe; }
    public void setLieuActe(String lieuActe) { this.lieuActe = lieuActe; }

    @JsonProperty("nom")
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    @JsonProperty("prenom")
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    @JsonProperty("date_naissance")
    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    @JsonProperty("lieu_naissance")
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }

    @JsonProperty("nom_pere")
    public String getNomPere() { return nomPere; }
    public void setNomPere(String nomPere) { this.nomPere = nomPere; }

    @JsonProperty("prenom_pere")
    public String getPrenomPere() { return prenomPere; }
    public void setPrenomPere(String prenomPere) { this.prenomPere = prenomPere; }

    @JsonProperty("date_naissance_pere")
    public Date getDateNaissancePere() { return dateNaissancePere; }
    public void setDateNaissancePere(Date dateNaissancePere) { this.dateNaissancePere = dateNaissancePere; }

    @JsonProperty("nom_mere")
    public String getNomMere() { return nomMere; }
    public void setNomMere(String nomMere) { this.nomMere = nomMere; }

    @JsonProperty("prenom_mere")
    public String getPrenomMere() { return prenomMere; }
    public void setPrenomMere(String prenomMere) { this.prenomMere = prenomMere; }

    @JsonProperty("date_naissance_mere")
    public Date getDateNaissanceMere() { return dateNaissanceMere; }
    public void setDateNaissanceMere(Date dateNaissanceMere) { this.dateNaissanceMere = dateNaissanceMere; }

    @JsonProperty("nom_conjoint")
    public String getNomConjoint() { return nomConjoint; }
    public void setNomConjoint(String nomConjoint) { this.nomConjoint = nomConjoint; }

    @JsonProperty("prenom_conjoint")
    public String getPrenomConjoint() { return prenomConjoint; }
    public void setPrenomConjoint(String prenomConjoint) { this.prenomConjoint = prenomConjoint; }

    @JsonProperty("date_naissance_conjoint")
    public Date getDateNaissanceConjoint() { return dateNaissanceConjoint; }
    public void setDateNaissanceConjoint(Date dateNaissanceConjoint) { this.dateNaissanceConjoint = dateNaissanceConjoint; }

    @JsonProperty("lieu_mariage")
    public String getLieuMariage() { return lieuMariage; }
    public void setLieuMariage(String lieuMariage) { this.lieuMariage = lieuMariage; }

    @JsonProperty("temoin1")
    public String getTemoin1() { return temoin1; }
    public void setTemoin1(String temoin1) { this.temoin1 = temoin1; }

    @JsonProperty("temoin2")
    public String getTemoin2() { return temoin2; }
    public void setTemoin2(String temoin2) { this.temoin2 = temoin2; }

    @JsonProperty("date_deces")
    public Date getDateDeces() { return dateDeces; }
    public void setDateDeces(Date dateDeces) { this.dateDeces = dateDeces; }

    @JsonProperty("heure_deces")
    public String getHeureDeces() { return heureDeces; }
    public void setHeureDeces(String heureDeces) { this.heureDeces = heureDeces; }

    @JsonProperty("cause_deces")
    public String getCauseDeces() { return causeDeces; }
    public void setCauseDeces(String causeDeces) { this.causeDeces = causeDeces; }

    @JsonProperty("lieu_deces")
    public String getLieuDeces() { return lieuDeces; }
    public void setLieuDeces(String lieuDeces) { this.lieuDeces = lieuDeces; }

    @JsonProperty("volume")
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    @JsonProperty("folio")
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    @JsonProperty("annee")
    public String getAnnee() { return annee; }
    public void setAnnee(String annee) { this.annee = annee; }

    @JsonProperty("centre")
    public String getCentre() { return centre; }
    public void setCentre(String centre) { this.centre = centre; }

    @JsonProperty("observations")
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    @JsonProperty("id_agent")
    public int getIdAgent() { return idAgent; }
    public void setIdAgent(int idAgent) { this.idAgent = idAgent; }

    @JsonProperty("date_renseignement")
    public Date getDateRenseignement() { return dateRenseignement; }
    public void setDateRenseignement(Date dateRenseignement) { this.dateRenseignement = dateRenseignement; }
}