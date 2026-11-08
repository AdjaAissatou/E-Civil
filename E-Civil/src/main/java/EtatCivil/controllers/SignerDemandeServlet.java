package EtatCivil.controllers;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.modeles.DemandeDocument;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/signerDemande")
public class SignerDemandeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String demandeIdStr = request.getParameter("idDemande");
        
        if(demandeIdStr == null) {
            session.setAttribute("error", "Aucune demande spécifiée !");
            response.sendRedirect(request.getContextPath() + "/officier");
            return;
        }

        DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
        
        try {
            int demandeId = Integer.parseInt(demandeIdStr);
            
            // Récupérer la demande pour vérification
            DemandeDocument demande = demandeDAO.getDemandeById(demandeId);
            if(demande == null) {
                session.setAttribute("error", "Demande introuvable !");
                response.sendRedirect(request.getContextPath() + "/officier");
                return;
            }
            
            // Vérifier que la demande peut être signée (statut "trouvé" ou "validée")
            if(!"trouvé".equals(demande.getStatus()) && !"validée".equals(demande.getStatus())) {
                session.setAttribute("error", "Cette demande ne peut pas être signée. Statut actuel : " + demande.getStatus());
                response.sendRedirect(request.getContextPath() + "/officier");
                return;
            }
            
            // Marquer la demande comme signée
            boolean success = demandeDAO.marquerCommeSignee(demandeId);
            
            if(success) {
                session.setAttribute("success", "L'acte #" + demandeId + " a été signé électroniquement avec succès !");
                
                // Générer le PDF signé (optionnel - vous pouvez ajouter une vraie signature électronique ici)
                genererPDFSigne(demande);
                
            } else {
                session.setAttribute("error", "Erreur lors de la signature de l'acte !");
            }
            
            response.sendRedirect(request.getContextPath() + "/officier");
            
        } catch (NumberFormatException e) {
            session.setAttribute("error", "ID de demande invalide !");
            response.sendRedirect(request.getContextPath() + "/officier");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur technique lors de la signature : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/officier");
        } finally {
            demandeDAO.close();
        }
    }
    
    private void genererPDFSigne(DemandeDocument demande) {
        // Ici vous pouvez implémenter la génération du PDF avec signature électronique
        // Pour l'instant, on se contente de marquer la demande comme signée
        System.out.println("📄 PDF signé généré pour la demande #" + demande.getIdDemande());
        System.out.println("🖊️ Signé électroniquement le : " + new SimpleDateFormat("dd/MM/yyyy à HH:mm").format(new Date()));
    }
}