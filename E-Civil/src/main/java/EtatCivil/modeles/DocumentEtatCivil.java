package EtatCivil.modeles;

import java.sql.Date;
import java.sql.Timestamp;

public class DocumentEtatCivil {
    private int iddocument;
    private int idcommune;
    private int numeroregistre;
    private int numerodocument;
    private Date dateevenement;
    private String lieuevenement;
    private Date dateenregistrement;
    private int idnature;
    private String statut;
    private Timestamp dateCreation;
    private Timestamp dateModification;
    private String createdBy;

    // --- Getters et Setters ---

    public int getIddocument() {
        return iddocument;
    }

    public void setIddocument(int iddocument) {
        this.iddocument = iddocument;
    }

    public int getIdcommune() {
        return idcommune;
    }

    public void setIdcommune(int idcommune) {
        this.idcommune = idcommune;
    }

    public int getNumeroregistre() {
        return numeroregistre;
    }

    public void setNumeroregistre(int numeroregistre) {
        this.numeroregistre = numeroregistre;
    }

    public int getNumerodocument() {
        return numerodocument;
    }

    public void setNumerodocument(int numerodocument) {
        this.numerodocument = numerodocument;
    }

    public Date getDateevenement() {
        return dateevenement;
    }

    public void setDateevenement(Date dateevenement) {
        this.dateevenement = dateevenement;
    }

    public String getLieuevenement() {
        return lieuevenement;
    }

    public void setLieuevenement(String lieuevenement) {
        this.lieuevenement = lieuevenement;
    }

    public Date getDateenregistrement() {
        return dateenregistrement;
    }

    public void setDateenregistrement(Date dateenregistrement) {
        this.dateenregistrement = dateenregistrement;
    }

    public int getIdnature() {
        return idnature;
    }

    public void setIdnature(int idnature) {
        this.idnature = idnature;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Timestamp getDateModification() {
        return dateModification;
    }

    public void setDateModification(Timestamp dateModification) {
        this.dateModification = dateModification;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // --- ToString pour debug ---
    @Override
    public String toString() {
        return "DocumentEtatCivil{" +
                "iddocument=" + iddocument +
                ", idcommune=" + idcommune +
                ", numeroregistre=" + numeroregistre +
                ", numerodocument=" + numerodocument +
                ", dateevenement=" + dateevenement +
                ", lieuevenement='" + lieuevenement + '\'' +
                ", dateenregistrement=" + dateenregistrement +
                ", idnature=" + idnature +
                ", statut='" + statut + '\'' +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }

	public DocumentEtatCivil(int iddocument, int idcommune, int numeroregistre, int numerodocument, Date dateevenement,
			String lieuevenement, Date dateenregistrement, int idnature, String statut, Timestamp dateCreation,
			Timestamp dateModification, String createdBy) {
		super();
		this.iddocument = iddocument;
		this.idcommune = idcommune;
		this.numeroregistre = numeroregistre;
		this.numerodocument = numerodocument;
		this.dateevenement = dateevenement;
		this.lieuevenement = lieuevenement;
		this.dateenregistrement = dateenregistrement;
		this.idnature = idnature;
		this.statut = statut;
		this.dateCreation = dateCreation;
		this.dateModification = dateModification;
		this.createdBy = createdBy;
	}

	public DocumentEtatCivil() {
		// TODO Auto-generated constructor stub
	}


}
