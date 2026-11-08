package EtatCivil.dao;

import EtatCivil.modeles.Profil;
import EtatCivil.modeles.Role;
import EtatCivil.utils.DatabaseListener;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfilDAO {
	   private Connection connection;
	    private boolean shouldCloseConnection = false;

	    // Constructeur avec connexion existante (gérée ailleurs)
	    public ProfilDAO(Connection connection) {
	        this.connection = connection;
	        this.shouldCloseConnection = false; // On ne ferme pas une connexion qu'on n'a pas créée
	    }

	    // Constructeur par défaut qui crée sa propre connexion
	    public ProfilDAO() {
	        try {
	            this.connection = DatabaseListener.getConnection();
	            this.shouldCloseConnection = true; // On ferme cette connexion
	        } catch (SQLException e) {
	            throw new RuntimeException("Erreur lors de l'obtention de la connexion", e);
	        }
	    }
	    public void close() {
	        if (shouldCloseConnection && connection != null) {
	            try {
	                connection.close();
	            } catch (SQLException e) {
	                System.err.println("Erreur fermeture connexion: " + e.getMessage());
	            }
	        }
	    }
    public boolean ajouterProfil(Profil profil) throws SQLException {
        String sql = "INSERT INTO profil (nom, idrole) VALUES (?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profil.getNom());
            statement.setInt(2, profil.getRole().getIdrole());
            
            return statement.executeUpdate() > 0;
        }
    }

    public List<Profil> getAllProfils() throws SQLException {
        List<Profil> profils = new ArrayList<>();
        String sql = "SELECT p.*, r.nom as role_nom, r.description as role_description " +
                     "FROM profil p LEFT JOIN role r ON p.idrole = r.idrole ORDER BY p.nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Profil profil = new Profil();
                profil.setIdProfil(rs.getInt("idProfil"));
                profil.setNom(rs.getString("nom"));
                
                Role role = new Role();
                role.setIdrole(rs.getInt("idrole"));
                role.setNom(rs.getString("role_nom"));
                role.setDescription(rs.getString("role_description"));
                
                profil.setRole(role);
                profils.add(profil);
            }
        }
        return profils;
    }

    public Profil getProfilById(int id) throws SQLException {
        String sql = "SELECT p.*, r.nom as role_nom, r.description as role_description " +
                     "FROM profil p LEFT JOIN role r ON p.idrole = r.idrole WHERE p.idProfil = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Profil profil = new Profil();
                    profil.setIdProfil(resultSet.getInt("idProfil"));
                    profil.setNom(resultSet.getString("nom"));
                    
                    Role role = new Role();
                    role.setIdrole(resultSet.getInt("idrole"));
                    role.setNom(resultSet.getString("role_nom"));
                    role.setDescription(resultSet.getString("role_description"));
                    
                    profil.setRole(role);
                    return profil;
                }
            }
        }
        return null;
    }

    public boolean mettreAJourProfil(Profil profil) throws SQLException {
        String sql = "UPDATE profil SET nom = ?, idrole = ? WHERE idProfil = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profil.getNom());
            statement.setInt(2, profil.getRole().getIdrole());
            statement.setInt(3, profil.getIdProfil());
            
            return statement.executeUpdate() > 0;
        }
    }

    public boolean supprimerProfil(int id) throws SQLException {
        String sql = "DELETE FROM profil WHERE idProfil = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}