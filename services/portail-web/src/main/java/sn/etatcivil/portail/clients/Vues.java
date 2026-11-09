package sn.etatcivil.portail.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Données renvoyées par les microservices, telles que les pages les affichent. */
public final class Vues {
    private Vues() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page<T>(List<T> contenu, int numero, int taille, long total, int nombrePages) {
        public boolean premiere() { return numero == 0; }
        public boolean derniere() { return numero + 1 >= nombrePages; }
        public boolean vide() { return contenu == null || contenu.isEmpty(); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TypeDocument(Long id, String code, String libelle, String description, String typeActe,
                               boolean concerneUnCouple, boolean copieIntegrale, int tarif, int delaiJours, boolean actif) {
        public String libelleActe() {
            return switch (typeActe) {
                case "NAISSANCE" -> "naissance";
                case "MARIAGE" -> "mariage";
                case "DIVORCE" -> "divorce";
                default -> "décès";
            };
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Motif(Long id, String libelle, boolean autre) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Historique(String nouveauStatut, String libelle, String auteur, String commentaire, Instant date) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Demande(UUID id, String reference, String statut, String libelleStatut, String explicationStatut,
                          boolean terminee, String demandeurId, String demandeurNom, String demandeurEmail,
                          Long typeDocumentId, String libelleDocument, String typeActe, boolean copieIntegrale, int montant,
                          String motif, String lien, String format, String adresseLivraison, String villeLivraison,
                          String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                          String nomPere, String prenomPere, String nomMere, String prenomMere,
                          String nomConjoint, String prenomConjoint, LocalDate dateEvenement,
                          UUID acteId, String numeroActe, String noteRecherche, String signataire, Instant dateSignature,
                          String codeVerification, String motifRejet, Instant dateCreation, Instant dateModification,
                          List<Historique> historique) {

        public String personneConcernee() { return prenom + " " + nom; }

        public boolean numerique() { return "NUMERIQUE".equals(format); }

        public boolean documentDisponible() { return "SIGNEE".equals(statut) || "LIVREE".equals(statut); }

        public String libelleFormat() { return numerique() ? "Numérique (PDF téléchargeable)" : "Papier (livraison ou retrait)"; }

        public String libelleLien() {
            return switch (lien) {
                case "MOI_MEME" -> "Moi-même";
                case "ENFANT" -> "Mon enfant";
                case "PARENT" -> "Mon père ou ma mère";
                case "CONJOINT" -> "Mon conjoint";
                default -> "Autre (mandataire)";
            };
        }

        /** Classe Bootstrap du badge de statut. */
        public String couleur() { return Vues.couleur(statut); }
    }

    public static String couleur(String statut) {
        return switch (statut) {
            case "SOUMISE" -> "secondary";
            case "A_PAYER" -> "warning";
            case "PAYEE", "ACTE_TROUVE" -> "info";
            case "ACTE_INTROUVABLE" -> "dark";
            case "SIGNEE", "LIVREE" -> "success";
            default -> "danger";
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paiement(UUID id, int montant, String statut, String urlRecu, Instant dateCreation, Instant dateConfirmation) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Acte(UUID id, String typeActe, String numeroActe, Integer annee, String statut,
                       String centre, String commune, String volume, String folio,
                       LocalDate dateEvenement, String lieuEvenement,
                       String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                       String nomPere, String prenomPere, String nomMere, String prenomMere,
                       String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
                       String heureDeces, String causeDeces, String temoin1, String temoin2,
                       String mentionsMarginales, String observations,
                       String agentSaisie, Instant dateSaisie, Instant dateModification) {
        public boolean estValide() { return "VALIDE".equals(statut); }

        public String libelleType() {
            return switch (typeActe) {
                case "NAISSANCE" -> "Naissance";
                case "MARIAGE" -> "Mariage";
                case "DIVORCE" -> "Divorce";
                default -> "Décès";
            };
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatistiquesRegistre(long total, long saisis, long valides) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Verification(String libelleDocument, Acte acte, String signataire, Instant dateSignature,
                               String codeVerification, String empreinte) {}
}
