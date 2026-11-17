// Corrigé : redirections vers l'adresse déclarée par @WebServlet, messages encodés pour l'URL.
package EtatCivil.controllers;

import EtatCivil.dao.DocumentEtatCivilDAO;
import EtatCivil.dao.CommuneDAO;
import EtatCivil.modeles.DocumentEtatCivil;
import EtatCivil.modeles.Commune;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet(name = "GestionDocumentsServlet", value = "/gestionDocuments")
public class GestionDocumentsServlet extends HttpServlet {
    private DocumentEtatCivilDAO documentDAO;
    private CommuneDAO communeDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        Connection connection = (Connection) getServletContext().getAttribute("connexion");
        documentDAO = new DocumentEtatCivilDAO(connection);
        communeDAO = new CommuneDAO(connection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        try {
            if (action == null) {
                action = "liste";
            }
            
            switch (action) {
                case "ajouter":
                    afficherFormulaireAjout(request, response);
                    break;
                case "modifier":
                    afficherFormulaireModification(request, response);
                    break;
                case "supprimer":
                    supprimerDocument(request, response);
                    break;
                case "liste":
                default:
                    listerDocuments(request, response);
                    break;
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        try {
            if ("ajouter".equals(action)) {
                ajouterDocument(request, response);
            } else if ("modifier".equals(action)) {
                modifierDocument(request, response);
            } else {
                listerDocuments(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException("Erreur lors de l'accès à la base de données", e);
        }
    }

    private void listerDocuments(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
    	DocumentEtatCivilDAO dao = new DocumentEtatCivilDAO();

        List<DocumentEtatCivil> documents = dao.getAllDocuments();
        request.setAttribute("documents", documents);
        request.setAttribute("title", "Gestion des documents d'état civil");
        request.setAttribute("pageContent", "/WEB-INF/views/gestionDocuments.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireAjout(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Commune> communes = communeDAO.getAllCommunes();
        request.setAttribute("communes", communes);
        request.setAttribute("title", "Ajouter un document");
        request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierDocument.jsp");
        request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
    }

    private void afficherFormulaireModification(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        int numeroDocument = Integer.parseInt(request.getParameter("id"));
        DocumentEtatCivil document = documentDAO.getDocumentByNumero(numeroDocument);
        List<Commune> communes = communeDAO.getAllCommunes();
        
        if (document != null) {
            request.setAttribute("document", document);
            request.setAttribute("communes", communes);
            request.setAttribute("title", "Modifier le document");
            request.setAttribute("pageContent", "/WEB-INF/views/ajouterModifierDocument.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document non trouvé");
        }
    }

    private void ajouterDocument(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        DocumentEtatCivil document = new DocumentEtatCivil();

        try {
            document.setNumeroregistre(Integer.parseInt(request.getParameter("numeroregistre")));
            document.setNumerodocument(Integer.parseInt(request.getParameter("numerodocument")));
            document.setDateevenement(java.sql.Date.valueOf(request.getParameter("dateevenement")));
            document.setLieuevenement(request.getParameter("lieuevenement"));
            document.setDateenregistrement(java.sql.Date.valueOf(request.getParameter("dateenregistrement")));
            document.setStatut(request.getParameter("statut"));

            // Récupération des ids pour la table
            int idCommune = Integer.parseInt(request.getParameter("commune")); // select commune
            int idNature = Integer.parseInt(request.getParameter("typedocument")); // select nature
            document.setIdcommune(idCommune);
            document.setIdnature(idNature);

            // Infos audit
            document.setCreatedBy("admin"); // ou récupérer l'utilisateur connecté
            document.setDateCreation(new Timestamp(System.currentTimeMillis()));

            if (documentDAO.ajouterDocument(document)) {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&message=" + URLEncoder.encode("Document ajouté avec succès", StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Erreur lors de l'ajout du document", StandardCharsets.UTF_8));
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Paramètres invalides", StandardCharsets.UTF_8));
        }
    }


    private void modifierDocument(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        try {
            int idDocument = Integer.parseInt(request.getParameter("iddocument")); // hidden field
            DocumentEtatCivil document = documentDAO.getDocumentById(idDocument);
            if (document == null) {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Document introuvable", StandardCharsets.UTF_8));
                return;
            }

            document.setNumeroregistre(Integer.parseInt(request.getParameter("numeroregistre")));
            document.setNumerodocument(Integer.parseInt(request.getParameter("numerodocument")));
            document.setDateevenement(java.sql.Date.valueOf(request.getParameter("dateevenement")));
            document.setLieuevenement(request.getParameter("lieuevenement"));
            document.setDateenregistrement(java.sql.Date.valueOf(request.getParameter("dateenregistrement")));
            document.setStatut(request.getParameter("statut"));

            int idCommune = Integer.parseInt(request.getParameter("commune"));
            int idNature = Integer.parseInt(request.getParameter("typedocument"));
            document.setIdcommune(idCommune);
            document.setIdnature(idNature);

            document.setDateModification(new Timestamp(System.currentTimeMillis()));
            document.setCreatedBy("admin"); // ou l'utilisateur connecté

            if (documentDAO.mettreAJourDocument(document)) {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&message=" + URLEncoder.encode("Document modifié avec succès", StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Erreur lors de la modification du document", StandardCharsets.UTF_8));
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Paramètres invalides", StandardCharsets.UTF_8));
        }
    }


    private void supprimerDocument(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        try {
            int idDocument = Integer.parseInt(request.getParameter("id"));
            if (documentDAO.supprimerDocumentById(idDocument)) {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&message=" + URLEncoder.encode("Document supprimé avec succès", StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Erreur lors de la suppression du document", StandardCharsets.UTF_8));
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/gestionDocuments?action=liste&erreur=" + URLEncoder.encode("Paramètre invalide", StandardCharsets.UTF_8));
        }
    }

}