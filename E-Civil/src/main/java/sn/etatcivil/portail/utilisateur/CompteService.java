package sn.etatcivil.portail.utilisateur;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.util.List;

@Service
@Transactional
public class CompteService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder encodeur;

    public CompteService(UtilisateurRepository repository, PasswordEncoder encodeur) {
        this.repository = repository;
        this.encodeur = encodeur;
    }

    /** Inscription publique : toujours un compte citoyen, quel que soit le rôle envoyé. */
    public Utilisateur inscrireCitoyen(Inscription f) {
        return creer(f, Role.CITOYEN);
    }

    /** Création d'un compte par l'administrateur (agents compris). */
    public Utilisateur creerCompte(Inscription f) {
        return creer(f, f.getRole() == null ? Role.CITOYEN : f.getRole());
    }

    public void changerRole(Long id, Role role, Utilisateur auteur) {
        Utilisateur u = trouver(id);
        if (u.getId().equals(auteur.getId())) {
            throw new ActionRefuseeException("Vous ne pouvez pas modifier votre propre rôle");
        }
        u.setRole(role);
    }

    public void changerActivation(Long id, boolean actif, Utilisateur auteur) {
        Utilisateur u = trouver(id);
        if (u.getId().equals(auteur.getId())) {
            throw new ActionRefuseeException("Vous ne pouvez pas désactiver votre propre compte");
        }
        u.setActif(actif);
    }

    public void mettreAJourProfil(Long id, String telephone, String adresse) {
        Utilisateur u = trouver(id);
        u.setTelephone(telephone);
        u.setAdresse(adresse);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> tous() {
        return repository.findAllByOrderByRoleAscNomAsc();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new ActionRefuseeException("Compte introuvable"));
    }

    private Utilisateur creer(Inscription f, Role role) {
        String email = f.getEmail().trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ActionRefuseeException("Un compte existe déjà avec cette adresse email");
        }
        Utilisateur u = new Utilisateur();
        u.setEmail(email);
        u.setMotDePasse(encodeur.encode(f.getMotDePasse()));
        u.setRole(role);
        u.setPrenom(f.getPrenom().trim());
        u.setNom(f.getNom().trim());
        u.setTelephone(vide(f.getTelephone()));
        u.setSexe(vide(f.getSexe()));
        u.setDateNaissance(f.getDateNaissance());
        u.setLieuNaissance(vide(f.getLieuNaissance()));
        u.setAdresse(vide(f.getAdresse()));
        return repository.save(u);
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
