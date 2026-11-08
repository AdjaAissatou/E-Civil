package EtatCivil.dao;

import EtatCivil.modeles.Compte;
import EtatCivil.modeles.TypeUtilisateur;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.utils.DatabaseListener;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    private Connection connection;
    private boolean shouldCloseConnection = false;

    public UtilisateurDAO(Connection connection) {
        this.connection = connection;
        this.shouldCloseConnection = false;
    }

    public UtilisateurDAO() {
        try {
            this.connection = DatabaseListener.getConnection();
            this.shouldCloseConnection = true;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion UtilisateurDAO", e);
        }
    }
    public int getNombreUtilisateurs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
	// Enregistrer un utilisateur et son type
    public int enregistrer(Utilisateur utilisateur, int idPersonne,int idcompte, Connection conn) throws Exception {
        String sql = "INSERT INTO utilisateur (idcompte, id, datedinscription, numerocartedintentite_visa, numerotelephone, nombredenfants) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING idutilisateur";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idcompte); // FK vers personne

            stmt.setInt(2, idPersonne); // FK vers personne
            stmt.setDate(3, new java.sql.Date(utilisateur.getDatedinscription().getTime()));
            stmt.setInt(4, utilisateur.getNumerocartedintentite_visa());
            stmt.setInt(5, utilisateur.getNumerotelephone());
            stmt.setInt(6, utilisateur.getNombredenfants());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idUtilisateur = rs.getInt("idutilisateur");

                    // Enregistrer les types d’utilisateur
                    if (utilisateur.getTypeUtilisateur() != null) {
                        String sqlType = "INSERT INTO typeutilisateur (idutilisateur, nom, paysderesidence) VALUES (?, ?, ?)";
                        try (PreparedStatement stmtType = conn.prepareStatement(sqlType)) {
                            for (TypeUtilisateur typeUser : utilisateur.getTypeUtilisateur()) {
                                stmtType.setInt(1, idUtilisateur);
                                stmtType.setString(2, typeUser.getNom());
                                stmtType.setString(3, typeUser.getPaysderesidence());
                                stmtType.addBatch();
                            }
                            stmtType.executeBatch();
                        }
                    }

                    return idUtilisateur;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return -1;
    }

    public Utilisateur getUtilisateurByIdUtilisateur(int idUtilisateur) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Utilisateur utilisateur = null;
        
        try {
            // CORRECTION: Ajouter l'espace manquant et corriger le nom de colonne
            String sql = "SELECT u.*, p.prenom, p.nom, p.datedenaissance, p.lieudenaissance, " +
                        "p.sexe, p.profession, p.residance " + // ESPACE AJOUTÉ ICI et residence au lieu de residance
                        "FROM utilisateur u " +
                        "JOIN personne p ON u.id = p.id " +
                        "WHERE u.idutilisateur = ?";
            
            System.out.println("🔍 SQL: " + sql); // Debug
            System.out.println("🔍 ID Utilisateur recherché: " + idUtilisateur);
            
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idUtilisateur);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                utilisateur = new Utilisateur();
                // Remplir les attributs de Personne
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setDatedenaissance(rs.getDate("datedenaissance"));
                utilisateur.setLieudenaissance(rs.getString("lieudenaissance"));
                utilisateur.setSexe(rs.getString("sexe"));
                utilisateur.setProfession(rs.getString("profession"));
                utilisateur.setResidance(rs.getString("residance")); // CORRECTION: residence
                
                // Remplir les attributs spécifiques à Utilisateur
                utilisateur.setIdutilisateur(rs.getInt("idutilisateur"));
                utilisateur.setDatedinscription(rs.getDate("datedinscription"));
                utilisateur.setNumerotelephone(rs.getInt("numerotelephone"));
                // Gérer les valeurs nulles
                if (rs.wasNull()) {
                    utilisateur.setNumerotelephone(0); // ou une valeur par défaut
                }
                
                System.out.println("✅ Utilisateur trouvé - ID: " + idUtilisateur + 
                                 ", Nom: " + utilisateur.getPrenom() + " " + utilisateur.getNom());
            } else {
                System.out.println("❌ Aucun utilisateur trouvé avec ID: " + idUtilisateur);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getUtilisateurByIdUtilisateur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return utilisateur;
    }
    // Récupérer tous les utilisateurs
    public List<Utilisateur> getTousUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur u JOIN personne p ON u.id = p.id";
        try (PreparedStatement ps = connection.prepareStatement(sql) // CORRECTION: utilise connection, pas ConnexionBD
;
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Utilisateur u = new Utilisateur();

                // Personne
                u.setId(rs.getInt("id"));
                u.setPrenom(rs.getString("prenom"));
                u.setNom(rs.getString("nom"));
                u.setSexe(rs.getString("sexe"));
                u.setDatedenaissance(rs.getDate("datedenaissance"));
                u.setLieudenaissance(rs.getString("lieudenaissance"));
                u.setProfession(rs.getString("profession"));
                u.setResidance(rs.getString("residance"));

                // Utilisateur
                u.setIdutilisateur(rs.getInt("idutilisateur"));
                u.setDatedinscription(rs.getDate("datedinscription"));
                u.setNumerocartedintentite_visa(rs.getInt("numerocartedintentite_visa"));
                u.setNumerotelephone(rs.getInt("numerotelephone"));
                u.setNombredenfants(rs.getInt("nombredenfants"));

                // Comptes liés
                List<Compte> comptes = getComptesParPersonne(u.getId());
                u.setCompte(comptes);

                // TypeUtilisateur
                u.setTypeUtilisateur(getTypeUtilisateurParUtilisateur(u.getIdutilisateur(), connection));

                utilisateurs.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }
    public Utilisateur getUtilisateurById(int id) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Utilisateur utilisateur = null;
        
        try {
            String sql = "SELECT * FROM personne WHERE id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                utilisateur = new Utilisateur();
                utilisateur.setId(rs.getInt("id"));
                utilisateur.setPrenom(rs.getString("prenom"));
                utilisateur.setNom(rs.getString("nom"));
                utilisateur.setDatedenaissance(rs.getDate("datedenaissance"));
                utilisateur.setLieudenaissance(rs.getString("lieudenaissance"));
                utilisateur.setSexe(rs.getString("sexe"));
                utilisateur.setProfession(rs.getString("profession"));
                utilisateur.setResidance(rs.getString("residance"));

            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getUtilisateurById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return utilisateur;
    }
    // Récupérer les comptes d’une personne
 // Récupérer les comptes d'une personne - DEVENUE PUBLIQUE
    public List<Compte> getComptesParPersonne(int idPersonne) throws SQLException {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM compte WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPersonne);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compte c = new Compte();
                    c.setIdcompte(rs.getInt("idcompte"));
                    c.setEmail(rs.getString("email"));
                    c.setMotdepasse(rs.getString("motdepasse"));
                    c.setStatut(rs.getBoolean("statut"));
                    c.setRole(rs.getString("role"));
                    c.setIdPersonne(rs.getInt("id"));

                    // Associer un utilisateur vide pour liaison côté session
                    Utilisateur u = new Utilisateur();
                    u.setIdutilisateur(rs.getInt("id"));
                    c.setPersonne(u);

                    comptes.add(c);
                }
                System.out.println("✅ Comptes trouvés pour personne " + idPersonne + ": " + comptes.size());
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR getComptesParPersonne: " + e.getMessage());
            throw e;
        }
        return comptes;
    }
    // Mettre à jour le rôle d'un compte
    public boolean mettreAJourRole(int idCompte, String nouveauRole) {
        String sql = "UPDATE compte SET role = ? WHERE idcompte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nouveauRole);
            ps.setInt(2, idCompte);
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Récupérer TypeUtilisateur pour un utilisateur donné
    private List<TypeUtilisateur> getTypeUtilisateurParUtilisateur(int idUtilisateur, Connection conn) throws SQLException {
        List<TypeUtilisateur> types = new ArrayList<>();
        String sql = "SELECT * FROM typeutilisateur WHERE idutilisateur = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TypeUtilisateur t = new TypeUtilisateur();
                    t.setNom(rs.getString("nom"));
                    t.setPaysderesidence(rs.getString("paysderesidence"));
                    types.add(t);
                }
            }
        }
        return types;
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
                System.out.println("UtilisateurDAO: Connexion fermée");
            } catch (SQLException e) {
                System.err.println("UtilisateurDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }
}
