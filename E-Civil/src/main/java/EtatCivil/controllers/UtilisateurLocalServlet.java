package EtatCivil.controllers;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.DocumentEtatCivilDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import EtatCivil.modeles.DocumentEtatCivil;
import EtatCivil.modeles.Utilisateur;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/utilisateurlocal") // UNE SEULE URL
public class UtilisateurLocalServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        // Vérification de connexion
        if (compte == null) {
            System.out.println("UtilisateurLocalServlet: Utilisateur non connecté - redirection");
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        System.out.println("UtilisateurLocalServlet: Utilisateur connecté - " + compte.getEmail());
        
        // Déclaration des DAOs
        DocumentEtatCivilDAO docDao = null;
        DemandeDocumentDAO demandeDao = null;
        
        try {
            // Vérification que getPersonne() n'est pas null
            Utilisateur utilisateur = compte.getPersonne();
            if (utilisateur == null) {
                System.out.println("UtilisateurLocalServlet: Personne null - déconnexion");
                session.removeAttribute("compte");
                response.sendRedirect(request.getContextPath() + "/connexion");
                return;
            }
            
            // Récupérer les documents disponibles
            docDao = new DocumentEtatCivilDAO();
            List<DocumentEtatCivil> documentsDisponibles = docDao.getAllDocuments();
            
            // Récupérer les demandes de l'utilisateur
            List<DemandeDocument> mesDemandes = null;
            int nombreDemandesEnCours = 0;
            int nombreDemandesValidees = 0;
            
            demandeDao = new DemandeDocumentDAO(); // INITIALISATION
            mesDemandes = demandeDao.getDemandesByUtilisateur(utilisateur.getIdutilisateur());
            nombreDemandesEnCours = demandeDao.getNombreDemandesEnCours(utilisateur.getIdutilisateur());
            nombreDemandesValidees = demandeDao.getNombreDemandesValidees(utilisateur.getIdutilisateur());
            
            // Passer les données à la JSP
            request.setAttribute("documentsDisponibles", documentsDisponibles);
            request.setAttribute("mesDemandes", mesDemandes);
            request.setAttribute("nombreDemandesEnCours", nombreDemandesEnCours);
            request.setAttribute("nombreDemandesValidees", nombreDemandesValidees);
            request.setAttribute("utilisateur", utilisateur);
            
            // Afficher la page
            request.setAttribute("pageContent", "/WEB-INF/views/utilisateurlocal.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors du chargement de la page: " + e.getMessage());
            request.setAttribute("pageContent", "/WEB-INF/views/utilisateurlocal.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } finally {
            // Fermeture des DAOs
            if (docDao != null) docDao.close();
            if (demandeDao != null) demandeDao.close();
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("POST - UtilisateurLocalServlet");
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String action = request.getParameter("action");
        if (action != null) {
            switch (action) {
                case "modifierParametres":
                    modifierParametres(request, response, compte);
                    break;
                default:
                    doGet(request, response);
            }
        } else {
            doGet(request, response);
        }
    }
    
    private void modifierParametres(HttpServletRequest request, HttpServletResponse response, Compte compte) 
            throws ServletException, IOException {
        
        String notifications = request.getParameter("notifications");
        String langue = request.getParameter("langue");
        
        try {
            request.getSession().setAttribute("notifications", "on".equals(notifications));
            request.getSession().setAttribute("langue", langue);
            
            request.setAttribute("success", "Paramètres mis à jour avec succès");
        } catch (Exception e) {
            request.setAttribute("error", "Erreur lors de la modification des paramètres");
        }
        
        doGet(request, response);
    }
}