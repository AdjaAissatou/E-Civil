package sn.etatcivil.central.acte;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "acte")
public class Acte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acte", nullable = false)
    private TypeActe typeActe;

    @Column(name = "numero_acte", nullable = false, unique = true, updatable = false)
    private String numeroActe;

    @Column(nullable = false, updatable = false)
    private Integer annee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutActe statut = StatutActe.SAISI;

    @Column(name = "centre")
    private String centre;
    @Column(name = "commune")
    private String commune;
    @Column(name = "volume")
    private String volume;
    @Column(name = "folio")
    private String folio;
    @Column(name = "date_evenement")
    private LocalDate dateEvenement;
    @Column(name = "lieu_evenement")
    private String lieuEvenement;
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
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
    @Column(name = "date_naissance_conjoint")
    private LocalDate dateNaissanceConjoint;
    @Column(name = "lieu_naissance_conjoint")
    private String lieuNaissanceConjoint;
    @Column(name = "heure_deces")
    private String heureDeces;
    @Column(name = "cause_deces")
    private String causeDeces;
    @Column(name = "temoin1")
    private String temoin1;
    @Column(name = "temoin2")
    private String temoin2;
    @Column(name = "mentions_marginales")
    private String mentionsMarginales;
    @Column(name = "observations")
    private String observations;

    @Column(name = "nom_normalise", nullable = false)
    private String nomNormalise;

    @Column(name = "prenom_normalise", nullable = false)
    private String prenomNormalise;

    @Column(name = "nom_conjoint_normalise")
    private String nomConjointNormalise;

    @Column(name = "prenom_conjoint_normalise")
    private String prenomConjointNormalise;

    @Column(name = "agent_saisie", nullable = false, updatable = false)
    private String agentSaisie;

    @Column(name = "date_saisie", nullable = false, updatable = false)
    private LocalDateTime dateSaisie;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @Version
    private Integer version;

    @PrePersist
    @PreUpdate
    void avantEnregistrement() {
        nomNormalise = Normalisation.normaliser(nom);
        prenomNormalise = Normalisation.normaliser(prenom);
        nomConjointNormalise = Normalisation.normaliser(nomConjoint);
        prenomConjointNormalise = Normalisation.normaliser(prenomConjoint);
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateSaisie == null) dateSaisie = maintenant;
        dateModification = maintenant;
    }

    public Long getId() { return id; }
    public TypeActe getTypeActe() { return typeActe; }
    public void setTypeActe(TypeActe typeActe) { this.typeActe = typeActe; }
    public String getNumeroActe() { return numeroActe; }
    public void setNumeroActe(String numeroActe) { this.numeroActe = numeroActe; }
    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    public StatutActe getStatut() { return statut; }
    public void setStatut(StatutActe statut) { this.statut = statut; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getCentre() { return centre; }
    public void setCentre(String centre) { this.centre = centre; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }
    public String getLieuEvenement() { return lieuEvenement; }
    public void setLieuEvenement(String lieuEvenement) { this.lieuEvenement = lieuEvenement; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    public String getNomPere() { return nomPere; }
    public void setNomPere(String nomPere) { this.nomPere = nomPere; }
    public String getPrenomPere() { return prenomPere; }
    public void setPrenomPere(String prenomPere) { this.prenomPere = prenomPere; }
    public String getNomMere() { return nomMere; }
    public void setNomMere(String nomMere) { this.nomMere = nomMere; }
    public String getPrenomMere() { return prenomMere; }
    public void setPrenomMere(String prenomMere) { this.prenomMere = prenomMere; }
    public String getNomConjoint() { return nomConjoint; }
    public void setNomConjoint(String nomConjoint) { this.nomConjoint = nomConjoint; }
    public String getPrenomConjoint() { return prenomConjoint; }
    public void setPrenomConjoint(String prenomConjoint) { this.prenomConjoint = prenomConjoint; }
    public LocalDate getDateNaissanceConjoint() { return dateNaissanceConjoint; }
    public void setDateNaissanceConjoint(LocalDate dateNaissanceConjoint) { this.dateNaissanceConjoint = dateNaissanceConjoint; }
    public String getLieuNaissanceConjoint() { return lieuNaissanceConjoint; }
    public void setLieuNaissanceConjoint(String lieuNaissanceConjoint) { this.lieuNaissanceConjoint = lieuNaissanceConjoint; }
    public String getHeureDeces() { return heureDeces; }
    public void setHeureDeces(String heureDeces) { this.heureDeces = heureDeces; }
    public String getCauseDeces() { return causeDeces; }
    public void setCauseDeces(String causeDeces) { this.causeDeces = causeDeces; }
    public String getTemoin1() { return temoin1; }
    public void setTemoin1(String temoin1) { this.temoin1 = temoin1; }
    public String getTemoin2() { return temoin2; }
    public void setTemoin2(String temoin2) { this.temoin2 = temoin2; }
    public String getMentionsMarginales() { return mentionsMarginales; }
    public void setMentionsMarginales(String mentionsMarginales) { this.mentionsMarginales = mentionsMarginales; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getAgentSaisie() { return agentSaisie; }
    public void setAgentSaisie(String agentSaisie) { this.agentSaisie = agentSaisie; }
    public LocalDateTime getDateSaisie() { return dateSaisie; }
    public LocalDateTime getDateModification() { return dateModification; }
}
