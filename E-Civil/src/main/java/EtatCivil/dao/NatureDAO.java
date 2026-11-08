package EtatCivil.dao;

import EtatCivil.utils.DatabaseListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NatureDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public NatureDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'obtention de la connexion pour NatureDAO", e);
        }
    }

    private void checkConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            throw new SQLException("La connexion à la base de données n'est pas disponible");
        }
    }
 // Récupérer toutes les natures avec leurs variétés COMPLÈTES (avec les booléens)
    public List<Object[]> getAllNaturesWithVariete() throws SQLException {
        List<Object[]> natures = new ArrayList<>();
        
        String sql = "SELECT n.idnature, n.nom, n.description, n.tarif_base, " +
                    "n.duree_validite, n.delai_traitement, v.nom as variete_nom, " +
                    "v.temoins, v.parents, v.conjoint, v.medecin, v.mentionmarginale, v.causedeces, " +
                    "v.description as variete_description, v.mots_cles " +
                    "FROM nature n " +
                    "LEFT JOIN varietes v ON n.idvariete = v.idvariete " +
                    "WHERE n.tarif_base > 0 " +  // Seulement les natures avec tarif (disponibles)
                    "ORDER BY v.nom, n.nom";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] nature = new Object[14]; // Augmenté à 14 pour inclure tous les booléens
                nature[0] = rs.getInt("idnature");           // ID nature
                nature[1] = rs.getString("nom");             // Nom du document
                nature[2] = rs.getString("description");     // Description
                nature[3] = rs.getDouble("tarif_base");      // Tarif
                nature[4] = rs.getInt("duree_validite");     // Validité
                nature[5] = rs.getInt("delai_traitement");   // Délai
                nature[6] = rs.getString("variete_nom");     // Catégorie (nom de la variété)
                
                // Ajout des booléens
                nature[7] = rs.getBoolean("temoins");
                nature[8] = rs.getBoolean("parents");
                nature[9] = rs.getBoolean("conjoint");
                nature[10] = rs.getBoolean("medecin");
                nature[11] = rs.getBoolean("mentionmarginale");
                nature[12] = rs.getBoolean("causedeces");
                
                // Informations supplémentaires de la variété
                nature[13] = rs.getString("variete_description");
                // nature[14] pourrait être mots_cles si besoin
                
                natures.add(nature);
            }
        }
        return natures;
    }
 // Récupérer une nature spécifique avec tous les détails de sa variété
    public Object[] getNatureByIdWithVariete(int natureId) throws SQLException {
        Object[] nature = null;
        
        String sql = "SELECT n.idnature, n.nom, n.description, n.tarif_base, " +
                    "n.duree_validite, n.delai_traitement, v.nom as variete_nom, " +
                    "v.temoins, v.parents, v.conjoint, v.medecin, v.mentionmarginale, v.causedeces, " +
                    "v.description as variete_description, v.mots_cles, v.idvariete " +
                    "FROM nature n " +
                    "LEFT JOIN varietes v ON n.idvariete = v.idvariete " +
                    "WHERE n.idnature = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, natureId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                nature = new Object[16];
                nature[0] = rs.getInt("idnature");           // ID nature
                nature[1] = rs.getString("nom");             // Nom du document
                nature[2] = rs.getString("description");     // Description
                nature[3] = rs.getDouble("tarif_base");      // Tarif
                nature[4] = rs.getInt("duree_validite");     // Validité
                nature[5] = rs.getInt("delai_traitement");   // Délai
                nature[6] = rs.getString("variete_nom");     // Catégorie (nom de la variété)
                
                // Booléens
                nature[7] = rs.getBoolean("temoins");
                nature[8] = rs.getBoolean("parents");
                nature[9] = rs.getBoolean("conjoint");
                nature[10] = rs.getBoolean("medecin");
                nature[11] = rs.getBoolean("mentionmarginale");
                nature[12] = rs.getBoolean("causedeces");
                
                // Informations supplémentaires
                nature[13] = rs.getString("variete_description");
                nature[14] = rs.getString("mots_cles");
                nature[15] = rs.getInt("idvariete");         // ID de la variété
                
            }
        }
        return nature;
    }

    // Récupérer une nature par son ID
    public Object[] getNatureById(int idNature) throws SQLException {
        String sql = "SELECT n.idnature, n.nom, n.description, n.tarif_base, " +
                    "n.duree_validite, n.delai_traitement, v.nom as variete_nom " +
                    "FROM nature n " +
                    "LEFT JOIN varietes v ON n.idvariete = v.idvariete " +
                    "WHERE n.idnature = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNature);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object[] nature = new Object[7];
                    nature[0] = rs.getInt("idnature");
                    nature[1] = rs.getString("nom");
                    nature[2] = rs.getString("description");
                    nature[3] = rs.getDouble("tarif_base");
                    nature[4] = rs.getInt("duree_validite");
                    nature[5] = rs.getInt("delai_traitement");
                    nature[6] = rs.getString("variete_nom");
                    return nature;
                }
            }
        }
        return null;
    }
    /**
     * Récupère les natures de documents - VERSION ADAPTÉE AU SCHÉMA RÉEL
     */
    public List<Object[]> getDocumentsAvecTarifs() throws SQLException {
        checkConnection();

        List<Object[]> documents = new ArrayList<>();
        
        // REQUÊTE ADAPTÉE - UTILISEZ LES COLONNES RÉELLES DE VOTRE TABLE
        String sql = "SELECT idnature, nom, description, duree_validite,tarif_base,delai_traitement FROM nature ORDER BY idnature";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // ADAPTEZ LE NOMBRE DE COLONNES À VOTRE SCHÉMA RÉEL
                Object[] document = new Object[6]; // Seulement 3 colonnes pour l'exemple
                document[0] = rs.getInt("idnature");
                document[1] = rs.getString("nom");
                document[2] = rs.getString("description");
                document[3] = rs.getString("duree_validite");
                document[4] = rs.getString("tarif_base");
                document[5] = rs.getString("delai_traitement");


                documents.add(document);
            }
        }
        return documents;
    }

    /**
     * Version alternative si vous voulez récupérer depuis documentetatcivil
     */
    public List<Object[]> getDocumentsFromDocumentEtatCivil() throws SQLException {
        checkConnection();

        List<Object[]> documents = new ArrayList<>();
        
        // Si vous voulez les documents depuis documentetatcivil
        String sql = "SELECT iddocument, numerodocument, dateevenement, lieuevenement, dateenregistrement, idnature, statut " +
                     "FROM documentetatcivil WHERE statut = 'actif' ORDER BY dateenregistrement DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Object[] document = new Object[7];
                document[0] = rs.getInt("iddocument");
                document[1] = rs.getInt("numerodocument");
                document[2] = rs.getDate("dateevenement");
                document[3] = rs.getString("lieuevenement");
                document[4] = rs.getDate("dateenregistrement");
                document[5] = rs.getInt("idnature");
                document[6] = rs.getString("statut");
                
                documents.add(document);
            }
        }
        return documents;
    }

    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("NatureDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("NatureDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}