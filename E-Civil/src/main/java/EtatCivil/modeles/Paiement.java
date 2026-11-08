package EtatCivil.modeles;
import java.util.Date;

public class Paiement {
    private int idPaiement;
    private Date datePaiement;
    private double montant;
    private String status;
    private String recu;
    private String modePaiement;
    private String reference; // Nouveau champ pour la référence de transaction

    public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	// Constructeurs
    public Paiement() {}
    
    public Paiement(double montant, String modePaiement) {
        this.datePaiement = new Date();
        this.montant = montant;
        this.status = "en_attente";
        this.modePaiement = modePaiement;
    }

    // Getters et Setters
    public int getIdPaiement() { return idPaiement; }
    public void setIdPaiement(int idPaiement) { this.idPaiement = idPaiement; }

    public Date getDatePaiement() { return datePaiement; }
    public void setDatePaiement(Date datePaiement) { this.datePaiement = datePaiement; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecu() { return recu; }
    public void setRecu(String recu) { this.recu = recu; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
}