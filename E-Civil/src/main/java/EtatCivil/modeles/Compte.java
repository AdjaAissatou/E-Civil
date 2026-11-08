package EtatCivil.modeles;

public class Compte {
	private int id;
    public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}

	private int idcompte;
    private int idPersonne; // correspond à la colonne `id` (FK vers personne)
    private String email;
    private String motdepasse;
    private Boolean statut;
    private String role;
 // Ajouter l'utilisateur/personne lié(e) au compte
    private Utilisateur personne; 
   // Ajouter un Utilisateur associé

    public int getIdUtilisateur() {
        if (personne != null) {
            return personne.getIdutilisateur();
        }
        return 0;
    }
    

	public Utilisateur getPersonne() {
		return personne;
	}

	public void setPersonne(Utilisateur personne) {
		this.personne = personne;
	}

	public int getIdcompte() {
        return idcompte;
    }

    public void setIdcompte(int idcompte) {
        this.idcompte = idcompte;
    }

    public int getIdPersonne() {
        return idPersonne;
    }

    public void setIdPersonne(int idPersonne) {
        this.idPersonne = idPersonne;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotdepasse() {
        return motdepasse;
    }

    public void setMotdepasse(String motdepasse) {
        this.motdepasse = motdepasse;
    }

    public Boolean getStatut() {
        return statut;
    }

    public void setStatut(Boolean statut) {
        this.statut = statut;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
