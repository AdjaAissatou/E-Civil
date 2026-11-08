package EtatCivil.dao;

import EtatCivil.modeles.Permision;
import EtatCivil.modeles.Role;
import EtatCivil.utils.DatabaseListener;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public PermissionDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }

    public PermissionDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion PermissionDAO", e);
        }
    }
    public int getNombrePermissions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM permission";
        try (Statement statement = this.connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return 0;
    }
    public boolean ajouterPermission(Permision permission) throws SQLException {
        String sql = "INSERT INTO permission (nom, description, avantage, interdiction) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, permission.getNom());
            statement.setString(2, permission.getDescription());
            statement.setString(3, permission.getAvantage());
            statement.setString(4, permission.getInterdiction());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            // Récupérer l'ID généré
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permission.setIdpermission(generatedKeys.getInt(1));
                }
            }
            
            return true;
        }
    }

    public List<Permision> getAllPermissions() throws SQLException {
        List<Permision> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permission ORDER BY nom";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Permision permission = new Permision();
                permission.setIdpermission(resultSet.getInt("idpermission"));
                permission.setNom(resultSet.getString("nom"));
                permission.setDescription(resultSet.getString("description"));
                permission.setAvantage(resultSet.getString("avantage"));
                permission.setInterdiction(resultSet.getString("interdiction"));
                permissions.add(permission);
            }
        }
        return permissions;
    }

    public Permision getPermissionById(int id) throws SQLException {
        String sql = "SELECT * FROM permission WHERE idpermission = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Permision permission = new Permision();
                    permission.setIdpermission(resultSet.getInt("idpermission"));
                    permission.setNom(resultSet.getString("nom"));
                    permission.setDescription(resultSet.getString("description"));
                    permission.setAvantage(resultSet.getString("avantage"));
                    permission.setInterdiction(resultSet.getString("interdiction"));
                    return permission;
                }
            }
        }
        return null;
    }
    public List<Role> getRolesByPermission(int permissionId) throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.* FROM role r " +
                     "INNER JOIN role_permissions rp ON r.idrole = rp.role_id " +
                     "WHERE rp.permission_id = ? ORDER BY r.nom";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, permissionId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Role role = new Role();
                    role.setIdrole(resultSet.getInt("idrole"));
                    role.setNom(resultSet.getString("nom"));
                    role.setDescription(resultSet.getString("description"));
                    roles.add(role);
                }
            }
        }
        return roles;
    }
    public boolean mettreAJourPermission(Permision permission) throws SQLException {
        String sql = "UPDATE permission SET nom = ?, description = ?, avantage = ?, interdiction = ? WHERE idpermission = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, permission.getNom());
            statement.setString(2, permission.getDescription());
            statement.setString(3, permission.getAvantage());
            statement.setString(4, permission.getInterdiction());
            statement.setInt(5, permission.getIdpermission());
            
            return statement.executeUpdate() > 0;
        }
    }

    public boolean supprimerPermission(int id) throws SQLException {
        String sql = "DELETE FROM permission WHERE idpermission = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean ajouterRolePermission(int roleId, int permissionId) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            statement.setInt(2, permissionId);
            
            return statement.executeUpdate() > 0;
        }
    }

    public boolean supprimerRolePermission(int roleId, int permissionId) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE role_id = ? AND permission_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            statement.setInt(2, permissionId);
            
            return statement.executeUpdate() > 0;
        }
    }

    public List<Permision> getPermissionsByRole(int roleId) throws SQLException {
        List<Permision> permissions = new ArrayList<>();
        String sql = "SELECT p.* FROM permission p " +
                     "INNER JOIN role_permissions rp ON p.idpermission = rp.permission_id " +
                     "WHERE rp.role_id = ? ORDER BY p.nom";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Permision permission = new Permision();
                    permission.setIdpermission(resultSet.getInt("idpermission"));
                    permission.setNom(resultSet.getString("nom"));
                    permission.setDescription(resultSet.getString("description"));
                    permission.setAvantage(resultSet.getString("avantage"));
                    permission.setInterdiction(resultSet.getString("interdiction"));
                    permissions.add(permission);
                }
            }
        }
        return permissions;
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("PermissionDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("PermissionDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}