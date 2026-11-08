package EtatCivil.controllers;

import java.io.IOException;
import java.util.List;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.NatureDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/delegue")
public class DelegueServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        // Vérifier si l'utilisateur est délégué
        if (compte == null || !"delegue".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        NatureDAO natureDAO = new NatureDAO();
        
        try {
            // Récupérer les paramètres de filtre
            String statut = request.getParameter("statut");
            String typeDocument = request.getParameter("typeDocument");
            String dateDebut = request.getParameter("dateDebut");
            String dateFin = request.getParameter("dateFin");
            
            // Récupérer les demandes filtrées
            List<DemandeDocument> demandes = demandeDAO.getDemandesFiltrees(statut, typeDocument, dateDebut, dateFin);
            
            // Récupérer les statistiques
            int totalDemandes = demandeDAO.getTotalDemandes();
            int demandesEnAttente = demandeDAO.getDemandesByStatusCount("en attente");
            int demandesValidees = demandeDAO.getDemandesByStatusCount("en cours");
            int demandesTerminees = demandeDAO.getDemandesByStatusCount("terminée");
            
            // Récupérer les types de documents pour le filtre
            List<Object[]> natures = natureDAO.getAllNaturesWithVariete();
            
            request.setAttribute("demandes", demandes);
            request.setAttribute("totalDemandes", totalDemandes);
            request.setAttribute("demandesEnAttente", demandesEnAttente);
            request.setAttribute("demandesValidees", demandesValidees);
            request.setAttribute("demandesTerminees", demandesTerminees);
            request.setAttribute("natures", natures);
            request.setAttribute("pageContent", "/WEB-INF/views/delegue.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du chargement des demandes");
            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
        } finally {
            demandeDAO.close();
            natureDAO.close();
        }
    }
}