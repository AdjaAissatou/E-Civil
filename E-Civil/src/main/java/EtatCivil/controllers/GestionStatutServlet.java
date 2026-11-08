//package EtatCivil.controllers;
//
//import EtatCivil.dao.DemandeDocumentDAO;
//import EtatCivil.services.GestionStatutService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import java.io.IOException;
//
//@WebServlet("/gestion-statut")
//public class GestionStatutServlet extends HttpServlet {
//    
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        DemandeDocumentDAO demandeDAO = null;
//        
//        try {
//            String action = request.getParameter("action");
//            String idDemandeStr = request.getParameter("idDemande");
//            String nouveauStatut = request.getParameter("statut");
//            String commentaire = request.getParameter("commentaire");
//            
//            if (idDemandeStr == null || idDemandeStr.isEmpty()) {
//                session.setAttribute("error", "ID de demande manquant");
//                response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
//                return;
//            }
//            
//            int idDemande = Integer.parseInt(idDemandeStr);
//            
//            demandeDAO = new DemandeDocumentDAO();
//            GestionStatutService gestionStatutService = new GestionStatutService(demandeDAO);
//            
//            boolean success = false;
//            
//            if ("changerStatut".equals(action) && nouveauStatut != null) {
//                success = gestionStatutService.mettreAJourStatut(idDemande, nouveauStatut, 
//                    commentaire != null ? commentaire : "Changement manuel de statut");
//                
//                if (success) {
//                    session.setAttribute("success", "Statut mis à jour avec succès");
//                } else {
//                    session.setAttribute("error", "Erreur lors de la mise à jour du statut");
//                }
//            } else if ("lancerRecherche".equals(action)) {
//                success = gestionStatutService.lancerRecherche(idDemande);
//                
//                if (success) {
//                    session.setAttribute("success", "Recherche lancée avec succès dans le serveur central");
//                } else {
//                    session.setAttribute("error", "Erreur lors du lancement de la recherche");
//                }
//            }
//            
//            // Rediriger vers la page de suivi
//            response.sendRedirect(request.getContextPath() + "/suividemande?iddemande=" + idDemande);
//            
//        } catch (NumberFormatException e) {
//            session.setAttribute("error", "ID de demande invalide");
//            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur technique: " + e.getMessage());
//            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
//        } finally {
//            if (demandeDAO != null) {
//                demandeDAO.close();
//            }
//        }
//    }
//}