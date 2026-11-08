package EtatCivil.controllers;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/suividemande")
public class SuiviDemandeServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String idDemandeStr = request.getParameter("iddemande");
        
        if (idDemandeStr == null) {
            session.setAttribute("error", "Paramètre de demande manquant");
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = null;
        
        try {
            int idDemande = Integer.parseInt(idDemandeStr);
            demandeDAO = new DemandeDocumentDAO();
            
            // Récupérer la demande avec vérification de propriété
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            
            if (demande == null) {
                session.setAttribute("error", "Demande non trouvée");
                response.sendRedirect(request.getContextPath() + "/mesdemandes");
                return;
            }
            
//            // Vérifier que la demande appartient à l'utilisateur
//            if (demande.getId() != compte.getIdPersonne()) {
//                session.setAttribute("error", "Accès non autorisé à cette demande");
//                response.sendRedirect(request.getContextPath() + "/mesdemandes");
//                return;
//            }
            
            // Récupérer l'historique des statuts
            List<Map<String, Object>> historique = demandeDAO.getHistoriqueStatut(idDemande);
            
            // Récupérer les variétés si nécessaire
            Map<String, String> varietes = demandeDAO.getVarietesByDemandeId(idDemande);
            
            request.setAttribute("demande", demande);
            request.setAttribute("historique", historique);
            request.setAttribute("varietes", varietes);
            request.setAttribute("pageContent", "/WEB-INF/views/suividemande.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du chargement du suivi: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
        } finally {
            if (demandeDAO != null) demandeDAO.close();
        }
    }
}