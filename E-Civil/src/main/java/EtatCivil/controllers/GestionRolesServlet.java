package EtatCivil.controllers;

import EtatCivil.dao.RoleDAO;
import EtatCivil.modeles.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GestionRolesServlet", value = "/gestionRoles")
public class GestionRolesServlet extends HttpServlet {
    private RoleDAO roleDAO;

    @Override
    public void init() throws ServletException {
        super.init();
//        Connection connection = (Connection) getServletContext().getAttribute("connexion");
        roleDAO = new RoleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        try {
            if (action == null) {
                action = "liste";
            }
            
            switch (action) {
                case "ajouter":
                    afficherFormulaireAjout(request, response);
                    break;
                case "modifier":
                    afficherFormulaireModification(request, response);
                    break;
                case "supprimer":
                    supprimerRole(request, response);
                    break;
                case "liste":
                default:
                    listerRoles(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        try {
            if ("ajouter".equals(action)) {
                ajouterRole(request, response);
            } else if ("modifier".equals(action)) {
                modifierRole(request, response);
            } else {
                listerRoles(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    private void listerRoles(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
        request.setAttribute("roles", roles);
        request.setAttribute("title", "Gestion des rôles");
        request.setAttribute("pageContent", "/WEB-INF/views/gestionRoles.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireAjout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("title", "Ajouter un rôle");
        request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierRole.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireModification(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        int idRole = Integer.parseInt(request.getParameter("id"));
        Role role = roleDAO.getRoleById(idRole);
        
        if (role != null) {
            request.setAttribute("role", role);
            request.setAttribute("title", "Modifier le rôle");
            request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierRole.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Rôle non trouvé");
        }
    }

    private void ajouterRole(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Role role = new Role();
        
        // Récupération des paramètres du formulaire
        role.setNom(request.getParameter("nom"));
        role.setDescription(request.getParameter("description"));
        
        if (roleDAO.ajouterRole(role)) {
            response.sendRedirect("gestionRoles?message=Rôle ajouté avec succès");
        } else {
            response.sendRedirect("gestionRoles?erreur=Erreur lors de l'ajout du rôle");
        }
    }

    private void modifierRole(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Role role = new Role();
        
        // Récupération des paramètres du formulaire
        role.setIdrole(Integer.parseInt(request.getParameter("idrole")));
        role.setNom(request.getParameter("nom"));
        role.setDescription(request.getParameter("description"));
        
        if (roleDAO.mettreAJourRole(role)) {
            response.sendRedirect("gestionRoles?message=Rôle modifié avec succès");
        } else {
            response.sendRedirect("gestionRoles?erreur=Erreur lors de la modification du rôle");
        }
    }

    private void supprimerRole(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int idRole = Integer.parseInt(request.getParameter("id"));
        
        if (roleDAO.supprimerRole(idRole)) {
            response.sendRedirect("gestionRoles?message=Rôle supprimé avec succès");
        } else {
            response.sendRedirect("gestionRoles?erreur=Erreur lors de la suppression du rôle");
        }
    }
}