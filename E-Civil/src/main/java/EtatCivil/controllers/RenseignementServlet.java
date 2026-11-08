//package EtatCivil.controllers;
//
//import EtatCivil.dao.RenseignementDAO;
//import EtatCivil.modeles.Compte;
//import EtatCivil.modeles.Renseignement;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.Enumeration;
//
//@WebServlet("/renseignement")
//public class RenseignementServlet extends HttpServlet {
//    
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        
//        RenseignementDAO renseignementDAO = null;
//        
//        try {
//            renseignementDAO = new RenseignementDAO();
//            
//            // Récupérer les statistiques seulement
//            System.out.println("📊 Récupération des statistiques...");
//            int totalRenseignements = renseignementDAO.getTotalRenseignements();
//            int enAttente = renseignementDAO.getRenseignementsByStatutCount("en_attente");
//            int completes = renseignementDAO.getRenseignementsByStatutCount("complete");
//            int nonTrouves = renseignementDAO.getRenseignementsByStatutCount("non_trouve");
//            
//            System.out.println("📈 Statistiques récupérées - Total: " + totalRenseignements + 
//                             ", En attente: " + enAttente + 
//                             ", Complets: " + completes + 
//                             ", Non trouvés: " + nonTrouves);
//            
//            request.setAttribute("totalRenseignements", totalRenseignements);
//            request.setAttribute("enAttente", enAttente);
//            request.setAttribute("completes", completes);
//            request.setAttribute("nonTrouves", nonTrouves);
//            request.setAttribute("pageContent", "/WEB-INF/views/renseignement.jsp");
//            
//            System.out.println("✅ GET /renseignement - Succès");
//            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
//            
//        } catch (Exception e) {
//            System.err.println("❌ ERREUR GET /renseignement: " + e.getMessage());
//            e.printStackTrace();
//            session.setAttribute("error", "Erreur lors du chargement des statistiques: " + e.getMessage());
//            response.sendRedirect(request.getContextPath() + "/utilisateurlocal");
//        } finally {
//            if (renseignementDAO != null) {
//                renseignementDAO.close();
//                System.out.println("🔒 DAO fermé");
//            }
//        }
//    }
//    
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
//            throws ServletException, IOException {
//        
//        HttpSession session = request.getSession();
//        Compte compte = (Compte) session.getAttribute("compte");
//        
//        System.out.println("🔍 POST /renseignement - Début - Action: " + request.getParameter("action"));
//        
//        String action = request.getParameter("action");
//        
//        RenseignementDAO renseignementDAO = null;
//        
//        try {
//            renseignementDAO = new RenseignementDAO();
//            
//            if ("nouvel_acte".equals(action)) {
//                System.out.println("📝 Traitement création nouvel acte...");
//                
//                // Logger tous les paramètres reçus
//                logParameters(request);
//                
//                // Récupération des paramètres
//                String typeActe = request.getParameter("type_acte");
//                String nom = request.getParameter("nom");
//                String prenom = request.getParameter("prenom");
//                String dateNaissanceStr = request.getParameter("date_naissance");
//                String lieuNaissance = request.getParameter("lieu_naissance");
//                String nomPere = request.getParameter("nom_pere");
//                String prenomPere = request.getParameter("prenom_pere");
//                String nomMere = request.getParameter("nom_mere");
//                String prenomMere = request.getParameter("prenom_mere");
//                String numeroActe = request.getParameter("numero_acte");
//                String volume = request.getParameter("volume");
//                String folio = request.getParameter("folio");
//                String annee = request.getParameter("annee");
//                String commune = request.getParameter("commune");
//                String centre = request.getParameter("centre");
//                String observations = request.getParameter("observations");
//                
//                System.out.println("📋 Paramètres reçus:");
//                System.out.println("   - Type: " + typeActe);
//                System.out.println("   - Nom: " + nom);
//                System.out.println("   - Prénom: " + prenom);
//                System.out.println("   - Date naissance: " + dateNaissanceStr);
//                System.out.println("   - Lieu naissance: " + lieuNaissance);
//                System.out.println("   - Numéro acte: " + numeroActe);
//                
//                // Validation des champs obligatoires
//                if (typeActe == null || typeActe.trim().isEmpty() ||
//                    nom == null || nom.trim().isEmpty() ||
//                    prenom == null || prenom.trim().isEmpty() ||
//                    dateNaissanceStr == null || dateNaissanceStr.trim().isEmpty()) {
//                    
//                    System.err.println("❌ Champs obligatoires manquants");
//                    session.setAttribute("error", "Veuillez remplir tous les champs obligatoires");
//                    response.sendRedirect(request.getContextPath() + "/renseignement");
//                    return;
//                }
//                
//                // Créer l'objet renseignement pour le nouvel acte
//                Renseignement nouvelActe = new Renseignement();
//                nouvelActe.setTypeActe(typeActe);
//                nouvelActe.setNom(nom);
//                nouvelActe.setPrenom(prenom);
//                nouvelActe.setLieuNaissance(lieuNaissance);
//                nouvelActe.setNomPere(nomPere);
//                nouvelActe.setPrenomPere(prenomPere);
//                nouvelActe.setNomMere(nomMere);
//                nouvelActe.setPrenomMere(prenomMere);
//                nouvelActe.setNumeroActe(numeroActe);
//                nouvelActe.setVolume(volume);
//                nouvelActe.setFolio(folio);
//                nouvelActe.setAnnee(annee);
//                nouvelActe.setCommune(commune);
//                nouvelActe.setCentre(centre);
//                nouvelActe.setObservations(observations);
//                nouvelActe.setDateRenseignement(new Timestamp(System.currentTimeMillis()));
//                nouvelActe.setIdAgent(compte != null ? compte.getIdUtilisateur() : 1); // ID par défaut si null
//                
//                // Convertir la date de naissance si elle existe
//                if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
//                    try {
//                        java.sql.Date dateNaissance = java.sql.Date.valueOf(dateNaissanceStr);
//                        nouvelActe.setDateNaissance(dateNaissance);
//                        System.out.println("📅 Date de naissance convertie: " + dateNaissance);
//                    } catch (IllegalArgumentException e) {
//                        System.err.println("❌ Format de date invalide: " + dateNaissanceStr);
//                        session.setAttribute("error", "Format de date de naissance invalide: " + dateNaissanceStr);
//                        response.sendRedirect(request.getContextPath() + "/renseignement");
//                        return;
//                    }
//                }
//                
//                System.out.println("🚀 Tentative de création d'acte via API...");
//                System.out.println("📦 Objet à envoyer: " + nouvelActe.toString());
//                
//                // Créer le nouvel acte via API dans le serveur central
//                boolean success = renseignementDAO.creerNouvelActe(nouvelActe);
//                
//                if (success) {
//                    System.out.println("✅ Nouvel acte créé avec succès!");
//                    session.setAttribute("success", "Nouvel acte créé avec succès dans le serveur central");
//                } else {
//                    System.err.println("❌ Échec de la création de l'acte via API");
//                    session.setAttribute("error", "Erreur lors de la création du nouvel acte - L'API a retourné une erreur");
//                }
//            } else {
//                System.err.println("❌ Action non reconnue: " + action);
//                session.setAttribute("error", "Action non reconnue: " + action);
//            }
//            
//        } catch (Exception e) {
//            System.err.println("💥 ERREUR CRITIQUE dans doPost:");
//            System.err.println("   Message: " + e.getMessage());
//            System.err.println("   Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
//            System.err.println("   StackTrace:");
//            e.printStackTrace();
//            
//            String errorMessage = "Erreur lors du traitement: ";
//            if (e.getMessage() != null) {
//                errorMessage += e.getMessage();
//            } else {
//                errorMessage += "Erreur inconnue - vérifiez les logs serveur";
//            }
//            
//            session.setAttribute("error", errorMessage);
//        } finally {
//            if (renseignementDAO != null) {
//                renseignementDAO.close();
//                System.out.println("🔒 DAO fermé dans finally");
//            }
//            System.out.println("🔚 POST /renseignement - Fin");
//        }
//        
//        response.sendRedirect(request.getContextPath() + "/renseignement");
//    }
//    
//    /**
//     * Méthode utilitaire pour logger tous les paramètres de la requête
//     */
//    private void logParameters(HttpServletRequest request) {
//        System.out.println("📨 Paramètres de la requête POST:");
//        Enumeration<String> parameterNames = request.getParameterNames();
//        while (parameterNames.hasMoreElements()) {
//            String paramName = parameterNames.nextElement();
//            String paramValue = request.getParameter(paramName);
//            System.out.println("   " + paramName + " = " + paramValue);
//        }
//    }
//}