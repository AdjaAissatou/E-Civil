package EtatCivil.dao;

import EtatCivil.modeles.Compte;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.utils.DatabaseListener;
import EtatCivil.utils.PasswordUtil;

import java.sql.*;

public class CompteDAO {
	   private Connection connection;
	    private boolean shouldCloseConnection = false;

	    public CompteDAO() {
	        try {
	            this.connection = DatabaseListener.getConnection();
	            this.shouldCloseConnection = true;
	        } catch (SQLException e) {
	            throw new RuntimeException("Erreur lors de l'obtention de la connexion pour CompteDAO", e);
	        }
	    }


	  public int creerCompte(Compte compte,int idPersonne, Utilisateur utilisateur, Connection conn) throws Exception {
	       
		  try {
			  
	 
	            // 2. Créer le Compte avec référence vers Personne
	            String sqlCompte = "INSERT INTO compte (email, motdepasse, statut, role, id) " +
	                             "VALUES (?, ?, ?, ?, ?) RETURNING idcompte";

	            int idCompte = -1;
	            try (PreparedStatement stmt = conn.prepareStatement(sqlCompte)) {
	                stmt.setString(1, compte.getEmail());
	                stmt.setString(2, PasswordUtil.hashPassword(compte.getMotdepasse()));
	                stmt.setBoolean(3, compte.getStatut());
	                stmt.setString(4, compte.getRole());
	                stmt.setInt(5, idPersonne); // FK vers personne

	                try (ResultSet rs = stmt.executeQuery()) {
	                    if (rs.next()) {
	                        idCompte = rs.getInt("idcompte");
	                        compte.setIdcompte(idCompte);
	                    }
	                }
	            }

	            if (idCompte <= 0) {
	                throw new Exception("Échec de la création du compte");
	            }



	            return idCompte;

	        } catch (SQLException e) {
	            throw new Exception("Erreur lors de la création du compte: " + e.getMessage(), e);
	        }
	        // Pas de gestion de transaction ici - le service s'en occupe
	    }

	   public Compte seConnecter(String email) {
		     Compte compte = null;
		        
		        // REQUÊTE CORRIGÉE - UTILISE L'ATTRIBUT CONNECTION
		        String sql = "SELECT c.idcompte, c.email, c.motdepasse, c.statut, c.role, " +
		                     "p.id as idpersonne, p.nom, p.prenom, p.sexe, p.datedenaissance, " +
		                     "p.lieudenaissance, p.profession, p.residance, " +
		                     "u.idutilisateur, u.datedinscription, " +
		                     "u.numerotelephone, u.nombredenfants, u.numerocartedintentite_visa " +
		                     "FROM compte c " +
		                     "JOIN utilisateur u ON u.idcompte = c.idcompte " +
		                     "JOIN personne p ON p.id = u.id " +
		                     "WHERE LOWER(c.email) = LOWER(?) AND c.statut = true";

		        try (PreparedStatement ps = connection.prepareStatement(sql)) { // CORRECTION: utilise connection, pas ConnexionBD

	            ps.setString(1, email);
	            System.out.println("Executing query: " + ps.toString()); // Pour voir la requête

	            ResultSet rs = ps.executeQuery();

	            if (rs.next()) {
	                compte = new Compte();
	                compte.setIdcompte(rs.getInt("idcompte"));
	                compte.setEmail(rs.getString("email"));
	                compte.setMotdepasse(rs.getString("motdepasse"));
	                compte.setStatut(rs.getBoolean("statut"));
	                compte.setRole(rs.getString("role"));

	                // Créer l'Utilisateur (qui hérite de Personne)
	                Utilisateur utilisateur = new Utilisateur();
	                
	                // Attributs de Personne (hérités)
	                utilisateur.setId(rs.getInt("idpersonne"));
	                utilisateur.setNom(rs.getString("nom"));
	                utilisateur.setPrenom(rs.getString("prenom"));
	                utilisateur.setSexe(rs.getString("sexe"));
	                utilisateur.setDatedenaissance(rs.getDate("datedenaissance"));
	                utilisateur.setLieudenaissance(rs.getString("lieudenaissance"));
	                utilisateur.setProfession(rs.getString("profession"));
	                utilisateur.setResidance(rs.getString("residance"));
	                
	                // Attributs spécifiques à Utilisateur
	                utilisateur.setIdutilisateur(rs.getInt("idutilisateur"));
	                utilisateur.setDatedinscription(rs.getDate("datedinscription"));
//	                utilisateur.setNumerotelephone(rs.getInt("numerotelephone"));
//	                utilisateur.setNombredenfants(rs.getInt("nombredenfants"));
//	                utilisateur.setNumerocartedintentite_visa(rs.getInt("numerocartedintentite_visa"));
	                // Gestion des valeurs NULL
	                int numTel = rs.getInt("numerotelephone");
	                if (!rs.wasNull()) {
	                    utilisateur.setNumerotelephone(numTel);
	                }
	                
	                int nbEnfants = rs.getInt("nombredenfants");
	                if (!rs.wasNull()) {
	                    utilisateur.setNombredenfants(nbEnfants);
	                }
	                int numCarte = rs.getInt("numerocartedintentite_visa");
	                if (!rs.wasNull()) {
	                    utilisateur.setNumerocartedintentite_visa(numCarte);
	                }
	                // Lier l'utilisateur au compte
	                compte.setPersonne(utilisateur);
	                System.out.println("✅ CompteDAO: Compte créé avec utilisateur - " + 
	                        utilisateur.getPrenom() + " " + utilisateur.getNom() +
	                        " (ID Utilisateur: " + utilisateur.getIdutilisateur() + ")");
	            }
		         else {
	                System.out.println("❌ CompteDAO: Aucun compte trouvé pour: " + email);
	            }
	        } catch (SQLException e) {
	            System.err.println("❌ CompteDAO: Erreur SQL - " + e.getMessage());
	            e.printStackTrace();
	        }
	        return compte;
	    }

	   public Compte getCompteByIdPersonne(int idPersonne) {
		    PreparedStatement stmt = null;
		    ResultSet rs = null;
		    Compte compte = null;
		    
		    try {
		        String sql = "SELECT * FROM compte WHERE id = ?";
		        stmt = connection.prepareStatement(sql);
		        stmt.setInt(1, idPersonne);
		        rs = stmt.executeQuery();
		        
		        if (rs.next()) {
		            compte = new Compte();
		            compte.setIdcompte(rs.getInt("idcompte"));
		            compte.setEmail(rs.getString("email"));
		            compte.setMotdepasse(rs.getString("motdepasse"));
		            compte.setStatut(rs.getBoolean("statut"));
		            compte.setRole(rs.getString("role"));
		            compte.setIdPersonne(rs.getInt("id"));
		            System.out.println("✅ Compte trouvé pour personne ID: " + idPersonne + ", Email: " + compte.getEmail());
		        } else {
		            System.out.println("❌ Aucun compte trouvé pour personne ID: " + idPersonne);
		        }
		        
		    } catch (SQLException e) {
		        System.err.println("❌ ERREUR getCompteByIdPersonne: " + e.getMessage());
		        e.printStackTrace();
		    } finally {
		        try {
		            if (rs != null) rs.close();
		            if (stmt != null) stmt.close();
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
		    
		    return compte;
		}
    public boolean updateRole(int idcompte, String role) {
        String sql = "UPDATE compte SET role = ? WHERE idcompte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // CORRECTION: utilise connection, pas ConnexionBD

            ps.setString(1, role);
            ps.setInt(2, idcompte);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void close() {
        if (shouldCloseConnection && connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("CompteDAO: Erreur fermeture connexion: " + e.getMessage());
            }
        }
    }

}
