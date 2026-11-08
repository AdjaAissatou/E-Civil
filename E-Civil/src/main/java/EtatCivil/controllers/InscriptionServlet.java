package EtatCivil.controllers;

import EtatCivil.modeles.Compte;
import EtatCivil.modeles.Personne;
import EtatCivil.modeles.TypeUtilisateur;
import EtatCivil.modeles.Utilisateur;
import EtatCivil.services.InscriptionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/inscription")
public class InscriptionServlet extends HttpServlet {

    private final InscriptionService inscriptionService = new InscriptionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 1. Récupération des champs du formulaire
            String prenom = request.getParameter("prenom");
            String nom = request.getParameter("nom");
            String sexe = request.getParameter("sexe");
            String dateNaissanceStr = request.getParameter("datedenaissance");
            String lieuNaissance = request.getParameter("lieudenaissance");
            String profession = request.getParameter("profession");
            String residence = request.getParameter("residance");
            String email = request.getParameter("email");
            String password = request.getParameter("password"); // Champ existe dans JSP
            String confirmationPassword = request.getParameter("confirmationPassword"); // À ajouter dans JSP
            int numerocarte = Integer.parseInt(request.getParameter("numerocarte"));
            int telephone = Integer.parseInt(request.getParameter("telephone"));
            int nombredenfants = Integer.parseInt(request.getParameter("nombredenfants"));
            String localite = request.getParameter("localite");

            // Validation mot de passe
            if (!password.equals(confirmationPassword)) {
                request.setAttribute("erreur", "Les mots de passe ne correspondent pas");
                doGet(request, response);
                return;
            }

            Date dateNaissance = null;
            if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
                dateNaissance = new SimpleDateFormat("yyyy-MM-dd").parse(dateNaissanceStr);
            }

            // 2. Construire UN SEUL objet Utilisateur (qui hérite de Personne)
            Utilisateur utilisateur = new Utilisateur();
            
            // Attributs de Personne (hérités)
            utilisateur.setPrenom(prenom);
            utilisateur.setNom(nom);
            utilisateur.setSexe(sexe);
            utilisateur.setDatedenaissance(dateNaissance);
            utilisateur.setLieudenaissance(lieuNaissance);
            utilisateur.setProfession(profession);
            utilisateur.setResidance(residence);
            
            // Attributs spécifiques à Utilisateur
            utilisateur.setDatedinscription(new Date());
            utilisateur.setNumerocartedintentite_visa(numerocarte);
            utilisateur.setNumerotelephone(telephone);
            utilisateur.setNombredenfants(nombredenfants);

            // Associer le type (local ou diaspora)
            TypeUtilisateur typeUser = new TypeUtilisateur();
            typeUser.setNom(localite);
            typeUser.setPaysderesidence(localite.equals("local") ? "Sénégal" : "Étranger");
            utilisateur.getTypeUtilisateur().add(typeUser);

            // 3. Construire Compte
            Compte compte = new Compte();
            compte.setEmail(email);
            compte.setMotdepasse(password); // Serra hashé dans CompteDAO
            compte.setRole("utilisateur"); // rôle par défaut
            compte.setStatut(true);

            // 4. Appeler le service d'inscription
            boolean success = inscriptionService.inscrireUtilisateur(utilisateur, compte);

            if (success) {
                response.sendRedirect(request.getContextPath() + "/connexion?success=Inscription réussie");
            } else {
                request.setAttribute("erreur", "Échec de l'inscription.");
                doGet(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("erreur", "Une erreur est survenue: " + e.getMessage());
            doGet(request, response);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("title", "Inscription - E-Civil");
        req.setAttribute("pageContent", "/WEB-INF/views/inscription.jsp");
        req.getRequestDispatcher("/WEB-INF/views/base.jsp").forward(req, resp);
    }
}
