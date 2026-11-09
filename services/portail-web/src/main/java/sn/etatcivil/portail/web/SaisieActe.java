package sn.etatcivil.portail.web;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import sn.etatcivil.portail.clients.Vues;

import java.time.LocalDate;

/** Formulaire de transcription d'un acte, envoyé tel quel à registre-service. */
public class SaisieActe {
    @NotBlank private String typeActe;
    @NotBlank @Size(max = 150) private String centre;
    @Size(max = 150) private String commune;
    @Size(max = 30) private String volume;
    @Size(max = 30) private String folio;
    @NotNull @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateEvenement;
    @NotBlank @Size(max = 150) private String lieuEvenement;
    @NotBlank @Size(max = 100) private String nom;
    @NotBlank @Size(max = 150) private String prenom;
    @Pattern(regexp = "^$|[MF]") private String sexe;
    @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateNaissance;
    @Size(max = 150) private String lieuNaissance;
    @Size(max = 100) private String nomPere;
    @Size(max = 150) private String prenomPere;
    @Size(max = 100) private String nomMere;
    @Size(max = 150) private String prenomMere;
    @Size(max = 100) private String nomConjoint;
    @Size(max = 150) private String prenomConjoint;
    @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateNaissanceConjoint;
    @Size(max = 150) private String lieuNaissanceConjoint;
    @Pattern(regexp = "^$|([01]\\d|2[0-3]):[0-5]\\d", message = "format HH:MM") private String heureDeces;
    @Size(max = 255) private String causeDeces;
    @Size(max = 200) private String temoin1;
    @Size(max = 200) private String temoin2;
    @Size(max = 2000) private String mentionsMarginales;
    @Size(max = 2000) private String observations;

    public static SaisieActe depuis(Vues.Acte a) {
        SaisieActe s = new SaisieActe();
        s.typeActe = a.typeActe(); s.centre = a.centre(); s.commune = a.commune(); s.volume = a.volume();
        s.folio = a.folio(); s.dateEvenement = a.dateEvenement(); s.lieuEvenement = a.lieuEvenement();
        s.nom = a.nom(); s.prenom = a.prenom(); s.sexe = a.sexe(); s.dateNaissance = a.dateNaissance();
        s.lieuNaissance = a.lieuNaissance(); s.nomPere = a.nomPere(); s.prenomPere = a.prenomPere();
        s.nomMere = a.nomMere(); s.prenomMere = a.prenomMere(); s.nomConjoint = a.nomConjoint();
        s.prenomConjoint = a.prenomConjoint(); s.dateNaissanceConjoint = a.dateNaissanceConjoint();
        s.lieuNaissanceConjoint = a.lieuNaissanceConjoint(); s.heureDeces = a.heureDeces();
        s.causeDeces = a.causeDeces(); s.temoin1 = a.temoin1(); s.temoin2 = a.temoin2();
        s.mentionsMarginales = a.mentionsMarginales(); s.observations = a.observations();
        return s;
    }

    /** Les champs vides du formulaire sont envoyés comme absents, et non comme chaînes vides. */
    public void nettoyer() {
        commune = v(commune); volume = v(volume); folio = v(folio); sexe = v(sexe);
        lieuNaissance = v(lieuNaissance); nomPere = v(nomPere); prenomPere = v(prenomPere);
        nomMere = v(nomMere); prenomMere = v(prenomMere); nomConjoint = v(nomConjoint);
        prenomConjoint = v(prenomConjoint); lieuNaissanceConjoint = v(lieuNaissanceConjoint);
        heureDeces = v(heureDeces); causeDeces = v(causeDeces); temoin1 = v(temoin1); temoin2 = v(temoin2);
        mentionsMarginales = v(mentionsMarginales); observations = v(observations);
    }

    private static String v(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    public String getTypeActe() { return typeActe; }
    public void setTypeActe(String typeActe) { this.typeActe = typeActe; }
    public String getCentre() { return centre; }
    public void setCentre(String centre) { this.centre = centre; }
    public String getCommune() { return commune; }
    public void setCommune(String commune) { this.commune = commune; }
    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }
    public String getLieuEvenement() { return lieuEvenement; }
    public void setLieuEvenement(String lieuEvenement) { this.lieuEvenement = lieuEvenement; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    public String getNomPere() { return nomPere; }
    public void setNomPere(String nomPere) { this.nomPere = nomPere; }
    public String getPrenomPere() { return prenomPere; }
    public void setPrenomPere(String prenomPere) { this.prenomPere = prenomPere; }
    public String getNomMere() { return nomMere; }
    public void setNomMere(String nomMere) { this.nomMere = nomMere; }
    public String getPrenomMere() { return prenomMere; }
    public void setPrenomMere(String prenomMere) { this.prenomMere = prenomMere; }
    public String getNomConjoint() { return nomConjoint; }
    public void setNomConjoint(String nomConjoint) { this.nomConjoint = nomConjoint; }
    public String getPrenomConjoint() { return prenomConjoint; }
    public void setPrenomConjoint(String prenomConjoint) { this.prenomConjoint = prenomConjoint; }
    public LocalDate getDateNaissanceConjoint() { return dateNaissanceConjoint; }
    public void setDateNaissanceConjoint(LocalDate d) { this.dateNaissanceConjoint = d; }
    public String getLieuNaissanceConjoint() { return lieuNaissanceConjoint; }
    public void setLieuNaissanceConjoint(String l) { this.lieuNaissanceConjoint = l; }
    public String getHeureDeces() { return heureDeces; }
    public void setHeureDeces(String heureDeces) { this.heureDeces = heureDeces; }
    public String getCauseDeces() { return causeDeces; }
    public void setCauseDeces(String causeDeces) { this.causeDeces = causeDeces; }
    public String getTemoin1() { return temoin1; }
    public void setTemoin1(String temoin1) { this.temoin1 = temoin1; }
    public String getTemoin2() { return temoin2; }
    public void setTemoin2(String temoin2) { this.temoin2 = temoin2; }
    public String getMentionsMarginales() { return mentionsMarginales; }
    public void setMentionsMarginales(String m) { this.mentionsMarginales = m; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
