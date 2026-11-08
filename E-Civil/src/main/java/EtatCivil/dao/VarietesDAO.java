package EtatCivil.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import EtatCivil.utils.DatabaseListener;

public class VarietesDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public VarietesDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion VarieteDAO", e);
        }
    }

    // Récupérer toutes les variétés (types de documents)
    public List<Object[]> getAllVarietes() throws SQLException {
        List<Object[]> varietes = new ArrayList<>();
        String sql = "SELECT idvariete, nom, description, mots_cles FROM varietes ORDER BY nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] variete = new Object[4];
                variete[0] = rs.getInt("idvariete");
                variete[1] = rs.getString("nom");
                variete[2] = rs.getString("description");
                variete[3] = rs.getString("mots_cles");
                
                varietes.add(variete);
            }
        }
        return varietes;
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("VarieteDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }

}