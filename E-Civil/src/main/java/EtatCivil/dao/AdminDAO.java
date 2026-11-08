package EtatCivil.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import EtatCivil.utils.DatabaseListener;

public class AdminDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;
    
    public AdminDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }
    public AdminDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion AdminDAO", e);
        }
    }


    // ✅ Attribuer un rôle à un compte
    public boolean attribuerRole(int idCompte, String role) {
        String sql = "UPDATE compte SET role=? WHERE idcompte=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // CORRECTION: utilise connection, pas ConnexionBD

            ps.setString(1, role);
            ps.setInt(2, idCompte);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Activer/Désactiver un utilisateur
    public boolean changerStatutCompte(int idCompte, boolean statut) {
        String sql = "UPDATE compte SET statut=? WHERE idcompte=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // CORRECTION: utilise connection, pas ConnexionBD

            ps.setBoolean(1, statut);
            ps.setInt(2, idCompte);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Créer une permission
    public boolean creerPermission(String nom, String description, String avantage, String interdiction) {
        String sql = "INSERT INTO permission (nom, description, avantage, interdiction) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // CORRECTION: utilise connection, pas ConnexionBD

            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setString(3, avantage);
            ps.setString(4, interdiction);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("AdminDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("AdminDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}
