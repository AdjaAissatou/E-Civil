package EtatCivil.dao;

import EtatCivil.utils.DatabaseListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MotifDemandeDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public MotifDemandeDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion MotifDemandeDAO", e);
        }
    }

    public MotifDemandeDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }

    /**
     * Récupère tous les motifs pour une nature donnée
     */
    public List<Object[]> getMotifsByNature(int idNature) throws SQLException {
        List<Object[]> motifs = new ArrayList<>();
        String sql = "SELECT idmotif, libelle, description, documents_requis FROM motif_demande WHERE idnature = ? ORDER BY libelle";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNature);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] motif = new Object[4];
                    motif[0] = rs.getInt("idmotif");
                    motif[1] = rs.getString("libelle");
                    motif[2] = rs.getString("description");
                    motif[3] = rs.getString("documents_requis");
                    motifs.add(motif);
                }
            }
        }
        return motifs;
    }
    public int creerMotifPersonnalise(int natureId, String libelleMotif) throws SQLException {
        String sql = "INSERT INTO motif_demande (idnature, libelle, description, documents_requis) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, natureId);
            stmt.setString(2, libelleMotif);
            stmt.setString(3, "Motif personnalisé : " + libelleMotif);
            stmt.setString(4, "À préciser selon la demande");
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        }
    }
    /**
     * Récupère un motif spécifique par son ID
     */
    public Object[] getMotifById(int idMotif) throws SQLException {
        String sql = "SELECT idmotif, libelle, description, documents_requis FROM motif_demande WHERE idmotif = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMotif);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object[] motif = new Object[4];
                    motif[0] = rs.getInt("idmotif");
                    motif[1] = rs.getString("libelle");
                    motif[2] = rs.getString("description");
                    motif[3] = rs.getString("documents_requis");
                    return motif;
                }
            }
        }
        return null;
    }


    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("MotifDemandeDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}