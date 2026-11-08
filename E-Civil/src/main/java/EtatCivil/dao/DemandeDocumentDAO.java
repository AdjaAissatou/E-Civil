package EtatCivil.dao;

import EtatCivil.modeles.DemandeDocument;
import EtatCivil.modeles.Nature;
import EtatCivil.utils.DatabaseListener;
import jakarta.servlet.http.HttpServletRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemandeDocumentDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;
    
    
    public DemandeDocumentDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }

    public DemandeDocumentDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion DemandeDocumentDAO", e);
        }
    }
//    // Lier un paiement à une demande de document
//    public boolean lierPaiementADemande(int iddemande, int idpaiement) throws SQLException {
//        String sql = "UPDATE demandedocument SET idpaiement = ?, status = 'payé' WHERE iddemande = ?";
//        
//        try (PreparedStatement statement = connection.prepareStatement(sql)) {
//            statement.setInt(1, idpaiement);
//            statement.setInt(2, iddemande);
//            
//            boolean success = statement.executeUpdate() > 0;
//            if (success) {
//                System.out.println("✅ Paiement lié à la demande - ID Demande: " + iddemande + ", ID Paiement: " + idpaiement);
//            }
//            return success;
//        }
//    }
    public DemandeDocument getDemandeById(int idDemande) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DemandeDocument demande = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT dd.*, n.nom as nature_document FROM demandedocument dd " +
                        "LEFT JOIN nature n ON dd.idnature = n.idnature " +
                        "WHERE dd.iddemande = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idDemande);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                demande = mapResultSetToDemandeDocument(rs);
                demande.setNatureDocument(rs.getString("nature_document"));
                
                // CORRECTION: Gérer correctement les valeurs NULL pour idutilisateur
                int idUtilisateur = rs.getInt("idutilisateur");
                if (rs.wasNull()) {
                    demande.setIdutilisateur(0); // Ou une valeur par défaut
                    System.out.println("⚠️ idutilisateur est NULL dans la base, défini à 0");
                } else {
                    demande.setIdutilisateur(idUtilisateur);
                    System.out.println("✅ idutilisateur récupéré: " + idUtilisateur);
                }
                
                // Récupérer l'ID personne
                int idPersonne = rs.getInt("id");
                demande.setId(idPersonne);
                System.out.println("✅ id (personne) récupéré: " + idPersonne);
                
            } else {
                System.out.println("❌ Aucune donnée trouvée pour demande " + idDemande);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getDemandeById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return demande;
    }
    public List<Map<String, Object>> getHistoriqueStatut(int idDemande) throws SQLException {
        List<Map<String, Object>> historique = new ArrayList<>();
        
        String sql = "SELECT statut, date_modification, commentaire " +
                    "FROM historiquestatut " +
                    "WHERE iddemande = ? " +
                    "ORDER BY date_modification DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> etape = new HashMap<>();
                    etape.put("statut", rs.getString("statut"));
                    etape.put("date", rs.getTimestamp("date_modification"));
                    etape.put("commentaire", rs.getString("commentaire"));
                    historique.add(etape);
                }
            }
        }
        return historique;
    }
 // Méthode pour ajouter une entrée dans l'historique des statuts
    public boolean ajouterHistoriqueStatut(int idDemande, String statut, String commentaire) throws SQLException {
        String sql = "INSERT INTO historiquestatut (iddemande, statut, commentaire) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.setString(2, statut);
            stmt.setString(3, commentaire);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
 // Mettez à jour votre méthode existante pour inclure le paiement
 // Dans DemandeDocumentDAO.java - Modifiez la méthode getDemandesByUtilisateur
    public List<DemandeDocument> getDemandesByUtilisateur(int idPersonne) throws SQLException {
        List<DemandeDocument> demandes = new ArrayList<>();
        
        String sql = "SELECT dd.*, n.idnature, n.nom as nom_document, n.description, n.tarif_base, " +
                    "n.duree_validite, n.delai_traitement, n.idvariete, " +
                    "p.prenom, p.nom, p.sexe, p.datedenaissance, p.lieudenaissance, " +
                    "p.profession, p.residance, " +
                    " u.numerotelephone, " +
                    "pa.idpaiement, pa.status as status_paiement " +
                    "FROM demandedocument dd " +
                    "JOIN nature n ON dd.idnature = n.idnature " +
                    "JOIN personne p ON dd.id = p.id " +
                    "JOIN utilisateur u ON dd.idutilisateur = u.idutilisateur " + // Garder mais pas utilisé en WHERE
                    "LEFT JOIN paiement pa ON dd.idpaiement = pa.idpaiement " +
                    "WHERE dd.id = ? " + // CHANGEMENT ICI : utiliser id au lieu de idutilisateur
                    "ORDER BY dd.date DESC, dd.iddemande DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersonne); // Maintenant c'est l'ID personne
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DemandeDocument demande = extraireDemandeFromResultSet(rs);
                    
                    // Vérifier si le paiement est requis
                    boolean paiementRequis = isPaiementRequis(demande.getIdDemande());
                    demande.setPaiementRequis(paiementRequis);
                    
                    demandes.add(demande);
                }
            }
        }
        return demandes;
    }
    // Méthode utilitaire pour extraire une demande
 // Dans DemandeDocumentDAO.java - méthode extraireDemandeFromResultSet
    private DemandeDocument extraireDemandeFromResultSet(ResultSet rs) throws SQLException {
        DemandeDocument demande = new DemandeDocument();
        
        // Informations de base de la demande
        demande.setIdDemande(rs.getInt("iddemande"));
        demande.setDate(rs.getDate("date"));
        demande.setFormat(rs.getString("format"));
        demande.setQrCode(rs.getString("qrcode"));
        demande.setStatus(rs.getString("status"));
        demande.setIdutilisateur(rs.getInt("idutilisateur"));
        
        // Gestion de idpaiement (peut être NULL)
        Integer idPaiement = (Integer) rs.getObject("idpaiement");
        demande.setIdpaiement(idPaiement);
        
        // Objet Nature
        Nature nature = new Nature();
        nature.setIdnature(rs.getInt("idnature"));
        nature.setNom(rs.getString("nom_document"));
        nature.setDescription(rs.getString("description"));
        nature.setTarif_base(rs.getDouble("tarif_base"));
        nature.setDuree_validite(rs.getInt("duree_validite"));
        nature.setDelaiTraitement(rs.getInt("delai_traitement"));
        nature.setIdVariete(rs.getInt("idvariete"));
        
        demande.setNature(nature);
        demande.setNomDocument(nature.getNom());
        
        // Informations personnelles
        demande.setPrenom(rs.getString("prenom"));
        demande.setNom(rs.getString("nom"));
        demande.setSexe(rs.getString("sexe"));
        demande.setDatedenaissance(rs.getDate("datedenaissance"));
        demande.setLieudenaissance(rs.getString("lieudenaissance"));
        demande.setProfession(rs.getString("profession"));
        demande.setResidance(rs.getString("residance"));
        
        return demande;
    }

 // Dans DemandeDocumentDAO.java
    public Map<String, Integer> getStatistiquesDemandes(int idPersonne) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM demandedocument WHERE id = ? GROUP BY status";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPersonne);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("status"), rs.getInt("count"));
                }
            }
        }
        return stats;
    }
//    public List<DemandeDocument> getDemandesByUtilisateur(int idUtilisateur) throws SQLException {
//        List<DemandeDocument> demandes = new ArrayList<>();
//        
//        String sql = "SELECT dd.*, n.idnature, n.nom as nom_document, n.description, n.tarif_base, " +
//                    "n.duree_validite, n.delai_traitement, n.idvariete, " +
//                    "p.prenom, p.nom, p.sexe, p.datedenaissance, p.lieudenaissance, " +
//                    "p.profession, p.residance " +
//                    "FROM demandedocument dd " +
//                    "JOIN nature n ON dd.idnature = n.idnature " + // CORRECTION: iddocument → idnature
//                    "JOIN personne p ON dd.id = p.id " +
//                    "WHERE dd.idutilisateur = ? " +
//                    "ORDER BY dd.date DESC";
//        
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, idUtilisateur);
//            
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    DemandeDocument demande = new DemandeDocument();
//                    
//                    demande.setIdDemande(rs.getInt("iddemande"));
//                    demande.setDate(rs.getDate("date"));
//                    demande.setFormat(rs.getString("format"));
//                    demande.setQrCode(rs.getString("qrcode"));
//                    demande.setStatus(rs.getString("status"));
//                    
//                    
//                    // CORRECTION : Créer et remplir l'objet Nature
//                    Nature nature = new Nature();
//                    nature.setIdnature(rs.getInt("idnature"));
//                    nature.setNom(rs.getString("nom_document"));
//                    nature.setDescription(rs.getString("description"));
//                    nature.setTarif_base(rs.getDouble("tarif_base"));
//                    nature.setDuree_validite(rs.getInt("duree_validite"));
//                    nature.setDelaiTraitement(rs.getInt("delai_traitement"));
//                    nature.setIdVariete(rs.getInt("idvariete"));
//                    
//                    demande.setNature(nature); // Associer la nature à la demande
//                    demande.setNomDocument(nature.getNom()); // Pour compatibilité
//                    
//                    // Informations personnelles (héritées de Personne)
//                    demande.setPrenom(rs.getString("prenom"));
//                    demande.setNom(rs.getString("nom"));
//                    demande.setSexe(rs.getString("sexe"));
//                    demande.setDatedenaissance(rs.getDate("datedenaissance"));
//                    demande.setLieudenaissance(rs.getString("lieudenaissance"));
//                    demande.setProfession(rs.getString("profession"));
//                    demande.setResidance(rs.getString("residence"));
//                    
//                    demandes.add(demande);
//                }
//            }
//        }
//        return demandes;
//    }
    public Map<String, String> getVarietesByDemandeId(int idDemande) {
        Map<String, String> varietes = new HashMap<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT type_variete, donnees FROM demande_variete_details WHERE id_demande = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idDemande);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                String typeVariete = rs.getString("type_variete");
                String donnees = rs.getString("donnees");
                
                // Parser les données (format: key1=value1&key2=value2)
                if (donnees != null) {
                    String[] paires = donnees.split("&");
                    for (String paire : paires) {
                        String[] keyValue = paire.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = keyValue[1];
                            if (!value.isEmpty()) {
                                varietes.put(key, value);
                            }
                        }
                    }
                }
            }
            
            System.out.println("✅ Variétés récupérées pour demande " + idDemande + ": " + varietes.size());
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getVarietesByDemandeId: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return varietes;
    }

 // Méthode pour vérifier si le paiement est requis
    public boolean isPaiementRequis(int idDemande) throws SQLException {
        String sql = "SELECT dd.status, n.tarif_base, dd.idpaiement " +
                    "FROM demandedocument dd " +
                    "JOIN nature n ON dd.idnature = n.idnature " +
                    "WHERE dd.iddemande = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String statusDemande = rs.getString("status");
                    double tarif = rs.getDouble("tarif_base");
                    Integer idPaiement = (Integer) rs.getObject("idpaiement");
                    
                    // Paiement requis si: statut est "validée" ET tarif > 0 ET pas de paiement associé
                    return statusDemande != null
                            && statusDemande.trim().equalsIgnoreCase("en cours")
                            && tarif > 0
                            && idPaiement == null;
                }


                
            }
        }
        return false;
        
    }

    // Méthode pour récupérer les informations de paiement
    public Map<String, Object> getInfosPaiement(int idDemande) throws SQLException {
        Map<String, Object> infosPaiement = new HashMap<>();
        
        String sql = "SELECT n.tarif_base, dd.status, dd.idpaiement " +
                    "FROM demandedocument dd " +
                    "JOIN nature n ON dd.idnature = n.idnature " +
                    "WHERE dd.iddemande = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    infosPaiement.put("tarifBase", rs.getDouble("tarif_base"));
                    infosPaiement.put("statusDemande", rs.getString("status"));
                    infosPaiement.put("idPaiement", rs.getObject("idpaiement"));
                }
            }
        }
        return infosPaiement;
    }

    public boolean verifierProprieteDemande(int idDemande, int idUtilisateur) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demandedocument WHERE iddemande = ? AND idutilisateur = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idDemande);
            stmt.setInt(2, idUtilisateur);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
 // Méthode pour lier un paiement à une demande
 public boolean lierPaiementADemande(int idDemande, int idPaiement) throws SQLException {
     String sql = "UPDATE demandedocument SET idpaiement = ? WHERE iddemande = ?";
     
     try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setInt(1, idPaiement);
         stmt.setInt(2, idDemande);
         
         int rowsAffected = stmt.executeUpdate();
         if (rowsAffected > 0) {
             System.out.println("✅ Paiement lié à la demande - ID Demande: " + idDemande + ", ID Paiement: " + idPaiement);
             return true;
         }
     }
     return false;
 }

 // Méthode pour créer un nouveau paiement
 public int creerPaiement(double montant, String modePaiement) throws SQLException {
     String sql = "INSERT INTO paiement (datepaiement, montant, status, modepaiement) VALUES (NOW(), ?, 'en attente', ?)";
     
     try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         stmt.setDouble(1, montant);
         stmt.setString(2, modePaiement);
         
         int affectedRows = stmt.executeUpdate();
         if (affectedRows == 0) {
             throw new SQLException("Échec de la création du paiement");
         }
         
         try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
             if (generatedKeys.next()) {
                 int idPaiement = generatedKeys.getInt(1);
                 System.out.println("✅ Paiement créé - ID: " + idPaiement + ", Montant: " + montant);
                 return idPaiement;
             } else {
                 throw new SQLException("Échec de récupération de l'ID du paiement");
             }
         }
     }
 }

 // Méthode pour mettre à jour le statut d'un paiement
 public boolean mettreAJourStatutPaiement(int idPaiement, String nouveauStatut, String recu) throws SQLException {
     String sql = "UPDATE paiement SET status = ?, recu = ? WHERE idpaiement = ?";
     
     try (PreparedStatement stmt = connection.prepareStatement(sql)) {
         stmt.setString(1, nouveauStatut);
         stmt.setString(2, recu);
         stmt.setInt(3, idPaiement);
         
         int rowsAffected = stmt.executeUpdate();
         return rowsAffected > 0;
     }
 }
    public int creerDemandeAvecVarietes(DemandeDocument demande, int idPersonne, 
            int natureId, int motifId, 
            HttpServletRequest request, 
            Object[] nature) {
        
        Connection connection = null;
        PreparedStatement stmtDemande = null;
        PreparedStatement stmtVariete = null;
        
        try {
            connection = DatabaseListener.getConnection();
            connection.setAutoCommit(false);
            
            String sqlDemande = "INSERT INTO demandedocument (date, format, status, id, idnature, idmotif, " +
            "prenom, nom, datedenaissance, lieudenaissance, sexe, profession, residance, " +
            "adresse_livraison, ville_livraison, destinataire) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            stmtDemande = connection.prepareStatement(sqlDemande, Statement.RETURN_GENERATED_KEYS);
            
            stmtDemande.setDate(1, new java.sql.Date(demande.getDate().getTime()));
            stmtDemande.setString(2, demande.getFormat());
            stmtDemande.setString(3, demande.getStatus());
            stmtDemande.setInt(4, idPersonne);
            stmtDemande.setInt(5, natureId);
            stmtDemande.setInt(6, motifId);
            stmtDemande.setString(7, demande.getPrenom());
            stmtDemande.setString(8, demande.getNom());
            stmtDemande.setDate(9, new java.sql.Date(demande.getDatedenaissance().getTime()));
            stmtDemande.setString(10, demande.getLieudenaissance());
            stmtDemande.setString(11, demande.getSexe());
            stmtDemande.setString(12, demande.getProfession());
            stmtDemande.setString(13, demande.getResidance());
            stmtDemande.setString(14, demande.getAdresseLivraison());
            stmtDemande.setString(15, demande.getVilleLivraison());
            stmtDemande.setString(16, demande.getDestinataire());
            
            int affectedRows = stmtDemande.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
                return 0; // Retourner 0 au lieu de false
            }
            
            // 2. Récupérer l'ID de la demande créée
            int demandeId;
            try (ResultSet generatedKeys = stmtDemande.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    demandeId = generatedKeys.getInt(1);
                } else {
                    connection.rollback();
                    return 0; // Retourner 0 au lieu de false
                }
            }
            
            // 3. Insérer les données des variétés
            String sqlVariete = "INSERT INTO demande_variete_details (id_demande, type_variete, donnees) VALUES (?, ?, ?)";
            stmtVariete = connection.prepareStatement(sqlVariete);
            
            // Récupérer les booléens de nature
            boolean temoins = (boolean) nature[7];
            boolean parents = (boolean) nature[8];
            boolean conjoints = (boolean) nature[9];
            boolean medecin = (boolean) nature[10];
            boolean mentionMarginale = (boolean) nature[11];
            boolean causeDeces = (boolean) nature[12];
            
            // Stocker les données des témoins
            if (temoins) {
                StringBuilder sbTemoins = new StringBuilder();
                sbTemoins.append("temoin1_nom=").append(request.getParameter("temoin1_nom"))
                .append("&temoin1_prenom=").append(request.getParameter("temoin1_prenom"))
                .append("&temoin2_nom=").append(request.getParameter("temoin2_nom") != null ? request.getParameter("temoin2_nom") : "")
                .append("&temoin2_prenom=").append(request.getParameter("temoin2_prenom") != null ? request.getParameter("temoin2_prenom") : "");
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "temoins");
                stmtVariete.setString(3, sbTemoins.toString());
                stmtVariete.addBatch();
            }
            
            // Stocker les données des parents
            if (parents) {
                StringBuilder sbParents = new StringBuilder();
                sbParents.append("pere_nom=").append(request.getParameter("pere_nom"))
                .append("&pere_prenom=").append(request.getParameter("pere_prenom"))
                .append("&pere_profession=").append(request.getParameter("pere_profession") != null ? request.getParameter("pere_profession") : "")
                .append("&pere_residance=").append(request.getParameter("pere_residance") != null ? request.getParameter("pere_residance") : "")
                .append("&mere_nom=").append(request.getParameter("mere_nom"))
                .append("&mere_prenom=").append(request.getParameter("mere_prenom"))
                .append("&mere_profession=").append(request.getParameter("mere_profession") != null ? request.getParameter("mere_profession") : "")
                .append("&mere_residance=").append(request.getParameter("mere_residance") != null ? request.getParameter("mere_residance") : "");
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "parents");
                stmtVariete.setString(3, sbParents.toString());
                stmtVariete.addBatch();
            }
            
            // Stocker les données du conjoint
            if (conjoints) {
                StringBuilder sbConjoint = new StringBuilder();
                sbConjoint.append("conjoint_nom=").append(request.getParameter("conjoint_nom"))
                .append("&conjoint_prenom=").append(request.getParameter("conjoint_prenom"))
                .append("&conjoint_date_naissance=").append(request.getParameter("conjoint_date_naissance"))
                .append("&conjoint_lieu_naissance=").append(request.getParameter("conjoint_lieu_naissance"))
                .append("&conjoint_profession=").append(request.getParameter("conjoint_profession") != null ? request.getParameter("conjoint_profession") : "")
                .append("&conjoint_residance=").append(request.getParameter("conjoint_residance") != null ? request.getParameter("conjoint_residance") : "");
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "conjoint");
                stmtVariete.setString(3, sbConjoint.toString());
                stmtVariete.addBatch();
            }
            
            // Stocker les données du médecin
            if (medecin) {
                StringBuilder sbMedecin = new StringBuilder();
                sbMedecin.append("medecin_nom=").append(request.getParameter("medecin_nom"))
                .append("&medecin_prenom=").append(request.getParameter("medecin_prenom"))
                .append("&medecin_adresse=").append(request.getParameter("medecin_adresse") != null ? request.getParameter("medecin_adresse") : "");
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "medecin");
                stmtVariete.setString(3, sbMedecin.toString());
                stmtVariete.addBatch();
            }
            
            // Stocker les données de la mention marginale
            if (mentionMarginale) {
                StringBuilder sbMention = new StringBuilder();
                sbMention.append("mention_type=").append(request.getParameter("mention_marginale"))
                .append("&mention_details=").append(request.getParameter("mention_details"));
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "mention_marginale");
                stmtVariete.setString(3, sbMention.toString());
                stmtVariete.addBatch();
            }
            
            // Stocker les données de la cause de décès
            if (causeDeces) {
                StringBuilder sbDeces = new StringBuilder();
                sbDeces.append("cause_deces=").append(request.getParameter("cause_deces"))
                .append("&date_deces=").append(request.getParameter("date_deces"))
                .append("&lieu_deces=").append(request.getParameter("lieu_deces"));
                
                stmtVariete.setInt(1, demandeId);
                stmtVariete.setString(2, "cause_deces");
                stmtVariete.setString(3, sbDeces.toString());
                stmtVariete.addBatch();
            }
            
            // Exécuter tous les batches
            if (temoins || parents || conjoints || medecin || mentionMarginale || causeDeces) {
                stmtVariete.executeBatch();
            }
            
            connection.commit();
            return demandeId; // Retourner l'ID de la demande créée
            
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return 0; // Retourner 0 en cas d'erreur
        } finally {
            // Fermer les ressources
            try {
                if (stmtVariete != null) stmtVariete.close();
                if (stmtDemande != null) stmtDemande.close();
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Récupérer le nombre de demandes en cours
    public int getNombreDemandesEnCours(int idUtilisateur) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demandedocument WHERE idutilisateur = ? AND status = 'en cours'";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    public List<DemandeDocument> getDemandesFiltrees(String statut, String typeDocument, String dateDebut, String dateFin) {
        List<DemandeDocument> demandes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DatabaseListener.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT dd.*, n.nom as nature_document " +
                "FROM demandedocument dd " +
                "LEFT JOIN nature n ON dd.idnature = n.idnature " +
                "WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            System.out.println("🔍 FILTRES - Statut: " + statut + ", Type: " + typeDocument + ", Début: " + dateDebut + ", Fin: " + dateFin);
            
            if (statut != null && !statut.isEmpty()) {
                sql.append(" AND dd.status = ?");
                params.add(statut);
            }
            
            if (typeDocument != null && !typeDocument.isEmpty()) {
                sql.append(" AND dd.idnature = ?");
                params.add(Integer.parseInt(typeDocument));
            }
            
            if (dateDebut != null && !dateDebut.isEmpty()) {
                sql.append(" AND dd.date >= ?");
                params.add(java.sql.Date.valueOf(dateDebut));
            }
            
            if (dateFin != null && !dateFin.isEmpty()) {
                sql.append(" AND dd.date <= ?");
                params.add(java.sql.Date.valueOf(dateFin));
            }
            
            sql.append(" ORDER BY dd.date DESC");
            
            System.out.println("🔍 SQL: " + sql.toString());
            System.out.println("🔍 Paramètres: " + params);
            
            stmt = connection.prepareStatement(sql.toString());
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                DemandeDocument demande = mapResultSetToDemandeDocument(rs);
                demande.setNatureDocument(rs.getString("nature_document"));
                demandes.add(demande);
            }
            
            System.out.println("✅ Demandes filtrées trouvées: " + demandes.size());
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getDemandesFiltrees: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return demandes;
    }
    public int getTotalDemandes() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int total = 0;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT COUNT(*) as total FROM demandedocument";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                total = rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getTotalDemandes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Total demandes: " + total);
        return total;
    }
    public int getDemandesByStatusCount(String status) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT COUNT(*) as count FROM demandedocument WHERE status = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getDemandesByStatusCount: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Demandes avec statut '" + status + "': " + count);
        return count;
    }

    public boolean updateStatus(int idDemande, String nouveauStatut) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "UPDATE demandedocument SET status = ? WHERE iddemande = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, nouveauStatut);
            stmt.setInt(2, idDemande);
            
            int affectedRows = stmt.executeUpdate();
            
            System.out.println("✅ Mise à jour statut - ID: " + idDemande + ", Nouveau statut: " + nouveauStatut + ", Lignes affectées: " + affectedRows);
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR updateStatus: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private DemandeDocument mapResultSetToDemandeDocument(ResultSet rs) throws SQLException {
        DemandeDocument demande = new DemandeDocument();
        
        demande.setIdDemande(rs.getInt("iddemande"));
        demande.setDate(rs.getDate("date"));
        demande.setFormat(rs.getString("format"));
        demande.setStatus(rs.getString("status"));
        
        if (columnExists(rs, "idutilisateur")) {
            demande.setIdutilisateur(rs.getInt("idutilisateur"));
        }
        
        if (columnExists(rs, "id")) {
            demande.setId(rs.getInt("id"));
        }
        
        if (columnExists(rs, "iddocument")) {
            demande.setIdDocument(rs.getInt("iddocument"));
        }
        if (columnExists(rs, "idpaiement")) {
            Integer idPaiement = (Integer) rs.getObject("idpaiement");
            demande.setIdpaiement(idPaiement);
        }
        if (columnExists(rs, "idnature")) {
            demande.setIdNature(rs.getInt("idnature"));
        }
        if (columnExists(rs, "idmotif")) {
            demande.setIdMotif(rs.getInt("idmotif"));
        }
        
        demande.setDestinataire(rs.getString("destinataire"));
        
        demande.setPrenom(rs.getString("prenom"));
        demande.setNom(rs.getString("nom"));
        demande.setDatedenaissance(rs.getDate("datedenaissance"));
        demande.setLieudenaissance(rs.getString("lieudenaissance"));
        demande.setSexe(rs.getString("sexe"));
        demande.setProfession(rs.getString("profession"));
        
        if (columnExists(rs, "residence")) {
            demande.setResidance(rs.getString("residence"));
        } else if (columnExists(rs, "residance")) {
            demande.setResidance(rs.getString("residance"));
        }
        
        demande.setAdresseLivraison(rs.getString("adresse_livraison"));
        demande.setVilleLivraison(rs.getString("ville_livraison"));
        
        return demande;
    }
    public List<DemandeDocument> getDemandesEnAttente() {
        return getDemandesByStatus("en attente");
    }
    private boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public List<DemandeDocument> getDemandesByStatus(String status) {
        List<DemandeDocument> demandes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT dd.*, n.nom as nature_document FROM demandedocument dd " +
                        "LEFT JOIN nature n ON dd.idnature = n.idnature " +
                        "WHERE dd.status = ? " +
                        "ORDER BY dd.date DESC";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                DemandeDocument demande = mapResultSetToDemandeDocument(rs);
                demande.setNatureDocument(rs.getString("nature_document"));
                demandes.add(demande);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getDemandesByStatus: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Demandes avec statut '" + status + "': " + demandes.size());
        return demandes;
    }

    public Map<String, Integer> getStatistiquesDemandes() {
        Map<String, Integer> stats = new HashMap<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT status, COUNT(*) as count FROM demandedocument GROUP BY status";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getStatistiquesDemandes: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Statistiques: " + stats);
        return stats;
    }
    // Récupérer le nombre de demandes validées
    public int getNombreDemandesValidees(int idUtilisateur) throws SQLException {
        String sql = "SELECT COUNT(*) FROM demandedocument WHERE idutilisateur = ? AND status = 'en cours'";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    public boolean marquerCommeSignee(int idDemande) {
        PreparedStatement stmt = null;
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "UPDATE demandes SET status = 'signé', date_signature = ? WHERE id_demande = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
            stmt.setInt(2, idDemande);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        }
 // Méthode pour récupérer TOUTES les demandes (pour debug)
    public List<DemandeDocument> getAllDemandes() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DemandeDocument> demandes = new ArrayList<>();
        
        try {
            connection = DatabaseListener.getConnection();
            String sql = "SELECT * FROM demandes ORDER BY date_demande DESC";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                DemandeDocument demande = new DemandeDocument();
                demande.setIdDemande(rs.getInt("id_demande"));
                demande.setDate(rs.getDate("date_demande"));
                demande.setFormat(rs.getString("format"));
                demande.setStatus(rs.getString("status"));
                demandes.add(demande);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }

//    // Méthode corrigée pour les filtres
//    public List<DemandeDocument> getDemandesFiltreesPourOfficier(String statut, String typeDocument, String format, String dateDemande) {
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        List<DemandeDocument> demandes = new ArrayList<>();
//        
//        try {
//            connection = DatabaseListener.getConnection();
//            
//            // Construction dynamique de la requête SQL
//            StringBuilder sql = new StringBuilder(
//                "SELECT d.*, p.prenom, p.nom, p.datedenaissance, p.lieudenaissance, p.sexe, p.profession, p.residance, " +
//                "n.nom as nature_document " +
//                "FROM demandes d " +
//                "LEFT JOIN personnes p ON d.id_personne = p.id_personne " +
//                "LEFT JOIN natures n ON d.id_nature = n.id_nature " +
//                "WHERE 1=1"
//            );
//            
//            List<Object> params = new ArrayList<>();
//            
//            // Filtre par statut
//            if (statut != null && !statut.trim().isEmpty()) {
//                sql.append(" AND d.status = ?");
//                params.add(statut.trim());
//            } else {
//                // Par défaut, montrer les demandes "trouvé" et "validée"
//                sql.append(" AND (d.status = 'trouvé' OR d.status = 'validée')");
//            }
//            
//            // Filtre par type de document
//            if (typeDocument != null && !typeDocument.trim().isEmpty()) {
//                sql.append(" AND n.nom = ?");
//                params.add(typeDocument.trim());
//            }
//            
//            // Filtre par format
//            if (format != null && !format.trim().isEmpty()) {
//                sql.append(" AND d.format = ?");
//                params.add(format.trim());
//            }
//            
//            // Filtre par date
//            if (dateDemande != null && !dateDemande.trim().isEmpty()) {
//                sql.append(" AND DATE(d.date_demande) = ?");
//                params.add(dateDemande.trim());
//            }
//            
//            sql.append(" ORDER BY d.date_demande DESC");
//            
//            System.out.println("SQL: " + sql.toString());
//            System.out.println("Params: " + params);
//            
//            stmt = connection.prepareStatement(sql.toString());
//            
//            // Set des paramètres
//            for (int i = 0; i < params.size(); i++) {
//                stmt.setObject(i + 1, params.get(i));
//            }
//            
//            rs = stmt.executeQuery();
//            
//            while (rs.next()) {
//                DemandeDocument demande = mapResultSetToDemandeDocument(rs);
//                demandes.add(demande);
//            }
//            
//            System.out.println("Nombre de demandes récupérées: " + demandes.size());
//            
//        } catch (SQLException e) {
//            System.err.println("Erreur SQL dans getDemandesFiltreesPourOfficier: " + e.getMessage());
//            e.printStackTrace();
//        
//        }
//        
//        return demandes;
//    }
    public List<DemandeDocument> getDemandesFiltreesPourOfficier(String statut, String typeDocument, String format, String dateDemande) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DemandeDocument> demandes = new ArrayList<>();
        
        try {
            connection = DatabaseListener.getConnection();

        	if (connection == null) {
                System.err.println("❌ Connexion null");
                return demandes;
            }
            
            // Construction de la requête SQL
            StringBuilder sql = new StringBuilder(
                "SELECT d.*, p.prenom, p.nom, p.datedenaissance, p.lieudenaissance, p.sexe, p.profession, p.residance, " +
                "n.nom as nature_document " +
                "FROM demandedocument d " +
                "LEFT JOIN personne p ON d.id = p.id " +
                "LEFT JOIN nature n ON d.idnature = n.idnature " +
                "WHERE 1=1"
            );
            
            List<Object> params = new ArrayList<>();
            
            // Filtre par statut - Dans la simulation, "recherché" = "trouvé"
            if (statut != null && !statut.trim().isEmpty()) {
                if ("trouvé".equals(statut.trim())) {
                    // Si on filtre par "trouvé", on inclut aussi "recherché"
                    sql.append(" AND (d.status = ? OR d.status = 'recherché')");
                    params.add("trouvé");
                } else {
                    sql.append(" AND d.status = ?");
                    params.add(statut.trim());
                }
            } else {
                // Par défaut : montrer les actes à signer ("trouvé" et "recherché")
                sql.append(" AND (d.status = 'trouvé' OR d.status = 'recherché')");
            }
            
            // Filtre par type de document
            if (typeDocument != null && !typeDocument.trim().isEmpty()) {
                sql.append(" AND n.nom = ?");
                params.add(typeDocument.trim());
            }
            
            // Filtre par format
            if (format != null && !format.trim().isEmpty()) {
                sql.append(" AND d.format = ?");
                params.add(format.trim());
            }
            
            // Filtre par date
            if (dateDemande != null && !dateDemande.trim().isEmpty()) {
                sql.append(" AND DATE(d.date) = ?");
                params.add(dateDemande.trim());
            }
            
            sql.append(" ORDER BY d.date DESC");
            
            System.out.println("SQL Officier: " + sql.toString());
            System.out.println("Params: " + params);
            
            stmt = connection.prepareStatement(sql.toString());
            
            // Set des paramètres
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                DemandeDocument demande = mapResultSetToDemandeDocument(rs);
                demandes.add(demande);
            }
            
            System.out.println("✅ Demandes récupérées pour officier: " + demandes.size());
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        } 
        
        return demandes;
    }
    
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("DemandeDocumentDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}