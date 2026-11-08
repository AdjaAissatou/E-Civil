package EtatCivil.dao;

import EtatCivil.modeles.Role;
import EtatCivil.utils.DatabaseListener;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
	   private Connection connection;
	    private boolean shouldCloseConnection = false;

	    public RoleDAO(Connection connection) {
	        this.connection = connection;
	        this.shouldCloseConnection = false;
	    }

	    public RoleDAO() {
	        try {
	            this.connection = DatabaseListener.getConnection();
	            this.shouldCloseConnection = true;
	        } catch (SQLException e) {
	            throw new RuntimeException("Erreur connexion RoleDAO", e);
	        }
	    }
    public int getNombreRoles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM role";
        try (Statement statement = this.connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
    public boolean ajouterRole(Role role) throws SQLException {
        String sql = "INSERT INTO role (nom, description) VALUES (?, ?)";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, role.getNom());
            statement.setString(2, role.getDescription());
            
            return statement.executeUpdate() > 0;
        }
    }

    public List<Role> getAllRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM role ORDER BY nom";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Role role = new Role();
                role.setIdrole(resultSet.getInt("idrole"));
                role.setNom(resultSet.getString("nom"));
                role.setDescription(resultSet.getString("description"));
                roles.add(role);
            }
        }
        return roles;
    }

    public Role getRoleById(int id) throws SQLException {
        String sql = "SELECT * FROM role WHERE idrole = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Role role = new Role();
                    role.setIdrole(resultSet.getInt("idrole"));
                    role.setNom(resultSet.getString("nom"));
                    role.setDescription(resultSet.getString("description"));
                    return role;
                }
            }
        }
        return null;
    }

    public boolean mettreAJourRole(Role role) throws SQLException {
        String sql = "UPDATE role SET nom = ?, description = ? WHERE idrole = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, role.getNom());
            statement.setString(2, role.getDescription());
            statement.setInt(3, role.getIdrole());
            
            return statement.executeUpdate() > 0;
        }
    }

    public boolean supprimerRole(int id) throws SQLException {
        String sql = "DELETE FROM role WHERE idrole = ?";
        
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("RoleDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("RoleDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}