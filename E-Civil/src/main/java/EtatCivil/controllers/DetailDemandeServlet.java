package EtatCivil.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import EtatCivil.dao.CompteDAO;
import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.PersonneDAO;
import EtatCivil.dao.UtilisateurDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import EtatCivil.modeles.Personne;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.utils.DatabaseListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/detailDemande")
public class DetailDemandeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null || !"delegue".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String idDemandeStr = request.getParameter("id");
        if (idDemandeStr == null) {
            session.setAttribute("error", "ID de demande manquant");
            response.sendRedirect(request.getContextPath() + "/delegue");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        CompteDAO compteDAO = new CompteDAO();
        
        try {
            int idDemande = Integer.parseInt(idDemandeStr);
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            
            System.out.println("=== DEBUG DETAIL DEMANDE ===");
            System.out.println("ID Demande: " + idDemande);
            System.out.println("Demande récupérée: " + (demande != null ? "OUI" : "NON"));
            
            if (demande != null) {
                System.out.println("ID Personne (sujet): " + demande.getId());
                System.out.println("ID Utilisateur (demandeur): " + demande.getIdutilisateur());
                System.out.println("Prénom (personne concernée): " + demande.getPrenom());
                System.out.println("Nom (personne concernée): " + demande.getNom());
            }
            
            if (demande == null) {
                session.setAttribute("error", "Demande non trouvée");
                response.sendRedirect(request.getContextPath() + "/delegue");
                return;
            }
            
            // CORRECTION: Récupérer l'utilisateur via l'ID de la personne
            Utilisateur demandeur = null;
            Compte compteDemandeur = null;
            
            if (demande.getId() != 0) {
                // Essayer de récupérer l'utilisateur par l'ID de la personne
                demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
                System.out.println("Demandeur récupéré via ID personne: " + (demandeur != null ? "OUI" : "NON"));
                
                if (demandeur != null) {
                    compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
                    if (compteDemandeur != null) {
                        System.out.println("Compte demandeur récupéré - Email: " + compteDemandeur.getEmail());
                    } else {
                        System.out.println("❌ Compte demandeur non trouvé pour personne ID: " + demande.getId());
                    }
                }
            } else {
                System.out.println("❌ ID Personne est 0, impossible de récupérer le demandeur");
            }
            
            // Si le demandeur n'est pas trouvé via l'ID personne, essayer via idutilisateur (si non null)
            if (demandeur == null && demande.getIdutilisateur() != 0) {
                demandeur = utilisateurDAO.getUtilisateurByIdUtilisateur(demande.getIdutilisateur());
                System.out.println("Demandeur récupéré via ID utilisateur: " + (demandeur != null ? "OUI" : "NON"));
                
                if (demandeur != null && compteDemandeur == null) {
                    compteDemandeur = compteDAO.getCompteByIdPersonne(demandeur.getId());
                }
            }
            
            // Récupérer les variétés
            Map<String, String> varietes = demandeDAO.getVarietesByDemandeId(idDemande);
            
            // Lancer la recherche automatique
            List<Object[]> resultatsRecherche = lancerRechercheRegistres(demande);
            
            request.setAttribute("demande", demande);
            request.setAttribute("demandeur", demandeur);
            request.setAttribute("compteDemandeur", compteDemandeur);
            request.setAttribute("varietes", varietes);
            request.setAttribute("resultatsRecherche", resultatsRecherche);
            request.setAttribute("pageContent", "/WEB-INF/views/detailDemande.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors de la récupération des détails: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/delegue");
        } finally {
            demandeDAO.close();
            utilisateurDAO.close();
        }
    }
    
    private List<Object[]> lancerRechercheRegistres(DemandeDocument demande) {
        List<Object[]> resultats = new ArrayList<>();
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DatabaseListener.getConnection();
            
            // Recherche dans la vue recherche_documents basée sur les informations de la demande
            String sql = "SELECT * FROM recherche_documents WHERE " +
                        "LOWER(nature_document) LIKE LOWER(?) AND " +
                        "LOWER(?) LIKE LOWER('%' || mots_cles || '%')";
            
            stmt = connection.prepareStatement(sql);
            
            // Adapter la recherche selon le type de document
            String typeRecherche = getTypeRechercheFromNature(demande.getNatureDocument());
            String motsCles = genererMotsCles(demande);
            
            stmt.setString(1, "%" + typeRecherche + "%");
            stmt.setString(2, motsCles);
            
            System.out.println("🔍 RECHERCHE REGISTRES:");
            System.out.println("  - Type: " + typeRecherche);
            System.out.println("  - Mots-clés: " + motsCles);
            System.out.println("  - SQL: " + sql);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] document = new Object[10];
                document[0] = rs.getInt("iddocument");
                document[1] = rs.getString("type_document");
                document[2] = rs.getString("nature_document");
                document[3] = rs.getString("description");
                document[4] = rs.getDate("dateevenement");
                document[5] = rs.getString("lieuevenement");
                document[6] = rs.getString("commune");
                document[7] = rs.getString("mots_cles");
                document[8] = rs.getDouble("tarif_base");
                document[9] = rs.getInt("duree_validite");
                
                resultats.add(document);
            }
            
            System.out.println("✅ Résultats recherche: " + resultats.size() + " document(s) trouvé(s)");
            
        } catch (SQLException e) {
            System.err.println("❌ ERREUR recherche registres: " + e.getMessage());
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
        
        return resultats;
    }
    
    private String getTypeRechercheFromNature(String natureDocument) {
        if (natureDocument == null) return "document";
        
        if (natureDocument.toLowerCase().contains("naissance")) {
            return "naissance";
        } else if (natureDocument.toLowerCase().contains("mariage")) {
            return "mariage";
        } else if (natureDocument.toLowerCase().contains("décès") || natureDocument.toLowerCase().contains("deces")) {
            return "décès";
        } else {
            return "document";
        }
    }
    
    private String genererMotsCles(DemandeDocument demande) {
        StringBuilder motsCles = new StringBuilder();
        
        motsCles.append(demande.getPrenom()).append(" ");
        motsCles.append(demande.getNom()).append(" ");
        motsCles.append(demande.getLieudenaissance()).append(" ");
        
        if (demande.getDatedenaissance() != null) {
            // Format YYYY-MM-DD pour la recherche
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            motsCles.append(sdf.format(demande.getDatedenaissance())).append(" ");
        }
        
        if (demande.getProfession() != null) {
            motsCles.append(demande.getProfession()).append(" ");
        }
        
        return motsCles.toString().trim();
    }
}