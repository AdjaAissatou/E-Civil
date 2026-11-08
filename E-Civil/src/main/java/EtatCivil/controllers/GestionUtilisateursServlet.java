package EtatCivil.controllers;

import EtatCivil.dao.UtilisateurDAO;
import EtatCivil.modeles.Utilisateur;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GestionUtilisateursServlet", value = "/gestionUtilisateurs")
public class GestionUtilisateursServlet extends HttpServlet {
    private UtilisateurDAO utilisateurDAO;

    @Override
    public void init() throws ServletException {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Utilisateur> utilisateurs = utilisateurDAO.getTousUtilisateurs();
            request.setAttribute("utilisateurs", utilisateurs);
            
            // Gérer les messages de succès/erreur
            String success = (String) request.getSession().getAttribute("success");
            String error = (String) request.getSession().getAttribute("error");
            if (success != null) {
                request.setAttribute("success", success);
                request.getSession().removeAttribute("success");
            }
            if (error != null) {
                request.setAttribute("error", error);
                request.getSession().removeAttribute("error");
            }
            
            request.setAttribute("pageContent", "/WEB-INF/views/gestionUtilisateurs.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors du chargement des utilisateurs: " + e.getMessage());
            request.setAttribute("pageContent", "/WEB-INF/views/gestionUtilisateurs.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if ("changerRole".equals(action)) {
            changerRoleUtilisateur(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/gestionUtilisateurs");
        }
    }

    private void changerRoleUtilisateur(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idCompteStr = request.getParameter("idcompte");
        String nouveauRole = request.getParameter("role");
        
        HttpSession session = request.getSession();
        
        try {
            if (idCompteStr == null || nouveauRole == null) {
                session.setAttribute("error", "Paramètres manquants");
                response.sendRedirect(request.getContextPath() + "/gestionUtilisateurs");
                return;
            }
            
            int idCompte = Integer.parseInt(idCompteStr);
            boolean success = utilisateurDAO.mettreAJourRole(idCompte, nouveauRole);
            
            if (success) {
                session.setAttribute("success", "Rôle mis à jour avec succès !");
            } else {
                session.setAttribute("error", "Erreur lors de la mise à jour du rôle");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("error", "ID de compte invalide");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur technique lors de la mise à jour");
        }
        
        response.sendRedirect(request.getContextPath() + "/gestionUtilisateurs");
    }

    @Override
    public void destroy() {
        if (utilisateurDAO != null) {
            utilisateurDAO.close();
        }
    }
}