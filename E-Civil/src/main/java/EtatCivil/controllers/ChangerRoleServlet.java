package EtatCivil.controllers;

import EtatCivil.dao.CompteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/changerRole")
public class ChangerRoleServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idCompteStr = request.getParameter("idcompte");
        String nouveauRole = request.getParameter("role");

        if (idCompteStr == null || idCompteStr.isEmpty() || nouveauRole == null) {
            request.setAttribute("error", "Paramètres manquants");
            response.sendRedirect(request.getContextPath() + "/gestionUtilisateurs");
            return;
        }

        try {
            int idCompte = Integer.parseInt(idCompteStr);
            CompteDAO compteDAO = new CompteDAO();
            boolean success = compteDAO.updateRole(idCompte, nouveauRole);

            if (success) {
                request.getSession().setAttribute("success", "Rôle mis à jour avec succès");
            } else {
                request.getSession().setAttribute("error", "Erreur lors de la mise à jour du rôle");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "ID de compte invalide");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("error", "Erreur technique");
        }

        response.sendRedirect(request.getContextPath() + "/gestionUtilisateurs");
    }
}
