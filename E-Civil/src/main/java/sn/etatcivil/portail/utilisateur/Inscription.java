package sn.etatcivil.portail.utilisateur;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Formulaire d'inscription d'un citoyen (ou de création d'un agent par l'administrateur). */
public class Inscription {
    @NotBlank @Size(max = 150) private String prenom;
    @NotBlank @Size(max = 100) private String nom;
    @NotBlank @Email @Size(max = 150) private String email;
    @Pattern(regexp = "^$|^\\+?[0-9 ]{7,20}$", message = "numéro de téléphone invalide") private String telephone;
    @Pattern(regexp = "^$|[MF]") private String sexe;
    @Past @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateNaissance;
    @Size(max = 150) private String lieuNaissance;
    @Size(max = 255) private String adresse;
    @NotBlank @Size(min = 8, max = 72, message = "au moins 8 caractères") private String motDePasse;
    private String confirmation;
    private Role role = Role.CITOYEN;

    @AssertTrue(message = "les deux mots de passe ne correspondent pas")
    public boolean isConfirmationValide() {
        return motDePasse == null || motDePasse.equals(confirmation);
    }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getConfirmation() { return confirmation; }
    public void setConfirmation(String confirmation) { this.confirmation = confirmation; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
