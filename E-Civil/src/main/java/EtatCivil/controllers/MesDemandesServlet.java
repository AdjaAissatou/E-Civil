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

@WebServlet("/mesdemandes")
public class MesDemandesServlet extends HttpServlet {
    
	// Dans MesDemandesServlet.java
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	        throws ServletException, IOException {
	    
	    HttpSession session = request.getSession();
	    Compte compte = (Compte) session.getAttribute("compte");
	    
	    if (compte == null) {
	        response.sendRedirect(request.getContextPath() + "/connexion");
	        return;
	    }
	    
	    DemandeDocumentDAO demandeDAO = null;
	    
	    try {
	        demandeDAO = new DemandeDocumentDAO();
	        int idPersonne = compte.getPersonne().getId(); // Utiliser l'ID personne
	        
	        // Récupérer les demandes de l'utilisateur par ID personne
	        List<DemandeDocument> demandes = demandeDAO.getDemandesByUtilisateur(idPersonne);
	        
	        // Pour chaque demande, vérifier si le paiement est requis
	        for (DemandeDocument demande : demandes) {
	            boolean paiementRequis = demandeDAO.isPaiementRequis(demande.getIdDemande());
	            demande.setPaiementRequis(paiementRequis);
	        }
	        
	        // Récupérer les statistiques (à adapter aussi)
	        Map<String, Integer> stats = demandeDAO.getStatistiquesDemandes(idPersonne);
	        
	        request.setAttribute("demandes", demandes);
	        request.setAttribute("stats", stats);
	        request.setAttribute("totalDemandes", demandes.size());
	        request.setAttribute("pageContent", "/WEB-INF/views/mesdemandes.jsp");
	        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("error", "Erreur lors de la récupération des demandes: " + e.getMessage());
	        response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
	    } finally {
	        if (demandeDAO != null) demandeDAO.close();
	    }
	}
}