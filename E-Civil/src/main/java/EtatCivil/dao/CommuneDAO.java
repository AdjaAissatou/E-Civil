package EtatCivil.dao;

import EtatCivil.modeles.Commune;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommuneDAO {
    private Connection connection;

    public CommuneDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Commune> getAllCommunes() throws SQLException {
        List<Commune> communes = new ArrayList<>();
        String sql = "SELECT * FROM communes ORDER BY nom";
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Commune commune = new Commune();
                commune.setIdCommune(resultSet.getInt("idCommune"));
                commune.setNom(resultSet.getString("nom"));
                commune.setVille(resultSet.getString("ville"));
                communes.add(commune);
            }
        }
        return communes;
    }

    public Commune getCommuneById(int id) throws SQLException {
        String sql = "SELECT * FROM communes WHERE idCommune = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Commune commune = new Commune();
                    commune.setIdCommune(resultSet.getInt("idCommune"));
                    commune.setNom(resultSet.getString("nom"));
                    commune.setVille(resultSet.getString("ville"));
                    return commune;
                }
            }
        }
        return null;
    }
}