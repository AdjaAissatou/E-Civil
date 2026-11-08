package EtatCivil.controllers;

import EtatCivil.dao.NatureDAO;
import EtatCivil.modeles.Compte;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/documents")
public class DocumentsServlet extends HttpServlet {
	
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
//        HttpSession session = request.getSession(false);
//        Compte compte = (session != null) ? (Compte) session.getAttribute("compte") : null;
//        
//        if (compte == null) {
//            System.out.println("DocumentsServlet: Session invalide - redirection");
//            
//            // Redirection avec l'URL de retour
//            String currentUrl = request.getRequestURL().toString();
//            if (request.getQueryString() != null) {
//                currentUrl += "?" + request.getQueryString();
//            }
//            
//            String encodedUrl = java.net.URLEncoder.encode(currentUrl, "UTF-8");
//            response.sendRedirect(request.getContextPath() + "/connexion?redirect=" + encodedUrl);
//            return;
//        }
//        
//        System.out.println("DocumentsServlet: Utilisateur connecté - " + compte.getEmail());
        
        NatureDAO natureDAO = null;
        try {
            natureDAO = new NatureDAO();
            List<Object[]> documents = natureDAO.getDocumentsAvecTarifs();
            
            request.setAttribute("documents", documents);
            request.setAttribute("pageContent", "/WEB-INF/views/documents.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors du chargement des documents: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
        } finally {
            if (natureDAO != null) {
                natureDAO.close();
            }
        }
    
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}