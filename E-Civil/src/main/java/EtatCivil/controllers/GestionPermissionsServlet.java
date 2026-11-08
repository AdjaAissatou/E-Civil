package EtatCivil.controllers;

import EtatCivil.dao.PermissionDAO;
import EtatCivil.dao.RoleDAO;
import EtatCivil.modeles.Permision;
import EtatCivil.modeles.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GestionPermissionsServlet", value = "/gestionPermissions")
public class GestionPermissionsServlet extends HttpServlet {
    private PermissionDAO permissionDAO;
    private RoleDAO roleDAO;

    @Override
    public void init() throws ServletException {
        super.init();
//        Connection connection = (Connection) getServletContext().getAttribute("connexion");
        permissionDAO = new PermissionDAO();
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
                    supprimerPermission(request, response);
                    break;
                case "gerer-roles":
                    gererRolesPermission(request, response);
                    break;
                case "liste":
                default:
                    listerPermissions(request, response);
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
                ajouterPermission(request, response);
            } else if ("modifier".equals(action)) {
                modifierPermission(request, response);
            } else if ("ajouter-role".equals(action)) {
                ajouterRolePermission(request, response);
            } else if ("supprimer-role".equals(action)) {
                supprimerRolePermission(request, response);
            } else {
                listerPermissions(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    private void listerPermissions(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Permision> permissions = permissionDAO.getAllPermissions();
        request.setAttribute("permissions", permissions);
        request.setAttribute("title", "Gestion des permissions");
        request.setAttribute("pageContent", "/WEB-INF/views/gestionPermissions.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireAjout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("title", "Ajouter une permission");
        request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierPermission.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireModification(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        int idPermission = Integer.parseInt(request.getParameter("id"));
        Permision permission = permissionDAO.getPermissionById(idPermission);
        
        if (permission != null) {
            request.setAttribute("permission", permission);
            request.setAttribute("title", "Modifier la permission");
            request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierPermission.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Permission non trouvé");
        }
    }

    private void gererRolesPermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        int idPermission = Integer.parseInt(request.getParameter("id"));
        Permision permission = permissionDAO.getPermissionById(idPermission);
        List<Role> roles = roleDAO.getAllRoles();
        List<Role> rolesAssocies = permissionDAO.getRolesByPermission(idPermission); // Correction ici
        
        if (permission != null) {
            request.setAttribute("permission", permission);
            request.setAttribute("roles", roles);
            request.setAttribute("rolesAssocies", rolesAssocies);
            request.setAttribute("title", "Gérer les rôles pour la permission");
            request.setAttribute("pageContent", "/WEB-INF/views/gererRolesPermission.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Permission non trouvé");
        }
    }

    private void ajouterPermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Permision permission = new Permision();
        
        // Récupération des paramètres du formulaire
        permission.setNom(request.getParameter("nom"));
        permission.setDescription(request.getParameter("description"));
        permission.setAvantage(request.getParameter("avantage"));
        permission.setInterdiction(request.getParameter("interdiction"));
        
        if (permissionDAO.ajouterPermission(permission)) {
            response.sendRedirect("gestion-permissions?message=Permission ajoutée avec succès");
        } else {
            response.sendRedirect("gestion-permissions?erreur=Erreur lors de l'ajout de la permission");
        }
    }

    private void modifierPermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Permision permission = new Permision();
        
        // Récupération des paramètres du formulaire
        permission.setIdpermission(Integer.parseInt(request.getParameter("idpermission")));
        permission.setNom(request.getParameter("nom"));
        permission.setDescription(request.getParameter("description"));
        permission.setAvantage(request.getParameter("avantage"));
        permission.setInterdiction(request.getParameter("interdiction"));
        
        if (permissionDAO.mettreAJourPermission(permission)) {
            response.sendRedirect("gestion-permissions?message=Permission modifiée avec succès");
        } else {
            response.sendRedirect("gestion-permissions?erreur=Erreur lors de la modification de la permission");
        }
    }

    private void ajouterRolePermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int idPermission = Integer.parseInt(request.getParameter("idPermission"));
        int idRole = Integer.parseInt(request.getParameter("role"));
        
        if (permissionDAO.ajouterRolePermission(idRole, idPermission)) {
            response.sendRedirect("gestion-permissions?action=gerer-roles&id=" + idPermission + "&message=Rôle ajouté avec succès");
        } else {
            response.sendRedirect("gestion-permissions?action=gerer-roles&id=" + idPermission + "&erreur=Erreur lors de l'ajout du rôle");
        }
    }

    private void supprimerRolePermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int idPermission = Integer.parseInt(request.getParameter("idPermission"));
        int idRole = Integer.parseInt(request.getParameter("role"));
        
        if (permissionDAO.supprimerRolePermission(idRole, idPermission)) {
            response.sendRedirect("gestion-permissions?action=gerer-roles&id=" + idPermission + "&message=Rôle supprimé avec succès");
        } else {
            response.sendRedirect("gestion-permissions?action=gerer-roles&id=" + idPermission + "&erreur=Erreur lors de la suppression du rôle");
        }
    }

    private void supprimerPermission(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int idPermission = Integer.parseInt(request.getParameter("id"));
        
        if (permissionDAO.supprimerPermission(idPermission)) {
            response.sendRedirect("gestion-permissions?message=Permission supprimée avec succès");
        } else {
            response.sendRedirect("gestion-permissions?erreur=Erreur lors de la suppression de la permission");
        }
    }
}