//package EtatCivil.controllers;
//
//import EtatCivil.dao.RenseignementDAO;
//import EtatCivil.modeles.Compte;
//import EtatCivil.modeles.Renseignement;
//import EtatCivil.services.ServeurCentralClient.ApiStatistiques;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.List;
//
//@WebServlet("/remplir-registre")
//public class RemplirRegistreServlet extends HttpServlet {
//    
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        // Vérifier si l'utilisateur est chargé de renseignement
//        if (compte == null || !"chargé de renseignement".equals(compte.getRole())) {
//            System.out.println("❌ Accès refusé - Rôle: " + (compte != null ? compte.getRole() : "null"));
//            session.setAttribute("error", "Accès réservé aux chargés de renseignement");
//            response.sendRedirect(request.getContextPath() + "/connexion");
//            return;
//        }
//        
//        System.out.println("✅ Accès autorisé pour chargé de renseignement: " + compte.getEmail());
//        
//        RenseignementDAO renseignementDAO = null;
//        
//        try {
//            renseignementDAO = new RenseignementDAO();
//            
//            // Récupérer les paramètres de filtre
//            String typeActe = request.getParameter("typeActe");
//            String statut = request.getParameter("statut");
//            String dateDebut = request.getParameter("dateDebut");
//            String dateFin = request.getParameter("dateFin");
//            
//            // Récupérer les demandes à renseigner via API
//            List<Renseignement> demandes = renseignementDAO.getDemandesARenseigner(
//                typeActe, statut, dateDebut, dateFin);
//            
//            // Récupérer les statistiques via API
//            ApiStatistiques stats = renseignementDAO.getStatistiques();
//            
//            request.setAttribute("demandes", demandes);
//            request.setAttribute("totalDemandes", stats.getTotalDemandes());
//            request.setAttribute("enAttente", stats.getEnAttente());
//            request.setAttribute("completes", stats.getCompletes());
//            request.setAttribute("nonTrouves", stats.getNonTrouves());
//            request.setAttribute("pageContent", "/WEB-INF/views/remplir-registre.jsp");
//            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur lors du chargement des demandes: " + e.getMessage());
//            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
//        } finally {
//            if (renseignementDAO != null) renseignementDAO.close();
//        }
//    }
//    
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        if (compte == null || !"chargé_renseignement".equals(compte.getRole())) {
//            response.sendRedirect(request.getContextPath() + "/connexion");
//            return;
//        }
//        
//        String action = request.getParameter("action");
//        
//        RenseignementDAO renseignementDAO = null;
//        
//        try {
//            renseignementDAO = new RenseignementDAO();
//            
//            if ("nouvel_acte".equals(action)) {
//                // Créer un nouvel acte manuellement via API
//                boolean success = creerNouvelActe(request, compte, renseignementDAO);
//                
//                if (success) {
//                    session.setAttribute("success", "Nouvel acte créé avec succès dans le serveur central");
//                } else {
//                    session.setAttribute("error", "Erreur lors de la création de l'acte dans le serveur central");
//                }
//                
//            } else if ("renseigner".equals(action)) {
//                // Renseigner une demande existante via API
//                boolean success = renseignerDemandeExistante(request, compte, renseignementDAO);
//                
//                if (success) {
//                    session.setAttribute("success", "Demande renseignée avec succès dans le serveur central");
//                } else {
//                    session.setAttribute("error", "Erreur lors du renseignement de la demande dans le serveur central");
//                }
//            }
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur lors du traitement: " + e.getMessage());
//        } finally {
//            if (renseignementDAO != null) renseignementDAO.close();
//        }
//        
//        response.sendRedirect(request.getContextPath() + "/remplir-registre");
//    }
//    
//    private boolean creerNouvelActe(HttpServletRequest request, Compte compte, RenseignementDAO dao) throws Exception {
//        // Récupérer les données du formulaire
//        String typeActe = request.getParameter("type_acte");
//        String nom = request.getParameter("nom");
//        String prenom = request.getParameter("prenom");
//        String dateNaissance = request.getParameter("date_naissance");
//        String lieuNaissance = request.getParameter("lieu_naissance");
//        String nomPere = request.getParameter("nom_pere");
//        String prenomPere = request.getParameter("prenom_pere");
//        String nomMere = request.getParameter("nom_mere");
//        String prenomMere = request.getParameter("prenom_mere");
//        
//        // Informations du registre
//        String numeroActe = request.getParameter("numero_acte");
//        String volume = request.getParameter("volume");
//        String folio = request.getParameter("folio");
//        String annee = request.getParameter("annee");
//        String commune = request.getParameter("commune");
//        String centre = request.getParameter("centre");
//        String observations = request.getParameter("observations");
//        
//        // Créer l'objet renseignement
//        Renseignement renseignement = new Renseignement();
//        renseignement.setTypeActe(typeActe);
//        renseignement.setNom(nom);
//        renseignement.setPrenom(prenom);
//        renseignement.setDateNaissance(java.sql.Date.valueOf(dateNaissance));
//        renseignement.setLieuNaissance(lieuNaissance);
//        renseignement.setNomPere(nomPere);
//        renseignement.setPrenomPere(prenomPere);
//        renseignement.setNomMere(nomMere);
//        renseignement.setPrenomMere(prenomMere);
//        
//        // Informations du registre
//        renseignement.setNumeroActe(numeroActe);
//        renseignement.setVolume(volume);
//        renseignement.setFolio(folio);
//        renseignement.setAnnee(annee);
//        renseignement.setCommune(commune);
//        renseignement.setCentre(centre);
//        renseignement.setObservations(observations);
//        renseignement.setStatut("complete");
//        renseignement.setDateRenseignement(new Timestamp(System.currentTimeMillis()));
//        renseignement.setIdAgent(compte.getIdUtilisateur());
//        
//        // Créer dans le serveur central via API
//        return dao.creerNouvelActe(renseignement);
//    }
//    
//    private boolean renseignerDemandeExistante(HttpServletRequest request, Compte compte, RenseignementDAO dao) throws Exception {
//        String idRenseignementStr = request.getParameter("id_renseignement");
//        
//        if (idRenseignementStr == null) {
//            return false;
//        }
//        
//        int idRenseignement = Integer.parseInt(idRenseignementStr);
//        
//        // Récupérer les données du formulaire
//        String numeroActe = request.getParameter("numero_acte");
//        String volume = request.getParameter("volume");
//        String folio = request.getParameter("folio");
//        String annee = request.getParameter("annee");
//        String commune = request.getParameter("commune");
//        String centre = request.getParameter("centre");
//        String observations = request.getParameter("observations");
//        String statut = request.getParameter("statut");
//        
//        // Créer l'objet renseignement
//        Renseignement renseignement = new Renseignement();
//        renseignement.setIdRenseignement(idRenseignement);
//        renseignement.setNumeroActe(numeroActe);
//        renseignement.setVolume(volume);
//        renseignement.setFolio(folio);
//        renseignement.setAnnee(annee);
//        renseignement.setCommune(commune);
//        renseignement.setCentre(centre);
//        renseignement.setObservations(observations);
//        renseignement.setStatut(statut);
//        renseignement.setDateRenseignement(new Timestamp(System.currentTimeMillis()));
//        renseignement.setIdAgent(compte.getIdUtilisateur());
//        
//        // Mettre à jour dans le serveur central via API
//        return dao.mettreAJourRenseignement(renseignement);
//    }
//}