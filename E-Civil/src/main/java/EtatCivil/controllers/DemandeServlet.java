package EtatCivil.controllers;

import EtatCivil.dao.NatureDAO;
import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.MotifDemandeDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import EtatCivil.services.EmailService;
import EtatCivil.services.GestionStatutService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@WebServlet("/demande")
public class DemandeServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        Compte compte = (Compte) session.getAttribute("compte");
        if (compte == null) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        NatureDAO natureDAO = null;
        MotifDemandeDAO motifDAO = null;

        try {
            String type = request.getParameter("type");
            String idStr = request.getParameter("id");
            
            if ("nature".equals(type) && idStr != null) {
                int natureId = Integer.parseInt(idStr);
                
                natureDAO = new NatureDAO();
                Object[] nature = natureDAO.getNatureByIdWithVariete(natureId);
                
                if (nature != null) {
                    // Récupération des motifs
                    motifDAO = new MotifDemandeDAO();
                    List<Object[]> motifs = motifDAO.getMotifsByNature(natureId);
                    
                    // Si aucun motif n'est disponible, permettre un motif personnalisé
                    if (motifs == null || motifs.isEmpty()) {
                        request.setAttribute("aucunMotifPredefini", true);
                    } else {
                        request.setAttribute("motifs", motifs);
                    }

                    request.setAttribute("nature", nature);
                    request.setAttribute("utilisateur", compte.getPersonne());
                    
                    request.setAttribute("pageContent", "/WEB-INF/views/demande.jsp");
                    request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
                } else {
                    session.setAttribute("error", "Type de document non trouvé");
                    response.sendRedirect(request.getContextPath() + "/documents");
                }
            } else {
                session.setAttribute("error", "Paramètres invalides");
                response.sendRedirect(request.getContextPath() + "/documents");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("error", "ID de nature invalide");
            response.sendRedirect(request.getContextPath() + "/documents");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur lors du chargement de la demande: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/documents");
        } finally {
            if (natureDAO != null) natureDAO.close();
            if (motifDAO != null) motifDAO.close();
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
        
        String action = request.getParameter("action");
        
        if ("creerDemande".equals(action)) {
            creerDemande(request, response, compte);
        } else {
            session.setAttribute("error", "Action non reconnue");
            response.sendRedirect(request.getContextPath() + "/documents");
        }
    }
    
    private void creerDemande(HttpServletRequest request, HttpServletResponse response, Compte compte) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        NatureDAO natureDAO = null;
        DemandeDocumentDAO demandeDAO = null;
        MotifDemandeDAO motifDAO = null;

        try {
            System.out.println("=== DÉBUT CRÉATION DEMANDE ===");

            // Récupération des paramètres de base
            String natureIdStr = request.getParameter("natureId");
            String format = request.getParameter("format");
            String motifIdStr = request.getParameter("motifId");
            String motifPersonnalise = request.getParameter("motifPersonnalise");
            String destinataire = request.getParameter("destinataire");
            
            // Récupération des informations de la personne concernée
            String prenom = request.getParameter("prenom");
            String nom = request.getParameter("nom");
            String dateNaissanceStr = request.getParameter("datedenaissance");
            String lieuNaissance = request.getParameter("lieudenaissance");
            String sexe = request.getParameter("sexe");
            String profession = request.getParameter("profession");
            String residance = request.getParameter("residance");
            String adresse = request.getParameter("adresse");
            String ville = request.getParameter("ville");
            
            System.out.println("=== ÉTAPE 1: PARAMÈTRES REÇUS ===");
            System.out.println("  natureId: " + natureIdStr);
            System.out.println("  format: " + format);
            System.out.println("  motifId: " + motifIdStr);
            System.out.println("  motifPersonnalise: " + motifPersonnalise);
            System.out.println("  destinataire: " + destinataire);
            System.out.println("  prenom: " + prenom);
            System.out.println("  nom: " + nom);
            System.out.println("  dateNaissance: " + dateNaissanceStr);
            System.out.println("  lieuNaissance: " + lieuNaissance);
            System.out.println("  sexe: " + sexe);
            System.out.println("  profession: " + profession);
            System.out.println("  residance: " + residance);
            System.out.println("  adresse: " + adresse);
            System.out.println("  ville: " + ville);
            
            // Validation des données de base
            if (natureIdStr == null || natureIdStr.isEmpty()) {
                System.out.println("❌ ERREUR: Nature ID non spécifié");
                session.setAttribute("error", "Type de document non spécifié");
                response.sendRedirect(request.getContextPath() + "/documents");
                return;
            }
            
            int natureId = Integer.parseInt(natureIdStr);
            int motifId = 0;
            System.out.println("✅ Nature ID parsé: " + natureId);

            // Gestion du motif
            System.out.println("=== ÉTAPE 2: GESTION MOTIF ===");
            if (motifIdStr != null && !motifIdStr.isEmpty() && !"0".equals(motifIdStr)) {
                System.out.println("🔹 Utilisation motif prédéfini: " + motifIdStr);
                motifId = Integer.parseInt(motifIdStr);
                motifDAO = new MotifDemandeDAO();
                Object[] motif = motifDAO.getMotifById(motifId);
                
                if (motif == null) {
                    System.out.println("❌ ERREUR: Motif non trouvé pour ID: " + motifId);
                    session.setAttribute("error", "Motif de demande non valide");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
                System.out.println("✅ Motif prédéfini trouvé: " + motif[1]);
            } else if (motifPersonnalise != null && !motifPersonnalise.trim().isEmpty()) {
                System.out.println("🔹 Création motif personnalisé: " + motifPersonnalise);
                // Créer un motif personnalisé
                motifDAO = new MotifDemandeDAO();
                motifId = motifDAO.creerMotifPersonnalise(natureId, motifPersonnalise.trim());
                if (motifId == 0) {
                    System.out.println("❌ ERREUR: Échec création motif personnalisé");
                    session.setAttribute("error", "Erreur lors de la création du motif personnalisé");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
                System.out.println("✅ Motif personnalisé créé avec ID: " + motifId);
            } else {
                System.out.println("❌ ERREUR: Aucun motif fourni");
                session.setAttribute("error", "Veuillez sélectionner un motif ou en préciser un");
                response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                return;
            }
            System.out.println("✅ Motif ID à utiliser: " + motifId);

            // Validation de la date de naissance
            System.out.println("=== ÉTAPE 3: VALIDATION DATE NAISSANCE ===");
            Date dateNaissance = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dateNaissance = sdf.parse(dateNaissanceStr);
                System.out.println("✅ Date de naissance parsée: " + dateNaissance);
                
                Date today = new Date();
                Calendar minDate = Calendar.getInstance();
                minDate.add(Calendar.YEAR, -120);
                
                if (dateNaissance.after(today)) {
                    System.out.println("❌ ERREUR: Date de naissance dans le futur");
                    session.setAttribute("error", "La date de naissance ne peut pas être dans le futur");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
                
                if (dateNaissance.before(minDate.getTime())) {
                    System.out.println("❌ ERREUR: Date de naissance trop ancienne");
                    session.setAttribute("error", "La date de naissance semble invraisemblable (plus de 120 ans)");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
                System.out.println("✅ Validation date naissance réussie");
            } catch (ParseException e) {
                System.out.println("❌ ERREUR: Format de date de naissance invalide");
                session.setAttribute("error", "Format de date de naissance invalide");
                response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                return;
            }
            
            // Validation des champs obligatoires de base
            System.out.println("=== ÉTAPE 4: VALIDATION CHAMPS OBLIGATOIRES ===");
            if (prenom == null || prenom.trim().isEmpty() || 
                nom == null || nom.trim().isEmpty() ||
                dateNaissanceStr == null || dateNaissanceStr.trim().isEmpty() ||
                lieuNaissance == null || lieuNaissance.trim().isEmpty() ||
                sexe == null || sexe.trim().isEmpty()) {
                
                System.out.println("❌ ERREUR: Champs obligatoires manquants");
                System.out.println("  prenom vide: " + (prenom == null || prenom.trim().isEmpty()));
                System.out.println("  nom vide: " + (nom == null || nom.trim().isEmpty()));
                System.out.println("  dateNaissance vide: " + (dateNaissanceStr == null || dateNaissanceStr.trim().isEmpty()));
                System.out.println("  lieuNaissance vide: " + (lieuNaissance == null || lieuNaissance.trim().isEmpty()));
                System.out.println("  sexe vide: " + (sexe == null || sexe.trim().isEmpty()));
                
                session.setAttribute("error", "Veuillez renseigner tous les champs obligatoires de la personne concernée");
                response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                return;
            }
            System.out.println("✅ Validation champs obligatoires réussie");
            
            // Vérifier que la nature existe et récupérer les booléens
            System.out.println("=== ÉTAPE 5: RÉCUPÉRATION NATURE ===");
            natureDAO = new NatureDAO();
            Object[] nature = natureDAO.getNatureByIdWithVariete(natureId);
            
            if (nature == null) {
                System.out.println("❌ ERREUR: Nature non trouvée pour ID: " + natureId);
                session.setAttribute("error", "Type de document non trouvé");
                response.sendRedirect(request.getContextPath() + "/documents");
                return;
            }
            
            System.out.println("✅ Nature récupérée: " + nature[1]);
            
            // Récupérer les booléens directement de nature
            boolean temoins = (boolean) nature[7];
            boolean parents = (boolean) nature[8];
            boolean conjoints = (boolean) nature[9];
            boolean medecin = (boolean) nature[10];
            boolean mentionMarginale = (boolean) nature[11];
            boolean causeDeces = (boolean) nature[12];
            
            System.out.println("=== VARIÉTÉS POUR CETTE NATURE ===");
            System.out.println("  temoins: " + temoins);
            System.out.println("  parents: " + parents);
            System.out.println("  conjoints: " + conjoints);
            System.out.println("  medecin: " + medecin);
            System.out.println("  mentionMarginale: " + mentionMarginale);
            System.out.println("  causeDeces: " + causeDeces);
            
            // Validation des variétés
            System.out.println("=== ÉTAPE 6: VALIDATION VARIÉTÉS ===");
            if (!validerVarietes(request, temoins, parents, conjoints, medecin, mentionMarginale, causeDeces)) {
                System.out.println("❌ ERREUR: Validation des variétés échouée");
                response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                return;
            }
            System.out.println("✅ Validation variétés réussie");
            
            // Validation de l'adresse de livraison pour le format physique
            System.out.println("=== ÉTAPE 7: VALIDATION LIVRAISON ===");
            if ("physique".equals(format)) {
                String adresseLivraison = request.getParameter("adresse");
                String villeLivraison = request.getParameter("ville");
                
                if (adresseLivraison == null || adresseLivraison.trim().isEmpty()) {
                    System.out.println("❌ ERREUR: Adresse de livraison manquante");
                    session.setAttribute("error", "L'adresse de livraison est obligatoire pour le format physique");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
                
                if (villeLivraison == null || villeLivraison.trim().isEmpty()) {
                    System.out.println("❌ ERREUR: Ville de livraison manquante");
                    session.setAttribute("error", "La ville de livraison est obligatoire pour le format physique");
                    response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
                    return;
                }
            }
            System.out.println("✅ Validation livraison réussie");
            
            // Créer l'objet demande
            System.out.println("=== ÉTAPE 8: CRÉATION OBJET DEMANDE ===");
            DemandeDocument demande = new DemandeDocument();
            demande.setIdDemande(0);
            demande.setDate(new java.util.Date());
            demande.setFormat(format != null ? format : "numerique");
            demande.setStatus("en attente");
            
            // Informations de la personne concernée
            demande.setPrenom(prenom.trim());
            demande.setNom(nom.trim());
            demande.setDatedenaissance(dateNaissance);
            demande.setLieudenaissance(lieuNaissance.trim());
            demande.setSexe(sexe);
            demande.setProfession(profession != null ? profession.trim() : null);
            demande.setResidance(residance != null ? residance.trim() : null);
            demande.setDestinataire("moi".equals(destinataire) ? "utilisateur" : "autre");
            
            // Informations de livraison si format physique
            if ("physique".equals(format)) {
                demande.setAdresseLivraison(adresse);
                demande.setVilleLivraison(ville);
            }
            
            System.out.println("✅ Objet DemandeDocument créé:");
            System.out.println("  - Prénom: " + demande.getPrenom());
            System.out.println("  - Nom: " + demande.getNom());
            System.out.println("  - Date naissance: " + demande.getDatedenaissance());
            System.out.println("  - Format: " + demande.getFormat());
            System.out.println("  - Statut: " + demande.getStatus());
            
            // Utiliser le DAO pour créer la demande avec toutes les variétés
            System.out.println("=== ÉTAPE 9: CRÉATION EN BASE ===");
            demandeDAO = new DemandeDocumentDAO();
            System.out.println("🔹 Avant appel creerDemandeAvecVarietes");
         // Avant d'appeler creerDemandeAvecVarietes
            System.out.println("=== DEBUG COMPTE ===");
            System.out.println("  compte.getIdPersonne(): " + compte.getIdPersonne());
            System.out.println("  compte email: " + compte.getEmail());
            System.out.println("  compte personne: " + compte.getPersonne());
         // Essayez ceci à la place :
            int idPersonne = compte.getPersonne().getId();
            System.out.println("  ID depuis personne: " + idPersonne);

            int idDemandeCree = demandeDAO.creerDemandeAvecVarietes(demande, idPersonne, natureId, motifId, request, nature);            
            System.out.println("🔹 ID demande créée: " + idDemandeCree);

            if (idDemandeCree > 0) {
                System.out.println("🎉 SUCCÈS: Demande créée avec ID: " + idDemandeCree);

                // Récupérer la demande créée avec toutes ses informations
                DemandeDocument demandeCree = demandeDAO.getDemandeById(idDemandeCree);
//                envoyerEmailConfirmation(demandeCree, compte);
                envoyerEmailConfirmationCreation(demandeCree, compte);
                

                if (demandeCree != null) {
                	
                    System.out.println("✅ Demande récupérée: " + demandeCree.getIdDemande());
                } else {
                    System.out.println("⚠️ ATTENTION: Impossible de récupérer la demande créée");
                }
                // LANCER AUTOMATIQUEMENT LA RECHERCHE SI LE STATUT EST "VALIDÉE"
//                if ("validée".equals(demandeCree.getStatus()) || "recherché".equals(demandeCree.getStatus())) {
//                    lancerRechercheAutomatique(idDemandeCree);
//                }
                // Préparer les données pour la page de confirmation
                session.setAttribute("demande", demandeCree);
                session.setAttribute("natureDocument", nature[1]); // Nom du document
                
                // Récupérer les variétés saisies pour affichage
                Map<String, String> varietesSaisies = new HashMap<>();
                if (temoins) {
                    varietesSaisies.put("Témoin 1", request.getParameter("temoin1_nom") + " " + request.getParameter("temoin1_prenom"));
                    if (request.getParameter("temoin2_nom") != null && !request.getParameter("temoin2_nom").isEmpty()) {
                        varietesSaisies.put("Témoin 2", request.getParameter("temoin2_nom") + " " + request.getParameter("temoin2_prenom"));
                    }
                }
                if (parents) {
                    varietesSaisies.put("Père", request.getParameter("pere_nom") + " " + request.getParameter("pere_prenom"));
                    varietesSaisies.put("Mère", request.getParameter("mere_nom") + " " + request.getParameter("mere_prenom"));
                }
                if (conjoints) {
                    varietesSaisies.put("Conjoint", request.getParameter("conjoint_nom") + " " + request.getParameter("conjoint_prenom"));
                }
                if (medecin) {
                    varietesSaisies.put("Médecin", request.getParameter("medecin_nom") + " " + request.getParameter("medecin_prenom"));
                }
                if (mentionMarginale) {
                    varietesSaisies.put("Mention marginale", request.getParameter("mention_marginale") + " - " + request.getParameter("mention_details"));
                }
                if (causeDeces) {
                    varietesSaisies.put("Cause décès", request.getParameter("cause_deces"));
                }
                
                session.setAttribute("varietes", varietesSaisies);
                
                // Rediriger vers la page de confirmation
                System.out.println("=== ÉTAPE 10: REDIRECTION VERS SUCCESS ===");
                System.out.println("🔀 Redirection vers: " + request.getContextPath() + "/success");
                response.sendRedirect(request.getContextPath() + "/success");
                
            } else {
                System.out.println("❌ ERREUR: Échec création demande, idDemandeCree = " + idDemandeCree);
                session.setAttribute("error", "Erreur lors de la création de la demande. Veuillez réessayer.");
                response.sendRedirect(request.getContextPath() + "/documents?type=nature&id=" + natureId);
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ ERREUR: NumberFormatException");
            e.printStackTrace();
            session.setAttribute("error", "ID de nature invalide");
            response.sendRedirect(request.getContextPath() + "/documents");
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION GÉNÉRIQUE:");
            e.printStackTrace();
            
            // Version détaillée avec StringWriter
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionAsString = sw.toString();
            System.out.println("Stack trace complète: " + exceptionAsString);
            
            session.setAttribute("error", "Une erreur technique s'est produite: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/documents");
        } finally {
        	// Juste après la récupération des paramètres
        	System.out.println("=== DEBUG: TOUS LES PARAMÈTRES ===");
        	Enumeration<String> paramNames = request.getParameterNames();
        	while (paramNames.hasMoreElements()) {
        	    String name = paramNames.nextElement();
        	    System.out.println("  " + name + " = " + request.getParameter(name));
        	}
            if (natureDAO != null) natureDAO.close();
            if (demandeDAO != null) demandeDAO.close();
            if (motifDAO != null) motifDAO.close();
        }
    }
    
    private boolean validerVarietes(HttpServletRequest request, boolean temoins, boolean parents, 
                                   boolean conjoints, boolean medecin, boolean mentionMarginale, 
                                   boolean causeDeces) {
        
        System.out.println("🔍 DÉBUT VALIDATION VARIÉTÉS");
        HttpSession session = request.getSession();
        
        // Validation des témoins
        if (temoins) {
            System.out.println("🔍 Validation témoins...");
            String temoin1Nom = request.getParameter("temoin1_nom");
            String temoin1Prenom = request.getParameter("temoin1_prenom");
            
            System.out.println("  temoin1_nom: " + temoin1Nom);
            System.out.println("  temoin1_prenom: " + temoin1Prenom);
            
            if (temoin1Nom == null || temoin1Nom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Nom témoin 1 manquant");
                session.setAttribute("error", "Le nom du témoin 1 est obligatoire pour ce document");
                return false;
            }
            
            if (temoin1Prenom == null || temoin1Prenom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Prénom témoin 1 manquant");
                session.setAttribute("error", "Le prénom du témoin 1 est obligatoire pour ce document");
                return false;
            }
        }
        
        // Validation des parents
        if (parents) {
            System.out.println("🔍 Validation parents...");
            String pereNom = request.getParameter("pere_nom");
            String perePrenom = request.getParameter("pere_prenom");
            String mereNom = request.getParameter("mere_nom");
            String merePrenom = request.getParameter("mere_prenom");
            
            System.out.println("  pere_nom: " + pereNom);
            System.out.println("  pere_prenom: " + perePrenom);
            System.out.println("  mere_nom: " + mereNom);
            System.out.println("  mere_prenom: " + merePrenom);
            
            if (pereNom == null || pereNom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Nom du père manquant");
                session.setAttribute("error", "Le nom du père est obligatoire pour ce document");
                return false;
            }
            
            if (perePrenom == null || perePrenom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Prénom du père manquant");
                session.setAttribute("error", "Le prénom du père est obligatoire pour ce document");
                return false;
            }
            
            if (mereNom == null || mereNom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Nom de la mère manquant");
                session.setAttribute("error", "Le nom de la mère est obligatoire pour ce document");
                return false;
            }
            
            if (merePrenom == null || merePrenom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Prénom de la mère manquant");
                session.setAttribute("error", "Le prénom de la mère est obligatoire pour ce document");
                return false;
            }
        }
        
        // Validation du conjoint
        if (conjoints) {
            System.out.println("🔍 Validation conjoint...");
            String conjointNom = request.getParameter("conjoint_nom");
            String conjointPrenom = request.getParameter("conjoint_prenom");
            String conjointDateNaissanceStr = request.getParameter("conjoint_date_naissance");
            String conjointLieuNaissance = request.getParameter("conjoint_lieu_naissance");
            
            System.out.println("  conjoint_nom: " + conjointNom);
            System.out.println("  conjoint_prenom: " + conjointPrenom);
            System.out.println("  conjoint_date_naissance: " + conjointDateNaissanceStr);
            System.out.println("  conjoint_lieu_naissance: " + conjointLieuNaissance);
            
            if (conjointNom == null || conjointNom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Nom du conjoint manquant");
                session.setAttribute("error", "Le nom du conjoint est obligatoire pour ce document");
                return false;
            }
            
            if (conjointPrenom == null || conjointPrenom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Prénom du conjoint manquant");
                session.setAttribute("error", "Le prénom du conjoint est obligatoire pour ce document");
                return false;
            }
            
            if (conjointDateNaissanceStr == null || conjointDateNaissanceStr.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Date de naissance du conjoint manquante");
                session.setAttribute("error", "La date de naissance du conjoint est obligatoire pour ce document");
                return false;
            }
            
            if (conjointLieuNaissance == null || conjointLieuNaissance.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Lieu de naissance du conjoint manquant");
                session.setAttribute("error", "Le lieu de naissance du conjoint est obligatoire pour ce document");
                return false;
            }
        }
        
        // Validation du médecin
        if (medecin) {
            System.out.println("🔍 Validation médecin...");
            String medecinNom = request.getParameter("medecin_nom");
            String medecinPrenom = request.getParameter("medecin_prenom");
            
            System.out.println("  medecin_nom: " + medecinNom);
            System.out.println("  medecin_prenom: " + medecinPrenom);
            
            if (medecinNom == null || medecinNom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Nom du médecin manquant");
                session.setAttribute("error", "Le nom du médecin est obligatoire pour ce document");
                return false;
            }
            
            if (medecinPrenom == null || medecinPrenom.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Prénom du médecin manquant");
                session.setAttribute("error", "Le prénom du médecin est obligatoire pour ce document");
                return false;
            }
        }
        
        // Validation de la mention marginale
        if (mentionMarginale) {
            System.out.println("🔍 Validation mention marginale...");
            String mentionType = request.getParameter("mention_marginale");
            String mentionDetails = request.getParameter("mention_details");
            
            System.out.println("  mention_marginale: " + mentionType);
            System.out.println("  mention_details: " + mentionDetails);
            
            if (mentionType == null || mentionType.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Type de mention marginale manquant");
                session.setAttribute("error", "Le type de mention marginale est obligatoire pour ce document");
                return false;
            }
            
            if (mentionDetails == null || mentionDetails.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Détails de la mention marginale manquants");
                session.setAttribute("error", "Les détails de la mention marginale sont obligatoires pour ce document");
                return false;
            }
        }
        
        // Validation de la cause de décès
        if (causeDeces) {
            System.out.println("🔍 Validation cause de décès...");
            String causeDecesValue = request.getParameter("cause_deces");
            String dateDecesStr = request.getParameter("date_deces");
            String lieuDeces = request.getParameter("lieu_deces");
            
            System.out.println("  cause_deces: " + causeDecesValue);
            System.out.println("  date_deces: " + dateDecesStr);
            System.out.println("  lieu_deces: " + lieuDeces);
            
            if (causeDecesValue == null || causeDecesValue.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Cause du décès manquante");
                session.setAttribute("error", "La cause du décès est obligatoire pour ce document");
                return false;
            }
            
            if (dateDecesStr == null || dateDecesStr.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Date du décès manquante");
                session.setAttribute("error", "La date du décès est obligatoire pour ce document");
                return false;
            }
            
            if (lieuDeces == null || lieuDeces.trim().isEmpty()) {
                System.out.println("❌ ERREUR VALIDATION: Lieu du décès manquant");
                session.setAttribute("error", "Le lieu du décès est obligatoire pour ce document");
                return false;
            }
        }
        
        System.out.println("✅ Validation variétés réussie");
        return true;
    }
    private void envoyerEmailConfirmation(DemandeDocument demande, Compte compte) {
        try {
            String to = compte.getEmail();
            String subject = "Confirmation de votre demande de document - #" + demande.getIdDemande();
            
            String body = "Bonjour " + compte.getPersonne().getPrenom() + " " + compte.getPersonne().getNom() + ",\n\n" +
                         "Votre demande de " + demande.getNatureDocument() + " a été créée avec succès.\n" +
                         "Référence: #" + demande.getIdDemande() + "\n" +
                         "Statut: En attente de validation\n" +
                         "Date de demande: " + new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()) + "\n\n" +
                         "Vous serez informé par email lorsque votre demande sera traitée par notre équipe.\n\n" +
                         "Cordialement,\nL'équipe de l'État Civil 2AD";
            
            // Utiliser votre service d'email
            boolean emailEnvoye = EmailService.envoyerEmailSimple(to, subject, body);
            
            if (emailEnvoye) {
                System.out.println("✅ Email de confirmation envoyé à: " + to);
            } else {
                System.out.println("❌ Échec envoi email de confirmation");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email confirmation: " + e.getMessage());
        }
    }
    private void envoyerEmailConfirmationCreation(DemandeDocument demande, Compte compte) {
        try {
            String to = compte.getEmail();
            String subject = "Confirmation de création de demande - #" + demande.getIdDemande();
            
            String body = "Bonjour " + compte.getPersonne().getPrenom() + " " + compte.getPersonne().getNom() + ",\n\n" +
                         "Votre demande de document a été créée avec succès.\n\n" +
                         "Détails de la demande:\n" +
                         "• Référence: #" + demande.getIdDemande() + "\n" +
                         "• Document: " + demande.getNatureDocument() + "\n" +
                         "• Format: " + (demande.getFormat().equals("numerique") ? "Numérique" : "Physique") + "\n" +
                         "• Statut: En attente de validation\n" +
                         "• Date de création: " + new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()) + "\n\n" +
                         "Vous recevrez un email lorsque votre demande sera traitée par notre équipe.\n\n" +
                         "Cordialement,\nL'équipe de l'État Civil";
            
            boolean emailEnvoye = EmailService.envoyerEmailSimple(to, subject, body);
            
            if (emailEnvoye) {
                System.out.println("✅ Email de confirmation de création envoyé à: " + to);
            } else {
                System.out.println("❌ Échec envoi email confirmation création");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email confirmation création: " + e.getMessage());
        }
    }
    
 // Dans DemandeServlet, ajoutez cette méthode privée
//    private void lancerRechercheAutomatique(int idDemande) {
//        try {
//            DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
//            GestionStatutService gestionStatutService = new GestionStatutService(demandeDAO);
//            
//            boolean succes = gestionStatutService.lancerRecherche(idDemande);
//            
//            if (succes) {
//                System.out.println("✅ Recherche automatique lancée pour demande: " + idDemande);
//            } else {
//                System.out.println("❌ Échec du lancement de la recherche automatique");
//            }
//            
//            demandeDAO.close();
//        } catch (Exception e) {
//            System.err.println("❌ ERREUR recherche automatique: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}