package sn.etatcivil.habilitation.domaine;

import java.time.Instant;
import java.util.List;

/** Compte d'une personne (citoyen ou agent), tel que l'enregistre Keycloak. */
public record Utilisateur(String id, String email, String prenom, String nom, boolean actif, Instant dateCreation,
                          List<ProfilResume> profils) {

    public String nomComplet() {
        String n = ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
        return n.isEmpty() ? email : n;
    }
}
