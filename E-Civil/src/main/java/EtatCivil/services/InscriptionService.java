package EtatCivil.services;

import EtatCivil.dao.CompteDAO;
import EtatCivil.dao.PersonneDAO;
import EtatCivil.dao.UtilisateurDAO;
import EtatCivil.dao.ConnexionBD;
import EtatCivil.modeles.Compte;
import EtatCivil.modeles.Personne;
import EtatCivil.modeles.Utilisateur;

import java.sql.Connection;

public class InscriptionService {

    private final PersonneDAO personneDAO = new PersonneDAO();
    private final CompteDAO compteDAO = new CompteDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Inscription complète d'un utilisateur
     */
    public boolean inscrireUtilisateur(Utilisateur utilisateur, Compte compte) {
        Connection conn = null;

        try {
            conn = ConnexionBD.getConnexion();
            conn.setAutoCommit(false);

            // 1. Enregistrer la personne (attributs hérités de Utilisateur)
            int idPersonne = personneDAO.enregistrer(utilisateur, conn);
            if (idPersonne <= 0) {
                conn.rollback();
                return false;
            }

            // 2. Enregistrer le compte
            int idCompte = compteDAO.creerCompte(compte,  idPersonne, utilisateur, conn);
            if (idCompte <= 0) {
                conn.rollback();
                return false;
            }

            // 3. Enregistrer l'utilisateur (attributs spécifiques + références)
            int idUtilisateur = utilisateurDAO.enregistrer(utilisateur, idPersonne, idCompte, conn);
            if (idUtilisateur <= 0) {
                conn.rollback();
                return false;
            }

            // ✅ Tout est OK
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}