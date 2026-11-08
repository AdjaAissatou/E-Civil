package EtatCivil.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "DeconnexionServlet", value = "/deconnexion")
public class DeconnexionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        System.out.println("=== DÉCONNEXION ===");
        
        if (session != null) {
            // Logger la déconnexion
            EtatCivil.modeles.Compte compte = (EtatCivil.modeles.Compte) session.getAttribute("compte");
            if (compte != null) {
                System.out.println("Déconnexion de: " + compte.getEmail() + " (Role: " + compte.getRole() + ")");
            }
            
            // Invalider la session
            session.invalidate();
            System.out.println("Session invalidée avec succès");
            
            // Effacer le cookie de session
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        
        // Rediriger vers la page de connexion avec message
        response.sendRedirect(request.getContextPath() + "/accueil?message=deconnecte");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
