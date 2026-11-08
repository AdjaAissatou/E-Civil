package EtatCivil.controllers;

import java.io.IOException;
import java.util.UUID;

import EtatCivil.dao.CompteDAO;
import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.dao.UtilisateurDAO;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.DemandeDocument;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.services.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/validerDemande")
public class ValiderDemandeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");
        
        if (compte == null || !"delegue".equals(compte.getRole())) {
            response.sendRedirect(request.getContextPath() + "/connexion");
            return;
        }
        
        String idDemandeStr = request.getParameter("id");
        String action = request.getParameter("action");
        
        if (idDemandeStr == null || action == null) {
            session.setAttribute("error", "Paramètres manquants");
            response.sendRedirect(request.getContextPath() + "/delegue");
            return;
        }
        
        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        CompteDAO compteDAO = new CompteDAO();
        
        try {
            int idDemande = Integer.parseInt(idDemandeStr);
            DemandeDocument demande = demandeDAO.getDemandeById(idDemande);
            
            if (demande == null) {
                session.setAttribute("error", "Demande non trouvée");
                response.sendRedirect(request.getContextPath() + "/delegue");
                return;
            }
            
            boolean success = false;
            String message = "";
            String nouveauStatut = "";
            
            switch (action) {
                case "valider":
                    nouveauStatut = "en cours";
                    success = demandeDAO.updateStatus(idDemande, nouveauStatut);
                    message = "Demande validée et mise en cours de traitement";
                    if (success) {
                        envoyerEmailPaiementEnLigne(demande);
                    }
                    break;
                    
                case "recherche":
                    nouveauStatut = "recherché";
                    success = demandeDAO.updateStatus(idDemande, nouveauStatut);
                    message = "Recherche lancée dans les registres";
                    if (success) {
                        envoyerEmailRecherche(demande);
                    }
                    break;
                    
                case "pret":
                    nouveauStatut = "prêt";
                    success = demandeDAO.updateStatus(idDemande, nouveauStatut);
                    message = "Document prêt pour retrait";
                    if (success) {
                        envoyerEmailDocumentPret(demande);
                    }
                    break;
                    
                case "terminee":
                    nouveauStatut = "terminée";
                    success = demandeDAO.updateStatus(idDemande, nouveauStatut);
                    message = "Demande terminée";
                    if (success) {
                        envoyerEmailDemandeTerminee(demande);
                    }
                    break;
                    
                case "rejeter":
                    nouveauStatut = "rejetée";
                    success = demandeDAO.updateStatus(idDemande, nouveauStatut);
                    message = "Demande rejetée";
                    if (success) {
                        envoyerEmailRejet(demande);
                    }
                    break;
                    
                default:
                    session.setAttribute("error", "Action non reconnue");
                    response.sendRedirect(request.getContextPath() + "/delegue");
                    return;
            }
            
            if (success) {
                session.setAttribute("success", message);
                System.out.println("✅ Statut mis à jour - Demande: " + idDemande + ", Nouveau statut: " + nouveauStatut);
            } else {
                session.setAttribute("error", "Erreur lors de l'opération");
                System.out.println("❌ Échec mise à jour statut - Demande: " + idDemande);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur technique: " + e.getMessage());
        } finally {
            demandeDAO.close();
            utilisateurDAO.close();
            compteDAO.close();
        }
        
        response.sendRedirect(request.getContextPath() + "/delegue");
    }
    
    private void envoyerEmailPaiementEnLigne(DemandeDocument demande) {
        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            // CORRECTION: Utiliser getUtilisateurByIdPersonne au lieu de getUtilisateurById
            Utilisateur demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
            Compte compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
            
            if (compteDemandeur != null && compteDemandeur.getEmail() != null && demandeur != null) {
                // Générer le lien de paiement en ligne
                String lienPaiement = genererLienPaiement(demande.getIdDemande());
                double montant = 5000.0; // À récupérer depuis la nature du document
                String delai = "24-48 heures";
                
                boolean emailEnvoye = EmailService.envoyerEmailPaiementEnLigne(
                    compteDemandeur.getEmail(),
                    demandeur.getPrenom() + " " + demandeur.getNom(),
                    demande.getIdDemande(),
                    demande.getNatureDocument(),
                    montant,
                    delai,
                    lienPaiement
                );
                
                if (emailEnvoye) {
                    System.out.println("✅ Email de paiement en ligne envoyé à: " + compteDemandeur.getEmail());
                } else {
                    System.out.println("❌ Échec envoi email paiement");
                }
            } else {
                System.out.println("❌ Impossible de récupérer l'email du demandeur");
                if (demandeur == null) {
                    System.out.println("  - Demandeur non trouvé pour ID personne: " + demande.getId());
                }
                if (compteDemandeur == null) {
                    System.out.println("  - Compte non trouvé pour ID personne: " + demande.getId());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void envoyerEmailRecherche(DemandeDocument demande) {
        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            Utilisateur demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
            Compte compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
            
            if (compteDemandeur != null && compteDemandeur.getEmail() != null && demandeur != null) {
                boolean emailEnvoye = EmailService.envoyerEmailRecherche(
                    compteDemandeur.getEmail(),
                    demandeur.getPrenom() + " " + demandeur.getNom(),
                    demande.getIdDemande(),
                    demande.getNatureDocument()
                );
                
                if (emailEnvoye) {
                    System.out.println("✅ Email de recherche envoyé à: " + compteDemandeur.getEmail());
                } else {
                    System.out.println("❌ Échec envoi email recherche");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email recherche: " + e.getMessage());
        }
    }
    
    private void envoyerEmailDocumentPret(DemandeDocument demande) {
        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            Utilisateur demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
            Compte compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
            
            if (compteDemandeur != null && compteDemandeur.getEmail() != null && demandeur != null) {
                boolean emailEnvoye = EmailService.envoyerEmailDocumentPret(
                    compteDemandeur.getEmail(),
                    demandeur.getPrenom() + " " + demandeur.getNom(),
                    demande.getIdDemande(),
                    demande.getNatureDocument()
                );
                
                if (emailEnvoye) {
                    System.out.println("✅ Email document prêt envoyé à: " + compteDemandeur.getEmail());
                } else {
                    System.out.println("❌ Échec envoi email document prêt");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email document prêt: " + e.getMessage());
        }
    }
    
    private void envoyerEmailDemandeTerminee(DemandeDocument demande) {
        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            Utilisateur demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
            Compte compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
            
            if (compteDemandeur != null && compteDemandeur.getEmail() != null && demandeur != null) {
                boolean emailEnvoye = EmailService.envoyerEmailDemandeTerminee(
                    compteDemandeur.getEmail(),
                    demandeur.getPrenom() + " " + demandeur.getNom(),
                    demande.getIdDemande(),
                    demande.getNatureDocument()
                );
                
                if (emailEnvoye) {
                    System.out.println("✅ Email demande terminée envoyé à: " + compteDemandeur.getEmail());
                } else {
                    System.out.println("❌ Échec envoi email demande terminée");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email demande terminée: " + e.getMessage());
        }
    }
    
    private void envoyerEmailRejet(DemandeDocument demande) {
        try {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            CompteDAO compteDAO = new CompteDAO();
            
            Utilisateur demandeur = utilisateurDAO.getUtilisateurById(demande.getId());
            Compte compteDemandeur = compteDAO.getCompteByIdPersonne(demande.getId());
            
            if (compteDemandeur != null && compteDemandeur.getEmail() != null && demandeur != null) {
                boolean emailEnvoye = EmailService.envoyerEmailRejet(
                    compteDemandeur.getEmail(),
                    demandeur.getPrenom() + " " + demandeur.getNom(),
                    demande.getIdDemande(),
                    demande.getNatureDocument()
                );
                
                if (emailEnvoye) {
                    System.out.println("✅ Email rejet envoyé à: " + compteDemandeur.getEmail());
                } else {
                    System.out.println("❌ Échec envoi email rejet");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email rejet: " + e.getMessage());
        }
    }
    
    private String genererLienPaiement(int idDemande) {
        String baseUrl = getServletContext().getInitParameter("app.baseUrl");
        if (baseUrl == null) {
            baseUrl = "http://localhost:8080" + getServletContext().getContextPath();
        }
        String token = genererTokenSecurise(idDemande);
        
        return baseUrl + "/paiement?demande=" + idDemande + "&token=" + token;
    }
    
    private String genererTokenSecurise(int idDemande) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}