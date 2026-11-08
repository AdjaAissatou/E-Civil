package EtatCivil.controllers;

import java.io.IOException;
import java.util.List;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.NatureDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/officier")
public class OfficierServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        // Vérifier si l'utilisateur est officier
        if (compte == null || !"officier".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = null;
        NatureDAO natureDAO = null;
        
        try {
            demandeDAO = new DemandeDocumentDAO();
            natureDAO = new NatureDAO();
            
            // Récupérer les paramètres de filtre
            String statut = request.getParameter("statut");
            String typeDocument = request.getParameter("typeDocument");
            String format = request.getParameter("format");
            String dateDemande = request.getParameter("dateDemande");
            
            // Dans la simulation, on considère "recherché" comme "trouvé"
            // Donc on montre par défaut les demandes "trouvé" et "recherché"
            if (statut == null || statut.isEmpty()) {
                statut = "trouvé"; // Par défaut montrer les actes à signer
            }
            
            // Récupérer les demandes filtrées
            List<DemandeDocument> demandes = demandeDAO.getDemandesFiltreesPourOfficier(statut, typeDocument, format, dateDemande);
            
            // DEBUG
            System.out.println("=== DEMANDES POUR OFFICIER ===");
            System.out.println("Filtre statut: " + statut);
            System.out.println("Nombre de demandes: " + demandes.size());
            
            // Récupérer les statistiques
            int totalDemandes = demandeDAO.getTotalDemandes();
            int demandesTrouvees = demandeDAO.getDemandesByStatusCount("trouvé") + 
                                  demandeDAO.getDemandesByStatusCount("recherché"); // On combine recherché et trouvé
            int demandesSignees = demandeDAO.getDemandesByStatusCount("signé");
            int demandesRejetees = demandeDAO.getDemandesByStatusCount("rejetée");
            
            // Récupérer les types de documents pour le filtre
            List<Object[]> natures = natureDAO.getAllNaturesWithVariete();
            
            // Définir les attributs pour la JSP
            request.setAttribute("demandes", demandes);
            request.setAttribute("totalDemandes", totalDemandes);
            request.setAttribute("demandesTrouvees", demandesTrouvees);
            request.setAttribute("demandesSignees", demandesSignees);
            request.setAttribute("demandesRejetees", demandesRejetees);
            request.setAttribute("natures", natures);
            
            // Utiliser le système de template avec base.jsp
            request.setAttribute("pageContent", "/WEB-INF/views/officier.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du chargement des demandes : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/connexion");
        } finally {
            if (demandeDAO != null) demandeDAO.close();
            if (natureDAO != null) natureDAO.close();
        }
    }
}
//package EtatCivil.controllers;
//
//import java.io.IOException;
//import java.util.List;
//
//import EtatCivil.dao.DemandeDocumentDAO;
//import EtatCivil.dao.NatureDAO;
//import EtatCivil.modeles.Compte;
//import EtatCivil.modeles.DemandeDocument;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//
//@WebServlet("/officier")
//public class OfficierServlet extends HttpServlet {
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        // Vérifier si l'utilisateur est officier
//        if (compte == null || !"officier".equals(compte.getRole())) {
//            response.sendRedirect(request.getContextPath() + "/connexion");
//            return;
//        }
//        
//        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
//        NatureDAO natureDAO = new NatureDAO();
//        
//        try {
//            // Récupérer les paramètres de filtre
//            String statut = request.getParameter("statut");
//            String typeDocument = request.getParameter("typeDocument");
//            String format = request.getParameter("format");
//            String dateDemande = request.getParameter("dateDemande");
//            
//            // DEBUG: Afficher les paramètres reçus
//            System.out.println("=== PARAMÈTRES FILTRES ===");
//            System.out.println("Statut: " + statut);
//            System.out.println("TypeDocument: " + typeDocument);
//            System.out.println("Format: " + format);
//            System.out.println("DateDemande: " + dateDemande);
//            
//            // Récupérer TOUTES les demandes d'abord pour debug
//            List<DemandeDocument> toutesLesDemandes = demandeDAO.getAllDemandes();
//            System.out.println("=== TOUTES LES DEMANDES (" + toutesLesDemandes.size() + ") ===");
//            for (DemandeDocument d : toutesLesDemandes) {
//                System.out.println("#" + d.getIdDemande() + " - " + d.getNatureDocument() + " - Statut: " + d.getStatus());
//            }
//            
//            // Récupérer les demandes filtrées
//            List<DemandeDocument> demandes = demandeDAO.getDemandesFiltreesPourOfficier(statut, typeDocument, format, dateDemande);
//            
//            // DEBUG: Afficher les résultats filtrés
//            System.out.println("=== DEMANDES FILTRÉES (" + demandes.size() + ") ===");
//            for (DemandeDocument d : demandes) {
//                System.out.println("#" + d.getIdDemande() + " - " + d.getNatureDocument() + " - Statut: " + d.getStatus());
//            }
//            
//            // Récupérer les statistiques
//            int totalDemandes = demandeDAO.getTotalDemandes();
//            int demandesEnAttente = demandeDAO.getDemandesByStatusCount("en attente");
//            int demandesTrouvees = demandeDAO.getDemandesByStatusCount("trouvé");
//            int demandesSignees = demandeDAO.getDemandesByStatusCount("signé");
//            int demandesRejetees = demandeDAO.getDemandesByStatusCount("rejetée");
//            int demandesTerminees = demandeDAO.getDemandesByStatusCount("terminée");
//            
//            // DEBUG: Afficher les statistiques
//            System.out.println("=== STATISTIQUES ===");
//            System.out.println("Total: " + totalDemandes);
//            System.out.println("En attente: " + demandesEnAttente);
//            System.out.println("Trouvées: " + demandesTrouvees);
//            System.out.println("Signées: " + demandesSignees);
//            System.out.println("Rejetées: " + demandesRejetees);
//            
//            // Récupérer les types de documents pour le filtre
//            List<Object[]> natures = natureDAO.getAllNaturesWithVariete();
//            
//            // Définir les attributs pour la JSP
//            request.setAttribute("demandes", demandes);
//            request.setAttribute("totalDemandes", totalDemandes);
//            request.setAttribute("demandesEnAttente", demandesEnAttente);
//            request.setAttribute("demandesTrouvees", demandesTrouvees);
//            request.setAttribute("demandesSignees", demandesSignees);
//            request.setAttribute("demandesRejetees", demandesRejetees);
//            request.setAttribute("demandesTerminees", demandesTerminees);
//            request.setAttribute("natures", natures);
//            
//            // Utiliser le système de template avec base.jsp
//            request.setAttribute("pageContent", "/WEB-INF/views/officier.jsp");
//            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur lors du chargement des demandes : " + e.getMessage());
//            response.sendRedirect(request.getContextPath() + "/connexion");
//        } finally {
//            demandeDAO.close();
//            natureDAO.close();
//        }
//    }
//}