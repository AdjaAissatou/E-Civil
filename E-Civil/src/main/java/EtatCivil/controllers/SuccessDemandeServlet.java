package EtatCivil.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.modeles.DemandeDocument;

@WebServlet("/success")
public class SuccessDemandeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== SUCCESS SERVLET APPELE ===");
        HttpSession session = request.getSession();
        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        
        if (session == null) {
            System.out.println("❌ Session null - redirection documents");
            response.sendRedirect(request.getContextPath() + "/documents");
            return;
        }
    
        
        try {
            // Récupérer la demande depuis la session ou par ID
            DemandeDocument demande = (DemandeDocument) session.getAttribute("demande");
            
            // Si la demande dans la session n'a pas toutes les infos, la recharger depuis la BD
            if (demande != null && demande.getPrenom() == null) {
                demande = demandeDAO.getDemandeById(demande.getIdDemande());
                session.setAttribute("demande", demande);
            }        
	       
	        if (demande == null) {
	            System.out.println("❌ Demande null dans session - redirection documents");
	            response.sendRedirect(request.getContextPath() + "/documents");
	            return;
	        }
  
        
        
	        System.out.println("✅ Affichage page success.jsp");
	        request.setAttribute("pageContent", "/WEB-INF/views/success.jsp");
	        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors de l'affichage de la confirmation");
            response.sendRedirect(request.getContextPath() + "/documents");
        } finally {
            demandeDAO.close();
        }
        // Nettoyer la session après affichage
        session.removeAttribute("demande");
        session.removeAttribute("varietes");
        session.removeAttribute("natureDocument");
        System.out.println("✅ Session nettoyée");
    }
}