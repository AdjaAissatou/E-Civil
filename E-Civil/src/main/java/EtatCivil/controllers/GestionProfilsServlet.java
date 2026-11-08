package EtatCivil.controllers;

import EtatCivil.dao.ProfilDAO;
import EtatCivil.dao.RoleDAO;
import EtatCivil.modeles.Profil;
import EtatCivil.modeles.Role;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "GestionProfilsServlet", value = "/gestionProfils")
public class GestionProfilsServlet extends HttpServlet {
    private ProfilDAO profilDAO;
    private RoleDAO roleDAO;

    @Override
    public void init() throws ServletException {
        super.init();
//        Connection connection = (Connection) getServletContext().getAttribute("connexion");
        profilDAO = new ProfilDAO();
        roleDAO = new RoleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // Vérifier la session utilisateur
        HttpSession session = request.getSession();
        EtatCivil.modeles.Compte compte = (EtatCivil.modeles.Compte) session.getAttribute("compte");
        
        if (compte == null || !"admin".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }        
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
                    supprimerProfil(request, response);
                    break;
                case "liste":
                default:
                    listerProfils(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // Vérifier la session utilisateur
        HttpSession session = request.getSession();
        EtatCivil.modeles.Compte compte = (EtatCivil.modeles.Compte) session.getAttribute("compte");
        
        if (compte == null || !"admin".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }        
        try {
            if ("ajouter".equals(action)) {
                ajouterProfil(request, response);
            } else if ("modifier".equals(action)) {
                modifierProfil(request, response);
            } else {
                listerProfils(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    private void listerProfils(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Profil> profils = profilDAO.getAllProfils();
        request.setAttribute("profils", profils);
        request.setAttribute("title", "Gestion des profils");
        request.setAttribute("pageContent", "/WEB-INF/views/gestionProfils.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireAjout(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Role> roles = roleDAO.getAllRoles();
        request.setAttribute("roles", roles);
        request.setAttribute("title", "Ajouter un profil");
        request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierProfil.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireModification(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        int idProfil = Integer.parseInt(request.getParameter("id"));
        Profil profil = profilDAO.getProfilById(idProfil);
        List<Role> roles = roleDAO.getAllRoles();
        
        if (profil != null) {
            request.setAttribute("profil", profil);
            request.setAttribute("roles", roles);
            request.setAttribute("title", "Modifier le profil");
            request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierProfil.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profil non trouvé");
        }
    }

    private void ajouterProfil(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Profil profil = new Profil();
        
        // Récupération des paramètres du formulaire
        profil.setNom(request.getParameter("nom"));
        
        // Récupération du rôle
        int idRole = Integer.parseInt(request.getParameter("role"));
        Role role = roleDAO.getRoleById(idRole);
        profil.setRole(role);
        
        if (profilDAO.ajouterProfil(profil)) {
            response.sendRedirect("gestionProfils?message=Profil ajouté avec succès");
        } else {
            response.sendRedirect("gestionProfils?erreur=Erreur lors de l'ajout du profil");
        }
    }

    private void modifierProfil(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        Profil profil = new Profil();
        
        // Récupération des paramètres du formulaire
        profil.setIdProfil(Integer.parseInt(request.getParameter("idProfil")));
        profil.setNom(request.getParameter("nom"));
        
        // Récupération du rôle
        int idRole = Integer.parseInt(request.getParameter("role"));
        Role role = roleDAO.getRoleById(idRole);
        profil.setRole(role);
        
        if (profilDAO.mettreAJourProfil(profil)) {
            response.sendRedirect("gestionProfils?message=Profil modifié avec succès");
        } else {
            response.sendRedirect("gestionProfils?erreur=Erreur lors de la modification du profil");
        }
    }

    private void supprimerProfil(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        int idProfil = Integer.parseInt(request.getParameter("id"));
        
        if (profilDAO.supprimerProfil(idProfil)) {
            response.sendRedirect("gestionProfils?message=Profil supprimé avec succès");
        } else {
            response.sendRedirect("gestionProfils?erreur=Erreur lors de la suppression du profil");
        }
    }
    
}