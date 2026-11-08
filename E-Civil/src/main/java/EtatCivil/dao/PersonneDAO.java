package EtatCivil.dao;

import java.sql.*;
import EtatCivil.modeles.Personne;
import EtatCivil.utils.DatabaseListener;

public class PersonneDAO {
	  private Connection connection;
	    private boolean shouldCloseConnection = false;

	    // Constructeur avec connexion existante
	    public PersonneDAO(Connection connection) {
	        this.connection = connection;
	        this.shouldCloseConnection = false;
	    }
	    // Constructeur par défaut
	    public PersonneDAO() {
	        try {
	            this.connection = DatabaseListener.getConnection();
	            this.shouldCloseConnection = true;
	        } catch (SQLException e) {
	            throw new RuntimeException("Erreur connexion PersonneDAO", e);
	        }
	    }
	    
	public int enregistrer(Personne personne, Connection conn) throws Exception {
	    String sql = "INSERT INTO personne (prenom, nom, sexe, datedenaissance, lieudenaissance, profession, residance) " +
	                 "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
	        ps.setString(1, personne.getPrenom());
	        ps.setString(2, personne.getNom());
	        ps.setString(3, personne.getSexe());
	        if (personne.getDatedenaissance() != null) {
	            ps.setDate(4, new java.sql.Date(personne.getDatedenaissance().getTime()));
	        } else {
	            ps.setNull(4, java.sql.Types.DATE);
	        }
	        ps.setString(5, personne.getLieudenaissance());
	        ps.setString(6, personne.getProfession());
	        ps.setString(7, personne.getResidance());

	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("id");
	        } else {
	            throw new Exception("Échec de l'insertion de la personne, aucun ID retourné.");
	        }
	    }
	}
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("PersonneDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("PersonneDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}