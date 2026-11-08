package EtatCivil.controllers;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.PaiementDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import EtatCivil.modeles.Paiement;
import EtatCivil.services.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

@WebServlet("/paiement")
public class PaiementServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String idDemandeStr = request.getParameter("iddemande");
        
        if (idDemandeStr == null) {
            session.setAttribute("error", "Paramètre de demande manquant");
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = null;
        
        try {
            int idDemande = Integer.parseInt(idDemandeStr);
            demandeDAO = new DemandeDocumentDAO();
            
            // Récupérer la demande avec vérification de propriété
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            
            if (demande == null) {
                session.setAttribute("error", "Demande non trouvée");
                response.sendRedirect(request.getContextPath() + "/mesdemandes");
                return;
            }
            
//            // Vérifier que la demande appartient à l'utilisateur via idutilisateur
//            if (demande.getId() != compte.getIdPersonne()) {
//                session.setAttribute("error", "Accès non autorisé à cette demande");
//                response.sendRedirect(request.getContextPath() + "/mesdemandes");
//                return;
//            }
            
            // Vérifier que le statut est "validée" pour permettre le paiement
            if (!"en cours".equals(demande.getStatus())) {
                session.setAttribute("error", "Cette demande n'est pas encore validée pour le paiement");
                response.sendRedirect(request.getContextPath() + "/mesdemandes");
                return;
            }
            
            // Vérifier si la demande a déjà un paiement
            if (demande.getIdpaiement() != null) { // Maintenant ça fonctionne car idpaiement est Integer
                session.setAttribute("error", "Cette demande a déjà été payée");
                response.sendRedirect(request.getContextPath() + "/mesdemandes");
                return;
            }
            
            // Récupérer les informations de tarif depuis la nature du document
            Map<String, Object> infosPaiement = demandeDAO.getInfosPaiement(idDemande);
            double montant = (Double) infosPaiement.get("tarifBase");
            
            request.setAttribute("demande", demande);
            request.setAttribute("montant", montant);
            request.setAttribute("pageContent", "/WEB-INF/views/paiement.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du chargement de la page de paiement: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
        } finally {
            if (demandeDAO != null) demandeDAO.close();
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        // DEBUG: Afficher tous les paramètres reçus
        System.out.println("=== DEBUG PARAMÈTRES REÇUS ===");
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            System.out.println("  " + paramName + " = " + paramValue);
        }
        
        String idDemandeStr = request.getParameter("iddemande");
        String methodePaiement = request.getParameter("methodePaiement");
        String referencePaiement = request.getParameter("referencePaiement");
        
        
        System.out.println("🔍 idDemandeStr: " + idDemandeStr);
        System.out.println("🔍 methodePaiement: " + methodePaiement);
        System.out.println("🔍 referencePaiement: " + referencePaiement);
        
        
        if (idDemandeStr == null || methodePaiement == null) {
            System.out.println("❌ ERREUR: Paramètres manquants - idDemandeStr: " + idDemandeStr + ", methodePaiement: " + methodePaiement);
            session.setAttribute("error", "Paramètres de paiement manquants");
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = null;
        PaiementDAO paiementDAO = null;
        
        try {
            int idDemande = Integer.parseInt(idDemandeStr);
            demandeDAO = new DemandeDocumentDAO();
            paiementDAO = new PaiementDAO();
            
            // Vérifier la demande et la propriété
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            
            if (demande == null) {
                session.setAttribute("error", "Demande non trouvée");
                response.sendRedirect(request.getContextPath() + "/mesdemandes");
                return;
            }
            
//            if (demande.getIdutilisateur() != compte.getIdPersonne()) {
//                session.setAttribute("error", "Accès non autorisé");
//                response.sendRedirect(request.getContextPath() + "/mesdemandes");
//                return;
//            }
            
            // Récupérer le montant depuis la nature du document
            Map<String, Object> infosPaiement = demandeDAO.getInfosPaiement(idDemande);
            double montant = (Double) infosPaiement.get("tarifBase");
            
            // Traiter le paiement
            boolean paiementReussi = traiterPaiement(methodePaiement, referencePaiement, montant);
            
            if (paiementReussi) {
                // Créer l'enregistrement de paiement
                Paiement paiement = new Paiement();
                paiement.setMontant(montant);
                paiement.setDatePaiement(new Date());
                paiement.setModePaiement(methodePaiement);
                paiement.setReference(referencePaiement);
                paiement.setStatus("payé");
                paiement.setRecu("RECU-" + System.currentTimeMillis());
                
                int idPaiement = paiementDAO.creerPaiement(paiement);
                
                if (idPaiement > 0) {
                    // Lier le paiement à la demande
                    boolean lienReussi = demandeDAO.lierPaiementADemande(idDemande, idPaiement);
                    
                    if (lienReussi) {
                        // Mettre à jour le statut de la demande
                        demandeDAO.updateStatus(idDemande, "recherché");
                        
                        // Envoyer email de confirmation
                        envoyerEmailConfirmationPaiement(demande, compte, paiement);
                        
                        session.setAttribute("success", "Paiement effectué avec succès! Votre demande est maintenant en cours de recherche.");
                        response.sendRedirect(request.getContextPath() + "/mesdemandes");
                    } else {
                        session.setAttribute("error", "Erreur lors de la liaison du paiement à la demande");
                        response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
                    }
                } else {
                    session.setAttribute("error", "Erreur lors de la création du paiement");
                    response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
                }
            } else {
                session.setAttribute("error", "Échec du paiement. Veuillez réessayer.");
                response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du traitement du paiement: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/mesdemandes");
        } finally {
            if (demandeDAO != null) demandeDAO.close();
            if (paiementDAO != null) paiementDAO.close();
        }
    }
    
    private boolean traiterPaiement(String methodePaiement, String referencePaiement, double montant) {
        // Simulation du traitement de paiement
        // En production, intégrez avec les APIs de Wave, Orange Money, etc.
        
        try {
            // Vérifications basiques
            if (referencePaiement == null || referencePaiement.trim().isEmpty()) {
                System.out.println("❌ Référence de paiement manquante");
                return false;
            }
            
            // Simulation d'un délai de traitement
            Thread.sleep(1000);
            
            // Log de simulation
            System.out.println("✅ Paiement simulé - Méthode: " + methodePaiement + 
                             ", Référence: " + referencePaiement + 
                             ", Montant: " + montant + " FCFA");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur traitement paiement: " + e.getMessage());
            return false;
        }
    }
    
    private void envoyerEmailConfirmationPaiement(DemandeDocument demande, Compte compte, Paiement paiement) {
        try {
            String to = compte.getEmail();
            String subject = "Confirmation de paiement - Demande #" + demande.getIdDemande();
            
            String body = "Bonjour " + compte.getPersonne().getPrenom() + " " + compte.getPersonne().getNom() + ",\n\n" +
                         "Nous avons bien reçu votre paiement pour la demande #" + demande.getIdDemande() + ".\n\n" +
                         "Détails du paiement:\n" +
                         "• Montant: " + paiement.getMontant() + " FCFA\n" +
                         "• Méthode: " + paiement.getModePaiement() + "\n" +
                         "• Référence: " + paiement.getReference() + "\n" +
                         "• Reçu: " + paiement.getRecu() + "\n" +
                         "• Date: " + paiement.getDatePaiement() + "\n\n" +
                         "Votre demande est maintenant en cours de recherche dans les registres.\n" +
                         "Vous serez informé par email dès que des résultats seront disponibles.\n\n" +
                         "Cordialement,\nL'équipe de l'État Civil";
            
            EmailService.envoyerEmailSimple(to, subject, body);
            System.out.println("✅ Email confirmation paiement envoyé à: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email confirmation paiement: " + e.getMessage());
        }
    }
}

////package EtatCivil.controllers;
////
////import EtatCivil.dao.DemandeDocumentDAO;
////import EtatCivil.dao.PaiementDAO;
////import EtatCivil.modeles.Compte;
////import EtatCivil.modeles.DemandeDocument;
////import EtatCivil.modeles.Paiement;
////import EtatCivil.services.EmailService;
////import jakarta.servlet.ServletException;
////import jakarta.servlet.annotation.WebServlet;
////import jakarta.servlet.http.HttpServlet;
////import jakarta.servlet.http.HttpServletRequest;
////import jakarta.servlet.http.HttpServletResponse;
////import jakarta.servlet.http.HttpSession;
////import java.io.IOException;
////import java.sql.SQLException;
////import java.util.Date;
////import java.util.Map;
////
////@WebServlet("/paiement")
////public class PaiementServlet extends HttpServlet {
////    
////    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
////            throws ServletException, IOException {
////        
////        HttpSession session = request.getSession();
////        Compte compte = (Compte) session.getAttribute("compte");
////        
////        if (compte == null) {
////            response.sendRedirect(request.getContextPath() + "/connexion");
////            return;
////        }
////        
////        String idDemandeStr = request.getParameter("iddemande");
////        
////        if (idDemandeStr == null) {
////            session.setAttribute("error", "Paramètre de demande manquant");
////            response.sendRedirect(request.getContextPath() + "/mesdemandes");
////            return;
////        }
////        
////        DemandeDocumentDAO demandeDAO = null;
////        
////        try {
////            int idDemande = Integer.parseInt(idDemandeStr);
////            demandeDAO = new DemandeDocumentDAO();
////            
////            // Récupérer la demande avec vérification de propriété
////            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
////            
////            if (demande == null) {
////                session.setAttribute("error", "Demande non trouvée");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
//////            // Vérifier que la demande appartient à l'utilisateur via idutilisateur
//////            if (demande.getId() != compte.getIdPersonne()) {
//////                session.setAttribute("error", "Accès non autorisé à cette demande");
//////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
//////                return;
//////            }
////            
////            // Vérifier que le statut est "validée" pour permettre le paiement
////            if (!"validée".equals(demande.getStatus())) {
////                session.setAttribute("error", "Cette demande n'est pas encore validée pour le paiement");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
////            // Vérifier si la demande a déjà un paiement
////            if (demande.getIdpaiement() != null) { // Maintenant ça fonctionne car idpaiement est Integer
////                session.setAttribute("error", "Cette demande a déjà été payée");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
////            // Récupérer les informations de tarif depuis la nature du document
////            Map<String, Object> infosPaiement = demandeDAO.getInfosPaiement(idDemande);
////            double montant = (Double) infosPaiement.get("tarifBase");
////            
////            request.setAttribute("demande", demande);
////            request.setAttribute("montant", montant);
////            request.setAttribute("pageContent", "/WEB-INF/views/paiement.jsp");
////            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
////            
////        } catch (Exception e) {
////            e.printStackTrace();
////            session.setAttribute("error", "Erreur lors du chargement de la page de paiement: " + e.getMessage());
////            response.sendRedirect(request.getContextPath() + "/mesdemandes");
////        } finally {
////            if (demandeDAO != null) demandeDAO.close();
////        }
////    }
////    
////    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
////            throws ServletException, IOException {
////        
////        HttpSession session = request.getSession();
////        Compte compte = (Compte) session.getAttribute("compte");
////        
////        if (compte == null) {
////            response.sendRedirect(request.getContextPath() + "/connexion");
////            return;
////        }
////        
////        String idDemandeStr = request.getParameter("iddemande");
////        String methodePaiement = request.getParameter("methodePaiement");
////        String referencePaiement = request.getParameter("referencePaiement");
////        
////        if (idDemandeStr == null || methodePaiement == null) {
////            session.setAttribute("error", "Paramètres de paiement manquants");
////            response.sendRedirect(request.getContextPath() + "/mesdemandes");
////            return;
////        }
////        
////        DemandeDocumentDAO demandeDAO = null;
////        PaiementDAO paiementDAO = null;
////        
////        try {
////            int idDemande = Integer.parseInt(idDemandeStr);
////            demandeDAO = new DemandeDocumentDAO();
////            paiementDAO = new PaiementDAO();
////            
////            // Vérifier la demande et la propriété
////            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
////            
////            if (demande == null) {
////                session.setAttribute("error", "Demande non trouvée");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
////            if (demande.getIdutilisateur() != compte.getIdPersonne()) {
////                session.setAttribute("error", "Accès non autorisé");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
////            // Récupérer le montant depuis la nature du document
////            Map<String, Object> infosPaiement = demandeDAO.getInfosPaiement(idDemande);
////            double montant = (Double) infosPaiement.get("tarifBase");
////            
////            // Traiter le paiement
////            boolean paiementReussi = traiterPaiement(methodePaiement, referencePaiement, montant);
////            
////            if (paiementReussi) {
////                // Créer l'enregistrement de paiement
////                Paiement paiement = new Paiement();
////                paiement.setMontant(montant);
////                paiement.setDatePaiement(new Date());
////                paiement.setModePaiement(methodePaiement);
////                paiement.setReference(referencePaiement);
////                paiement.setStatus("payé");
////                paiement.setRecu("RECU-" + System.currentTimeMillis());
////                
////                int idPaiement = paiementDAO.creerPaiement(paiement);
////                
////                if (idPaiement > 0) {
////                    // Lier le paiement à la demande
////                    boolean lienReussi = demandeDAO.lierPaiementADemande(idDemande, idPaiement);
////                    
////                    if (lienReussi) {
////                        // Mettre à jour le statut de la demande
////                        demandeDAO.updateStatus(idDemande, "recherché");
////                        
////                        // Envoyer email de confirmation
////                        envoyerEmailConfirmationPaiement(demande, compte, paiement);
////                        
////                        session.setAttribute("success", "Paiement effectué avec succès! Votre demande est maintenant en cours de recherche.");
////                        response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                    } else {
////                        session.setAttribute("error", "Erreur lors de la liaison du paiement à la demande");
////                        response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
////                    }
////                } else {
////                    session.setAttribute("error", "Erreur lors de la création du paiement");
////                    response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
////                }
////            } else {
////                session.setAttribute("error", "Échec du paiement. Veuillez réessayer.");
////                response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
////            }
////            
////        } catch (Exception e) {
////            e.printStackTrace();
////            session.setAttribute("error", "Erreur lors du traitement du paiement: " + e.getMessage());
////            response.sendRedirect(request.getContextPath() + "/mesdemandes");
////        } finally {
////            if (demandeDAO != null) demandeDAO.close();
////            if (paiementDAO != null) paiementDAO.close();
////        }
////    }
////    
////    private boolean traiterPaiement(String methodePaiement, String referencePaiement, double montant) {
////        // Simulation du traitement de paiement
////        // En production, intégrez avec les APIs de Wave, Orange Money, etc.
////        
////        try {
////            // Vérifications basiques
////            if (referencePaiement == null || referencePaiement.trim().isEmpty()) {
////                System.out.println("❌ Référence de paiement manquante");
////                return false;
////            }
////            
////            // Simulation d'un délai de traitement
////            Thread.sleep(1000);
////            
////            // Log de simulation
////            System.out.println("✅ Paiement simulé - Méthode: " + methodePaiement + 
////                             ", Référence: " + referencePaiement + 
////                             ", Montant: " + montant + " FCFA");
////            
////            return true;
////            
////        } catch (Exception e) {
////            System.err.println("❌ Erreur traitement paiement: " + e.getMessage());
////            return false;
////        }
////    }
////    
////    private void envoyerEmailConfirmationPaiement(DemandeDocument demande, Compte compte, Paiement paiement) {
////        try {
////            String to = compte.getEmail();
////            String subject = "Confirmation de paiement - Demande #" + demande.getIdDemande();
////            
////            String body = "Bonjour " + compte.getPersonne().getPrenom() + " " + compte.getPersonne().getNom() + ",\n\n" +
////                         "Nous avons bien reçu votre paiement pour la demande #" + demande.getIdDemande() + ".\n\n" +
////                         "Détails du paiement:\n" +
////                         "• Montant: " + paiement.getMontant() + " FCFA\n" +
////                         "• Méthode: " + paiement.getModePaiement() + "\n" +
////                         "• Référence: " + paiement.getReference() + "\n" +
////                         "• Reçu: " + paiement.getRecu() + "\n" +
////                         "• Date: " + paiement.getDatePaiement() + "\n\n" +
////                         "Votre demande est maintenant en cours de recherche dans les registres.\n" +
////                         "Vous serez informé par email dès que des résultats seront disponibles.\n\n" +
////                         "Cordialement,\nL'équipe de l'État Civil";
////            
////            EmailService.envoyerEmailSimple(to, subject, body);
////            System.out.println("✅ Email confirmation paiement envoyé à: " + to);
////            
////        } catch (Exception e) {
////            System.err.println("❌ Erreur envoi email confirmation paiement: " + e.getMessage());
////        }
////    }
////}
//// PaiementServlet.java - Version avec vrais paiements
//package EtatCivil.controllers;
//
//import EtatCivil.dao.DemandeDocumentDAO;
//import EtatCivil.dao.PaiementDAO;
//import EtatCivil.modeles.Compte;
//import EtatCivil.modeles.DemandeDocument;
//import EtatCivil.modeles.Paiement;
//import EtatCivil.services.EmailService;
//import EtatCivil.services.RealPaymentService;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.*;
//
//@WebServlet("/paiement")
//public class PaiementServlet extends HttpServlet {
//    
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        // ... [Le code doGet reste inchangé] ...
//        // Gardez votre code doGet existant
//    }
//    
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        if (compte == null) {
//            response.sendRedirect(request.getContextPath() + "/connexion");
//            return;
//        }
//        
//        String idDemandeStr = request.getParameter("iddemande");
//        String methodePaiement = request.getParameter("methodePaiement");
//        String referencePaiement = request.getParameter("referencePaiement");
//        String phoneNumber = request.getParameter("phoneNumber"); // Pour Wave/Orange Money
//        String stripeToken = request.getParameter("stripeToken"); // Pour Stripe
//        
//        if (idDemandeStr == null || methodePaiement == null) {
//            session.setAttribute("error", "Paramètres de paiement manquants");
//            response.sendRedirect(request.getContextPath() + "/mesdemandes");
//            return;
//        }
//        
//        DemandeDocumentDAO demandeDAO = null;
//        PaiementDAO paiementDAO = null;
//        
//        try {
//            int idDemande = Integer.parseInt(idDemandeStr);
//            demandeDAO = new DemandeDocumentDAO();
//            paiementDAO = new PaiementDAO();
//            
//            // Vérifier la demande et la propriété
//            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
//            
//            if (demande == null) {
//                session.setAttribute("error", "Demande non trouvée");
//                response.sendRedirect(request.getContextPath() + "/mesdemandes");
//                return;
//            }
//            
////            if (demande.getIdutilisateur() != compte.getIdPersonne()) {
////                session.setAttribute("error", "Accès non autorisé");
////                response.sendRedirect(request.getContextPath() + "/mesdemandes");
////                return;
////            }
////            
//            // Récupérer le montant depuis la nature du document
//            Map<String, Object> infosPaiement = demandeDAO.getInfosPaiement(idDemande);
//            double montant = (Double) infosPaiement.get("tarifBase");
//            
//            // Traiter le paiement avec le vrai service
//            Map<String, Object> paiementResult = processRealPayment(methodePaiement, montant, 
//                    referencePaiement, phoneNumber, stripeToken, idDemande, compte);
//            
//            if ((Boolean) paiementResult.get("success")) {
//                // Créer l'enregistrement de paiement
//                Paiement paiement = new Paiement();
//                paiement.setMontant(montant);
//                paiement.setDatePaiement(new Date());
//                paiement.setModePaiement(methodePaiement);
//                paiement.setReference(paiementResult.get("transaction_id").toString());
//                paiement.setStatus("payé");
//                paiement.setRecu("RECU-" + System.currentTimeMillis());
//                
//                int idPaiement = paiementDAO.creerPaiement(paiement);
//                
//                if (idPaiement > 0) {
//                    // Lier le paiement à la demande
//                    boolean lienReussi = demandeDAO.lierPaiementADemande(idDemande, idPaiement);
//                    
//                    if (lienReussi) {
//                        // Mettre à jour le statut de la demande
//                        demandeDAO.updateStatus(idDemande, "recherché");
//                        
//                        // Envoyer email de confirmation
//                        envoyerEmailConfirmationPaiement(demande, compte, paiement);
//                        
//                        session.setAttribute("success", "Paiement effectué avec succès! Votre demande est maintenant en cours de recherche.");
//                        response.sendRedirect(request.getContextPath() + "/mesdemandes");
//                    } else {
//                        session.setAttribute("error", "Erreur lors de la liaison du paiement à la demande");
//                        response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
//                    }
//                } else {
//                    session.setAttribute("error", "Erreur lors de la création du paiement");
//                    response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
//                }
//            } else {
//                session.setAttribute("error", "Échec du paiement: " + paiementResult.get("error"));
//                response.sendRedirect(request.getContextPath() + "/paiement?iddemande=" + idDemande);
//            }
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur lors du traitement du paiement: " + e.getMessage());
//            response.sendRedirect(request.getContextPath() + "/mesdemandes");
//        } finally {
//            if (demandeDAO != null) demandeDAO.close();
//            if (paiementDAO != null) paiementDAO.close();
//        }
//    }
//    
//    private Map<String, Object> processRealPayment(String methodePaiement, double montant, 
//            String reference, String phoneNumber, String stripeToken, 
//            int idDemande, Compte compte) {
//        
//        String description = "Paiement demande #" + idDemande + " - " + compte.getPersonne().getNom();
//        
//        switch (methodePaiement) {
//            case "wave":
//                return RealPaymentService.processWavePayment(montant, phoneNumber, reference);
//                
//            case "orange_money":
//                return RealPaymentService.processOrangeMoneyPayment(montant, phoneNumber, reference);
//                
//            case "carte_bancaire":
//                return RealPaymentService.processStripePayment(montant, stripeToken, description);
//                
//            default:
//                Map<String, Object> result = new HashMap<>();
//                result.put("success", false);
//                result.put("error", "Méthode de paiement non supportée");
//                return result;
//        }
//    }
//    
//    private void envoyerEmailConfirmationPaiement(DemandeDocument demande, Compte compte, Paiement paiement) {
//        try {
//            String to = compte.getEmail();
//            String subject = "Confirmation de paiement - Demande #" + demande.getIdDemande();
//            
//            String body = "Bonjour " + compte.getPersonne().getPrenom() + " " + compte.getPersonne().getNom() + ",\n\n" +
//                         "Nous avons bien reçu votre paiement pour la demande #" + demande.getIdDemande() + ".\n\n" +
//                         "Détails du paiement:\n" +
//                         "• Montant: " + paiement.getMontant() + " FCFA\n" +
//                         "• Méthode: " + paiement.getModePaiement() + "\n" +
//                         "• Référence: " + paiement.getReference() + "\n" +
//                         "• Reçu: " + paiement.getRecu() + "\n" +
//                         "• Date: " + paiement.getDatePaiement() + "\n\n" +
//                         "Votre demande est maintenant en cours de recherche dans les registres.\n" +
//                         "Vous serez informé par email dès que des résultats seront disponibles.\n\n" +
//                         "Cordialement,\nL'équipe de l'État Civil";
//            
//            EmailService.envoyerEmailSimple(to, subject, body);
//            System.out.println("✅ Email confirmation paiement envoyé à: " + to);
//            
//        } catch (Exception e) {
//            System.err.println("❌ Erreur envoi email confirmation paiement: " + e.getMessage());
//        }
//    }
//}
 