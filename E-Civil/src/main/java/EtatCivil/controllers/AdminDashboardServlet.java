package EtatCivil.controllers;

import EtatCivil.dao.*;
import EtatCivil.modeles.Activite;
import EtatCivil.modeles.Compte;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AdminDashboardServlet", value = "/adminDashboard")
public class AdminDashboardServlet extends HttpServlet {
    private UtilisateurDAO utilisateurDAO;
    private DocumentEtatCivilDAO documentDAO;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        utilisateurDAO = new UtilisateurDAO();
        documentDAO = new DocumentEtatCivilDAO();
        roleDAO = new RoleDAO();
        permissionDAO = new PermissionDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        // Vérifier si l'utilisateur est connecté
        if (compte== null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        try {
            // Récupérer les statistiques
            int nombreUtilisateurs = utilisateurDAO.getNombreUtilisateurs();
            int nombreDocuments = documentDAO.getNombreDocuments();
            int nombreRoles = roleDAO.getNombreRoles();
            int nombrePermissions = permissionDAO.getNombrePermissions();

            request.setAttribute("nombreUtilisateurs", nombreUtilisateurs);
            request.setAttribute("nombreDocuments", nombreDocuments);
            request.setAttribute("nombreRoles", nombreRoles);
            request.setAttribute("nombrePermissions", nombrePermissions);

            // Récupérer les activités récentes
            List<Activite> activitesRecentes = getActivitesRecentes();
            request.setAttribute("activitesRecentes", activitesRecentes);

            // CORRECTION : Utiliser base.jsp comme template principal
            request.setAttribute("title", "Tableau de Bord Administrateur");
            request.setAttribute("pageContent", "adminDashboard.jsp"); // Chemin relatif depuis views/
            
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors de l'accès à la base de données: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private List<Activite> getActivitesRecentes() {
        List<Activite> activites = new ArrayList<>();
        activites.add(new Activite("Nouvel utilisateur inscrit", "2025-01-26", "John Doe"));
        activites.add(new Activite("Document validé", "2025-01-26", "Officier Smith"));
        return activites;
    }
}