package sn.etatcivil.portail.web;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Formulaires des pages ; ils sont envoyés tels quels (JSON) aux microservices. */
public final class Formulaires {
    private Formulaires() {}

    public static class Demande {
        /** Vérification d'identité (documents sensibles) : envoyées à part, jamais dans le JSON de la demande. */
        private String typePiece = "CNI";
        private org.springframework.web.multipart.MultipartFile recto;
        private org.springframework.web.multipart.MultipartFile verso;
        private org.springframework.web.multipart.MultipartFile selfie;

        @com.fasterxml.jackson.annotation.JsonIgnore public String getTypePiece() { return typePiece; }
        public void setTypePiece(String v) { typePiece = v; }
        @com.fasterxml.jackson.annotation.JsonIgnore public org.springframework.web.multipart.MultipartFile getRecto() { return recto; }
        public void setRecto(org.springframework.web.multipart.MultipartFile v) { recto = v; }
        @com.fasterxml.jackson.annotation.JsonIgnore public org.springframework.web.multipart.MultipartFile getVerso() { return verso; }
        public void setVerso(org.springframework.web.multipart.MultipartFile v) { verso = v; }
        @com.fasterxml.jackson.annotation.JsonIgnore public org.springframework.web.multipart.MultipartFile getSelfie() { return selfie; }
        public void setSelfie(org.springframework.web.multipart.MultipartFile v) { selfie = v; }

        @NotNull private Long typeDocumentId;
        @NotNull(message = "choisissez un motif") private Long motifId;
        @Size(max = 255) private String precisionMotif;
        @NotBlank private String lien = "MOI_MEME";
        @NotBlank private String format = "NUMERIQUE";
        @Size(max = 255) private String adresseLivraison;
        @Size(max = 100) private String villeLivraison;
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
        @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateEvenement;
        @Size(max = 255) private String profession;
        @Size(max = 255) private String residence;
        @Size(max = 255) private String professionPere;
        @Size(max = 255) private String residencePere;
        @Size(max = 255) private String professionMere;
        @Size(max = 255) private String residenceMere;
        @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dateNaissanceConjoint;
        @Size(max = 255) private String lieuNaissanceConjoint;
        @Size(max = 255) private String professionConjoint;
        @Size(max = 255) private String residenceConjoint;
        @Size(max = 255) private String temoin1;
        @Size(max = 255) private String temoin2;
        @Size(max = 255) private String medecin;
        @Size(max = 255) private String adresseMedecin;
        @Size(max = 255) private String causeDeces;
        @Size(max = 255) private String lieuDeces;
        @Size(max = 255) private String typeMention;
        @Size(max = 1000) private String detailsMention;
        @Size(max = 1000) private String precisions;
        /** Retrait d'un document papier à une mission diplomatique (diaspora) au lieu d'une livraison. */
        private Long missionRetraitId;

        public Long getTypeDocumentId() { return typeDocumentId; }
        public void setTypeDocumentId(Long v) { typeDocumentId = v; }
        public Long getMotifId() { return motifId; }
        public void setMotifId(Long v) { motifId = v; }
        public String getPrecisionMotif() { return precisionMotif; }
        public void setPrecisionMotif(String v) { precisionMotif = v; }
        public String getLien() { return lien; }
        public void setLien(String v) { lien = v; }
        public String getFormat() { return format; }
        public void setFormat(String v) { format = v; }
        public String getAdresseLivraison() { return adresseLivraison; }
        public void setAdresseLivraison(String v) { adresseLivraison = v; }
        public String getVilleLivraison() { return villeLivraison; }
        public void setVilleLivraison(String v) { villeLivraison = v; }
        public String getNom() { return nom; }
        public void setNom(String v) { nom = v; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String v) { prenom = v; }
        public String getSexe() { return sexe; }
        public void setSexe(String v) { sexe = v; }
        public LocalDate getDateNaissance() { return dateNaissance; }
        public void setDateNaissance(LocalDate v) { dateNaissance = v; }
        public String getLieuNaissance() { return lieuNaissance; }
        public void setLieuNaissance(String v) { lieuNaissance = v; }
        public String getNomPere() { return nomPere; }
        public void setNomPere(String v) { nomPere = v; }
        public String getPrenomPere() { return prenomPere; }
        public void setPrenomPere(String v) { prenomPere = v; }
        public String getNomMere() { return nomMere; }
        public void setNomMere(String v) { nomMere = v; }
        public String getPrenomMere() { return prenomMere; }
        public void setPrenomMere(String v) { prenomMere = v; }
        public String getNomConjoint() { return nomConjoint; }
        public void setNomConjoint(String v) { nomConjoint = v; }
        public String getPrenomConjoint() { return prenomConjoint; }
        public void setPrenomConjoint(String v) { prenomConjoint = v; }
        public LocalDate getDateEvenement() { return dateEvenement; }
        public void setDateEvenement(LocalDate v) { dateEvenement = v; }
        public String getProfession() { return profession; }
        public void setProfession(String v) { profession = v; }
        public String getResidence() { return residence; }
        public void setResidence(String v) { residence = v; }
        public String getProfessionPere() { return professionPere; }
        public void setProfessionPere(String v) { professionPere = v; }
        public String getResidencePere() { return residencePere; }
        public void setResidencePere(String v) { residencePere = v; }
        public String getProfessionMere() { return professionMere; }
        public void setProfessionMere(String v) { professionMere = v; }
        public String getResidenceMere() { return residenceMere; }
        public void setResidenceMere(String v) { residenceMere = v; }
        public LocalDate getDateNaissanceConjoint() { return dateNaissanceConjoint; }
        public void setDateNaissanceConjoint(LocalDate v) { dateNaissanceConjoint = v; }
        public String getLieuNaissanceConjoint() { return lieuNaissanceConjoint; }
        public void setLieuNaissanceConjoint(String v) { lieuNaissanceConjoint = v; }
        public String getProfessionConjoint() { return professionConjoint; }
        public void setProfessionConjoint(String v) { professionConjoint = v; }
        public String getResidenceConjoint() { return residenceConjoint; }
        public void setResidenceConjoint(String v) { residenceConjoint = v; }
        public String getTemoin1() { return temoin1; }
        public void setTemoin1(String v) { temoin1 = v; }
        public String getTemoin2() { return temoin2; }
        public void setTemoin2(String v) { temoin2 = v; }
        public String getMedecin() { return medecin; }
        public void setMedecin(String v) { medecin = v; }
        public String getAdresseMedecin() { return adresseMedecin; }
        public void setAdresseMedecin(String v) { adresseMedecin = v; }
        public String getCauseDeces() { return causeDeces; }
        public void setCauseDeces(String v) { causeDeces = v; }
        public String getLieuDeces() { return lieuDeces; }
        public void setLieuDeces(String v) { lieuDeces = v; }
        public String getTypeMention() { return typeMention; }
        public void setTypeMention(String v) { typeMention = v; }
        public String getDetailsMention() { return detailsMention; }
        public void setDetailsMention(String v) { detailsMention = v; }
        public String getPrecisions() { return precisions; }
        public void setPrecisions(String v) { precisions = v; }
        public Long getMissionRetraitId() { return missionRetraitId; }
        public void setMissionRetraitId(Long v) { missionRetraitId = v; }
    }
}
