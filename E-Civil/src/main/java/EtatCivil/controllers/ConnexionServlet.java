package EtatCivil.controllers;

import EtatCivil.dao.CompteDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.TypeUtilisateur;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.utils.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/connexion")
public class ConnexionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String motdepasse = req.getParameter("motdepasse");
        System.out.println("═══════════════════════════════════════");
        System.out.println("🎯 TENTATIVE DE CONNEXION APRES NETTOYAGE");
        System.out.println("═══════════════════════════════════════");
        System.out.println("📧 Email saisi: " + email);
        System.out.println("🔑 Mot de passe saisi: " + motdepasse);
        CompteDAO dao = new CompteDAO();
        Compte compte = dao.seConnecter(email);

        // Vérification login
        if (compte != null) {
        	System.out.println("✅ COMPTE TROUVÉ DANS LA BASE");
            System.out.println("📝 Email du compte: " + compte.getEmail());
            System.out.println("🆔 ID Compte: " + compte.getIdcompte());
            System.out.println("🔐 Hash BCrypt en base: " + compte.getMotdepasse());
            System.out.println("📏 Longueur du hash: " + compte.getMotdepasse().length());
            System.out.println("🏷️  Rôle: " + compte.getRole());
            System.out.println("🔓 Statut: " + compte.getStatut());
            

            boolean passwordCorrect = PasswordUtil.checkPassword(motdepasse, compte.getMotdepasse());
            System.out.println("Résultat de la vérification BCrypt: " + passwordCorrect);
            System.out.println("Résultat de la vérification: " + passwordCorrect);
            if (passwordCorrect) {
            HttpSession session = req.getSession();
            session.setAttribute("compte", compte);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // DIAGNOSTIC
            System.out.println("=== SESSION CRÉÉE ===");
            System.out.println("Compte stocké: " + compte.getEmail());
            System.out.println("Personne via getPersonne(): " + 
                (compte.getPersonne() != null ? "PRÉSENTE" : "ABSENTE"));
         

            String role = compte.getRole().toLowerCase();
            System.out.println("🔍 Rôle récupéré: '" + role + "'");

            String page = "/WEB-INF/views/accueil.jsp"; 
           
            if ("admin".equals(role)) page = "/WEB-INF/views/adminDashboard.jsp";
            else if ("delegue".equals(role)) page = "/WEB-INF/views/delegue.jsp";
            else if ("officier".equals(role)) page = "/WEB-INF/views/officier.jsp";
            else if ("chargé de renseignement".equals(role)) page = "/WEB-INF/views/acteregistre.jsp";
            else page = "/WEB-INF/views/utilisateurlocal.jsp";

                
            

            req.setAttribute("pageContent", page);
            req.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(req, resp);
        } else {
            req.setAttribute("erreur", "Email ou mot de passe incorrect.");
            req.setAttribute("pageContent", "/WEB-INF/views/connexion.jsp");
            req.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(req, resp);
        }
    }
  }
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("title", "Connexion - E-Civil");
        req.setAttribute("pageContent", "/WEB-INF/views/connexion.jsp");
        req.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(req, resp);
    }
}
