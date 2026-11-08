package EtatCivil.controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import EtatCivil.dao.ActeRegistreDAO;
import EtatCivil.modeles.ActeRegistre;
import EtatCivil.modeles.Compte;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/acteregistre")
public class ActeRegistreServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        ActeRegistreDAO dao = new ActeRegistreDAO();
        try {
            int total = dao.getStatistiques().getTotalDemandes();
            int enAttente = dao.getStatistiques().getEnAttente();
            int completes = dao.getStatistiques().getCompletes();
            int nonTrouves = dao.getStatistiques().getNonTrouves();

            request.setAttribute("totalActes", total);
            request.setAttribute("enAttente", enAttente);
            request.setAttribute("completes", completes);
            request.setAttribute("nonTrouves", nonTrouves);

            request.setAttribute("pageContent", "/WEB-INF/views/acteregistre.jsp");
            request.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(request, response);
        } finally {
            dao.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Compte compte = (Compte) session.getAttribute("compte");

        ActeRegistre acte = new ActeRegistre();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Champs communs à tous les actes
            acte.setTypeActe(request.getParameter("typeActe"));
            acte.setLieuActe(request.getParameter("lieuActe"));
            
            String dateActe = request.getParameter("dateActe");
            if (dateActe != null && !dateActe.isEmpty()) {
                acte.setDateActe(sdf.parse(dateActe));
            }
            
            acte.setObservations(request.getParameter("observations"));
            acte.setDateRenseignement(new Timestamp(System.currentTimeMillis()));
            acte.setIdAgent(compte != null ? compte.getIdUtilisateur() : 1);

            // Informations personnelles de base
            acte.setNom(request.getParameter("nom"));
            acte.setPrenom(request.getParameter("prenom"));
            
            String dateNaissance = request.getParameter("dateNaissance");
            if (dateNaissance != null && !dateNaissance.isEmpty()) {
                acte.setDateNaissance(sdf.parse(dateNaissance));
            }
            acte.setLieuNaissance(request.getParameter("lieuNaissance"));

            // Champs spécifiques selon le type d'acte
            String typeActe = acte.getTypeActe();
            if (typeActe != null) {
                switch (typeActe) {
                    case "Naissance":
                        // Informations parents
                        acte.setNomPere(request.getParameter("nomPere"));
                        acte.setPrenomPere(request.getParameter("prenomPere"));
                        String dateNaissancePere = request.getParameter("dateNaissancePere");
                        if (dateNaissancePere != null && !dateNaissancePere.isEmpty()) {
                            acte.setDateNaissancePere(sdf.parse(dateNaissancePere));
                        }
                        
                        acte.setNomMere(request.getParameter("nomMere"));
                        acte.setPrenomMere(request.getParameter("prenomMere"));
                        String dateNaissanceMere = request.getParameter("dateNaissanceMere");
                        if (dateNaissanceMere != null && !dateNaissanceMere.isEmpty()) {
                            acte.setDateNaissanceMere(sdf.parse(dateNaissanceMere));
                        }
                        break;

                    case "Mariage":
                    case "Divorce":
                        // Informations conjoint
                        acte.setNomConjoint(request.getParameter("nomConjoint"));
                        acte.setPrenomConjoint(request.getParameter("prenomConjoint"));
                        String dateNaissanceConjoint = request.getParameter("dateNaissanceConjoint");
                        if (dateNaissanceConjoint != null && !dateNaissanceConjoint.isEmpty()) {
                            acte.setDateNaissanceConjoint(sdf.parse(dateNaissanceConjoint));
                        }
                        
                        acte.setLieuMariage(request.getParameter("lieuMariage"));
                        acte.setTemoin1(request.getParameter("temoin1"));
                        acte.setTemoin2(request.getParameter("temoin2"));
                        break;

                    case "Décès":
                        String dateDeces = request.getParameter("dateDeces");
                        if (dateDeces != null && !dateDeces.isEmpty()) {
                            acte.setDateDeces(sdf.parse(dateDeces));
                        }
                        acte.setHeureDeces(request.getParameter("heureDeces"));
                        acte.setCauseDeces(request.getParameter("causeDeces"));
                        acte.setLieuDeces(request.getParameter("lieuDeces"));
                        break;
                }
            }

            // Informations du registre
            acte.setVolume(request.getParameter("volume"));
            acte.setFolio(request.getParameter("folio"));
            acte.setCentre(request.getParameter("centre"));

            ActeRegistreDAO dao = new ActeRegistreDAO();
            boolean ok = dao.creerNouvelActe(acte);

            session.setAttribute(ok ? "success" : "error",
                    ok ? "Acte enregistré avec succès" : "Erreur lors de la création de l'acte");

            response.sendRedirect(request.getContextPath() + "/acteregistre");

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Erreur : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/acteregistre");
        }
    }
}