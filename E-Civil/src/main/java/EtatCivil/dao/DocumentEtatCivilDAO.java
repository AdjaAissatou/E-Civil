package EtatCivil.dao;

import EtatCivil.modeles.DocumentEtatCivil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import EtatCivil.utils.*;
/**
 * DAO pour la table documentetatcivil
 */
public class DocumentEtatCivilDAO {

	   private Connection connection;
	    private boolean shouldCloseConnection = false;

	    // Constructeur avec connexion existante (gérée ailleurs)
	    public DocumentEtatCivilDAO(Connection connection) {
	        this.connection = connection;
	        this.shouldCloseConnection = false; // On ne ferme pas une connexion qu'on n'a pas créée
	    }

	    // Constructeur par défaut qui crée sa propre connexion
	    public DocumentEtatCivilDAO() {
	        try {
	            this.connection = DatabaseListener.getConnection();
	            this.shouldCloseConnection = true; // On ferme cette connexion
	        } catch (SQLException e) {
	            throw new RuntimeException("Erreur lors de l'obtention de la connexion", e);
	        }
	    }




    /** Mappe une ligne ResultSet vers un DocumentEtatCivil */
    private DocumentEtatCivil mapRow(ResultSet rs) throws SQLException {
        DocumentEtatCivil doc = new DocumentEtatCivil();
        doc.setIddocument(rs.getInt("iddocument"));
        doc.setIdcommune(rs.getInt("idcommune"));
        doc.setNumeroregistre(rs.getInt("numeroregistre"));
        doc.setNumerodocument(rs.getInt("numerodocument"));
        doc.setDateevenement(rs.getDate("dateevenement"));
        doc.setLieuevenement(rs.getString("lieuevenement"));
        doc.setDateenregistrement(rs.getDate("dateenregistrement"));
        doc.setIdnature(rs.getInt("idnature"));
        doc.setStatut(rs.getString("statut"));
        doc.setDateCreation(rs.getTimestamp("date_creation"));
        doc.setDateModification(rs.getTimestamp("date_modification"));
        doc.setCreatedBy(rs.getString("created_by"));
        return doc;
    }

    /**
     * Récupère tous les documents (ordre : dateenregistrement DESC)
     */
    public List<DocumentEtatCivil> getAllDocuments() throws SQLException {
        List<DocumentEtatCivil> documents = new ArrayList<>();
        String sql = "SELECT * FROM documentetatcivil ORDER BY dateenregistrement DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                documents.add(mapRow(rs));
            }
        }
        return documents;
    }

    /**
     * Récupère un document par son iddocument (PK)
     */
    public DocumentEtatCivil getDocumentById(int iddocument) throws SQLException {
        String sql = "SELECT * FROM documentetatcivil WHERE iddocument = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, iddocument);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }
    /**
     * Méthode utilitaire pour le tableau - utilise les colonnes RÉELLES
     */
    public Object[] getDocumentAsArray(int idDocument) throws SQLException {
        DocumentEtatCivil doc = getDocumentById(idDocument);
        if (doc != null) {
            Object[] docArray = new Object[7];
            docArray[0] = doc.getIddocument();           // id
            docArray[1] = doc.getNumerodocument();       // numéro document
            docArray[2] = doc.getDateevenement();        // date événement
            docArray[3] = doc.getLieuevenement();        // lieu événement
            docArray[4] = doc.getDateenregistrement();   // date enregistrement
            docArray[5] = doc.getIdnature();             // id nature
            docArray[6] = doc.getStatut();               // statut
            return docArray;
        }
        return null;
    }
    
    /**
     * Récupère tous les documents sous forme de tableau
     */
    public List<Object[]> getAllDocumentsAsArray() throws SQLException {
        List<Object[]> documents = new ArrayList<>();
        String sql = "SELECT * FROM documentetatcivil WHERE statut = 'actif' ORDER BY dateenregistrement DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] doc = new Object[7];
                doc[0] = rs.getInt("iddocument");
                doc[1] = rs.getInt("numerodocument");
                doc[2] = rs.getDate("dateevenement");
                doc[3] = rs.getString("lieuevenement");
                doc[4] = rs.getDate("dateenregistrement");
                doc[5] = rs.getInt("idnature");
                doc[6] = rs.getString("statut");
                
                documents.add(doc);
            }
        }
        return documents;
    }
    /**
     * Récupère un document par son numerodocument
     */
    public DocumentEtatCivil getDocumentByNumero(int numerodocument) throws SQLException {
        String sql = "SELECT * FROM documentetatcivil WHERE numerodocument = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, numerodocument);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
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
                System.err.println("Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
    /**
     * Ajoute un document.
     * Remarque: utilise RETURNING iddocument pour récupérer la PK si la colonne est générée.
     * Retourne true si insertion OK (et met iddocument sur l'objet si retourné).
     */
    public boolean ajouterDocument(DocumentEtatCivil document) throws SQLException {
        String sql = "INSERT INTO documentetatcivil " +
                "(idcommune, numeroregistre, numerodocument, dateevenement, lieuevenement, dateenregistrement, idnature, statut, date_creation, date_modification, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING iddocument";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // idcommune
            if (document.getIdcommune() > 0) ps.setInt(1, document.getIdcommune());
            else ps.setNull(1, Types.INTEGER);

            ps.setInt(2, document.getNumeroregistre());
            ps.setInt(3, document.getNumerodocument());

            if (document.getDateevenement() != null) ps.setDate(4, document.getDateevenement());
            else ps.setNull(4, Types.DATE);

            ps.setString(5, document.getLieuevenement());

            if (document.getDateenregistrement() != null) ps.setDate(6, document.getDateenregistrement());
            else ps.setNull(6, Types.DATE);

            if (document.getIdnature() > 0) ps.setInt(7, document.getIdnature());
            else ps.setNull(7, Types.INTEGER);

            ps.setString(8, document.getStatut());

            // date_creation et date_modification : timestamps
            if (document.getDateCreation() != null) ps.setTimestamp(9, document.getDateCreation());
            else ps.setTimestamp(9, new Timestamp(System.currentTimeMillis())); // default now

            if (document.getDateModification() != null) ps.setTimestamp(10, document.getDateModification());
            else ps.setNull(10, Types.TIMESTAMP);

            ps.setString(11, document.getCreatedBy());

            // Execute and read returned iddocument
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    document.setIddocument(generatedId);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Met à jour un document.
     * Si iddocument est renseigné (>0) on met à jour par PK, sinon on tente par numerodocument.
     */
    public boolean mettreAJourDocument(DocumentEtatCivil document) throws SQLException {
        String sqlById = "UPDATE documentetatcivil SET idcommune = ?, numeroregistre = ?, numerodocument = ?, dateevenement = ?, lieuevenement = ?, dateenregistrement = ?, idnature = ?, statut = ?, date_modification = ?, created_by = ? WHERE iddocument = ?";
        String sqlByNumero = "UPDATE documentetatcivil SET idcommune = ?, numeroregistre = ?, dateevenement = ?, lieuevenement = ?, dateenregistrement = ?, idnature = ?, statut = ?, date_modification = ?, created_by = ? WHERE numerodocument = ?";

        boolean byId = document.getIddocument() > 0;
        try (PreparedStatement ps = connection.prepareStatement(byId ? sqlById : sqlByNumero)) {
            // idcommune
            if (document.getIdcommune() > 0) ps.setInt(1, document.getIdcommune());
            else ps.setNull(1, Types.INTEGER);

            ps.setInt(2, document.getNumeroregistre());

            if (byId) {
                ps.setInt(3, document.getNumerodocument());
            } else {
                // when updating by numerodocument we don't overwrite numerodocument in SET
                if (document.getDateevenement() != null) ps.setDate(3, document.getDateevenement());
                else ps.setNull(3, Types.DATE);
            }

            if (!byId) {
                // index shift for sqlByNumero
                ps.setString(4, document.getLieuevenement());
                if (document.getDateenregistrement() != null) ps.setDate(5, document.getDateenregistrement());
                else ps.setNull(5, Types.DATE);

                if (document.getIdnature() > 0) ps.setInt(6, document.getIdnature());
                else ps.setNull(6, Types.INTEGER);

                ps.setString(7, document.getStatut());
                if (document.getDateModification() != null) ps.setTimestamp(8, document.getDateModification());
                else ps.setNull(8, Types.TIMESTAMP);
                ps.setString(9, document.getCreatedBy());

                // WHERE numerodocument = ?
                ps.setInt(10, document.getNumerodocument());
            } else {
                // continue filling parameters for sqlById after we've set param 3 (numerodocument)
                if (document.getDateevenement() != null) ps.setDate(4, document.getDateevenement());
                else ps.setNull(4, Types.DATE);

                ps.setString(5, document.getLieuevenement());

                if (document.getDateenregistrement() != null) ps.setDate(6, document.getDateenregistrement());
                else ps.setNull(6, Types.DATE);

                if (document.getIdnature() > 0) ps.setInt(7, document.getIdnature());
                else ps.setNull(7, Types.INTEGER);

                ps.setString(8, document.getStatut());

                if (document.getDateModification() != null) ps.setTimestamp(9, document.getDateModification());
                else ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

                ps.setString(10, document.getCreatedBy());

                // WHERE iddocument = ?
                ps.setInt(11, document.getIddocument());
            }
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un document par iddocument (PK)
     */
    public boolean supprimerDocumentById(int iddocument) throws SQLException {
        String sql = "DELETE FROM documentetatcivil WHERE iddocument = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, iddocument);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Supprime un document par numerodocument
     */
    public boolean supprimerDocumentByNumero(int numerodocument) throws SQLException {
        String sql = "DELETE FROM documentetatcivil WHERE numerodocument = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, numerodocument);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Nombre total de documents
     */
    public int getNombreDocuments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM documentetatcivil";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // --- Méthodes utilitaires supplémentaires possibles ---
    // public List<DocumentEtatCivil> getDocumentsByCommune(int idCommune) { ... }
    // public List<DocumentEtatCivil> getDocumentsByNature(int idNature) { ... }
    // public int ajouterDocumentRetourId(DocumentEtatCivil doc) { ... }  // retourne id généré

}
