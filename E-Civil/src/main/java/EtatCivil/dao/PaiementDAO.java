package EtatCivil.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import EtatCivil.modeles.Paiement;
import EtatCivil.utils.DatabaseListener;

public class PaiementDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public PaiementDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }

    public PaiementDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion PaiementDAO", e);
        }
    }

    // Créer un nouveau paiement et retourner l'ID
    public int creerPaiement(Paiement paiement) throws SQLException {
        String sql = "INSERT INTO paiement (datepaiement, montant, status, recu, modepaiement, reference) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setTimestamp(1, new Timestamp(paiement.getDatePaiement().getTime()));
            statement.setDouble(2, paiement.getMontant());
            statement.setString(3, paiement.getStatus());
            statement.setString(4, paiement.getRecu());
            statement.setString(5, paiement.getModePaiement());
            statement.setString(6, paiement.getReference()); // Nouveau champ
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int idPaiement = generatedKeys.getInt(1);
                        paiement.setIdPaiement(idPaiement);
                        System.out.println("✅ Paiement créé avec ID: " + idPaiement);
                        return idPaiement;
                    }
                }
            }
            return 0;
        }
    }

    // Récupérer un paiement par ID
    public Paiement getPaiementById(int idPaiement) throws SQLException {
        String sql = "SELECT * FROM paiement WHERE idpaiement = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPaiement);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToPaiement(resultSet);
                }
            }
        }
        return null;
    }

    // Mettre à jour le statut d'un paiement
    public boolean updateStatutPaiement(int idPaiement, String nouveauStatut) throws SQLException {
        String sql = "UPDATE paiement SET status = ? WHERE idpaiement = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nouveauStatut);
            statement.setInt(2, idPaiement);
            
            boolean success = statement.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Statut paiement mis à jour - ID: " + idPaiement + ", Statut: " + nouveauStatut);
            }
            return success;
        }
    }

    // Mapper ResultSet vers objet Paiement
    private Paiement mapResultSetToPaiement(ResultSet resultSet) throws SQLException {
        Paiement paiement = new Paiement();
        paiement.setIdPaiement(resultSet.getInt("idpaiement"));
        paiement.setDatePaiement(resultSet.getTimestamp("datepaiement"));
        paiement.setMontant(resultSet.getDouble("montant"));
        paiement.setStatus(resultSet.getString("status"));
        paiement.setRecu(resultSet.getString("recu"));
        paiement.setModePaiement(resultSet.getString("modepaiement"));
        paiement.setReference(resultSet.getString("reference")); // Nouveau champ
        return paiement;
    }

    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("PaiementDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}