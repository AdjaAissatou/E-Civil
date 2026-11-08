package EtatCivil.modeles;

public class Activite {
    private String titre;
    private String date;
    private String utilisateur;
    
    public Activite() {}
    
    public Activite(String titre, String date, String utilisateur) {
        this.titre = titre;
        this.date = date;
        this.utilisateur = utilisateur;
    }
    
    // Getters et setters
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
}