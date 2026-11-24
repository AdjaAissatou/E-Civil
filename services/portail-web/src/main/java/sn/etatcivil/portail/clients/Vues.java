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
                               boolean concerneUnCouple, boolean copieIntegrale, String modele, String origine,
                               int tarif, int delaiJours, boolean actif,
                               Long natureId, String libelleNature, String libelleModele, String typeMention,
                               List<String> champs, Integer dureeValiditeJours, String motsCles, String piecesAFournir,
                               boolean estOriginal, Long copieDeId, boolean exigeActe) {

        public TypeDocument(Long id, String code, String libelle, String description, String typeActe,
                            boolean concerneUnCouple, boolean copieIntegrale, String modele, String origine,
                            int tarif, int delaiJours, boolean actif) {
            this(id, code, libelle, description, typeActe, concerneUnCouple, copieIntegrale, modele, origine, tarif,
                    delaiJours, actif, null, null, null, null, List.of(), null, null, null, false, null, true);
        }

        /** La variété fait-elle remplir cette rubrique (PARENTS, CONJOINT, TEMOINS…) ? */
        public boolean demande(String champ) { return champs != null && champs.contains(champ); }

        /** Durée de validité lisible (« 3 mois », « 1 an », « illimitée »). */
        public String libelleValidite() {
            if (dureeValiditeJours == null) return "sans limite";
            int j = dureeValiditeJours;
            if (j % 365 == 0) return j / 365 == 1 ? "1 an" : j / 365 + " ans";
            if (j % 30 == 0) return j / 30 + " mois";
            return j + " jours";
        }

        /** Ordre d'affichage des familles dans le catalogue. */
        public static final List<String> FAMILLES = List.of("NAISSANCE", "RECONNAISSANCE", "MARIAGE", "DIVORCE", "DECES", "CERTIFICAT");

        public String libelleActe() {
            return switch (typeActe) {
                case "NAISSANCE" -> "naissance";
                case "MARIAGE" -> "mariage";
                case "DIVORCE" -> "divorce";
                case "RECONNAISSANCE" -> "reconnaissance";
                default -> "décès";
            };
        }

        public boolean estCertificat() { return modele != null && modele.startsWith("CERTIFICAT_"); }

        public boolean estLivret() { return "LIVRET_FAMILLE".equals(modele); }

        /** Famille du catalogue : les certificats sont regroupés, les autres documents suivent leur type d'acte. */
        public String famille() { return estCertificat() ? "CERTIFICAT" : typeActe; }

        public int rangFamille() { return FAMILLES.indexOf(famille()); }

        public String etiquette() {
            if ("JUGEMENT_SUPPLETIF".equals(origine)) return "Jugement supplétif";
            if ("TRANSCRIPTION".equals(origine)) return "Transcription";
            if (modele == null) return copieIntegrale ? "Copie intégrale" : "Extrait";
            return switch (modele) {
                case "COPIE_INTEGRALE" -> "Copie intégrale";
                case "EXTRAIT_SANS_FILIATION" -> "Extrait sans filiation";
                case "LIVRET_FAMILLE" -> "Duplicata";
                case "EXTRAIT" -> "Extrait";
                default -> "Certificat";
            };
        }

        public String accent() {
            return switch (famille()) {
                case "NAISSANCE", "RECONNAISSANCE" -> "accent-naissance";
                case "MARIAGE" -> "accent-mariage";
                case "DIVORCE" -> "accent-divorce";
                case "CERTIFICAT" -> "accent-certificat";
                default -> "accent-deces";
            };
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Motif(Long id, String libelle, boolean autre, int ordre, String piecesRequises, Long typeDocumentId) {
        public Motif(Long id, String libelle, boolean autre) {
            this(id, libelle, autre, 0, null, null);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nature(Long id, String code, String libelle, String description, int ordre, boolean active) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Libelle(String code, String libelle) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReferentielsCatalogue(List<Libelle> modeles, List<Libelle> champs, List<Libelle> typesActe,
                                        List<Libelle> origines) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pays(Long id, String nom, String codeIso, String continent, String capitale, String zone) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mission(Long id, String type, String nom, Long paysId, String pays, String ville, String codePostal,
                          String competence, String adresse, String telephone, String email, List<Long> paysCouverts,
                          boolean active) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReferentielDiaspora(List<Pays> pays, List<String> typesMission, List<String> zones) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComplementsDemande(String profession, String residence, String professionPere, String residencePere, String professionMere, String residenceMere, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint, String professionConjoint, String residenceConjoint, String temoin1, String temoin2, String medecin, String adresseMedecin, String causeDeces, String lieuDeces, String typeMention, String detailsMention, String precisions) {
        public boolean vide() {
            return java.util.stream.Stream.of(profession, residence, professionPere, residencePere, professionMere, residenceMere, dateNaissanceConjoint, lieuNaissanceConjoint, professionConjoint, residenceConjoint, temoin1, temoin2, medecin, adresseMedecin, causeDeces, lieuDeces, typeMention, detailsMention, precisions).allMatch(java.util.Objects::isNull);
        }
    }

    /** Informations personnelles et préférences du compte connecté (« Mon compte »). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfilPersonnel(String prenom, String nom, String email, String telephone, String sexe,
                                  LocalDate dateNaissance, String lieuNaissance, String profession, String residence,
                                  String localite, Integer nombreEnfants, String numeroCni, String typeUtilisateur,
                                  String paysResidence, boolean notifications, String langue) {
        public boolean diaspora() { return "DIASPORA".equals(typeUtilisateur); }
    }

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
                          List<Historique> historique, ComplementsDemande complements, Integer dureeValiditeJours,
                          Boolean exigeActe) {

        public Demande(UUID id, String reference, String statut, String libelleStatut, String explicationStatut,
                       boolean terminee, String demandeurId, String demandeurNom, String demandeurEmail,
                       Long typeDocumentId, String libelleDocument, String typeActe, boolean copieIntegrale, int montant,
                       String motif, String lien, String format, String adresseLivraison, String villeLivraison,
                       String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                       String nomPere, String prenomPere, String nomMere, String prenomMere,
                       String nomConjoint, String prenomConjoint, LocalDate dateEvenement,
                       UUID acteId, String numeroActe, String noteRecherche, String signataire, Instant dateSignature,
                       String codeVerification, String motifRejet, Instant dateCreation, Instant dateModification,
                       List<Historique> historique) {
            this(id, reference, statut, libelleStatut, explicationStatut, terminee, demandeurId, demandeurNom,
                    demandeurEmail, typeDocumentId, libelleDocument, typeActe, copieIntegrale, montant, motif, lien, format,
                    adresseLivraison, villeLivraison, nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere,
                    nomMere, prenomMere, nomConjoint, prenomConjoint, dateEvenement, acteId, numeroActe, noteRecherche,
                    signataire, dateSignature, codeVerification, motifRejet, dateCreation, dateModification, historique,
                    null, null, true);
        }

        /** Document établi sur comparution devant l'officier, sans acte du registre. */
        public boolean surComparution() { return Boolean.FALSE.equals(exigeActe); }

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

        /** Dernière étape franchie du suivi visuel : 1 déposée, 2 validée, 3 payée, 4 acte retrouvé, 5 délivrée. */
        public static int etapeDe(String statut) {
            return switch (statut) {
                case "SOUMISE" -> 1;
                case "A_PAYER" -> 2;
                case "PAYEE", "ACTE_INTROUVABLE" -> 3;
                case "ACTE_TROUVE" -> 4;
                case "SIGNEE", "LIVREE" -> 5;
                default -> 0;
            };
        }

        public boolean arretee() { return "REJETEE".equals(statut) || "ANNULEE".equals(statut); }

        /**
         * Étape en cours dans le suivi (les précédentes sont franchies ; 6 quand tout est terminé). Pour une demande rejetée ou annulée, c'est l'étape où elle s'est arrêtée
         * (la suivante après la dernière atteinte).
         */
        public int etapeCourante() {
            if (!arretee()) return etapeDe(statut) + 1; // 6 = tout est terminé
            int atteinte = historique == null ? 1 : historique.stream().mapToInt(h -> etapeDe(h.nouveauStatut())).max().orElse(1);
            return Math.min(atteinte + 1, 5);
        }
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
                       String mentionsMarginales, String observations, String origine, String referenceOrigine,
                       String agentSaisie, Instant dateSaisie, Instant dateModification,
                       String profession, String domicile, String professionPere, String domicilePere,
                       String professionMere, String domicileMere, String professionConjoint, String domicileConjoint,
                       String medecin, List<MentionActe> mentions) {

        public Acte(UUID id, String typeActe, String numeroActe, Integer annee, String statut,
                    String centre, String commune, String volume, String folio,
                    LocalDate dateEvenement, String lieuEvenement,
                    String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                    String nomPere, String prenomPere, String nomMere, String prenomMere,
                    String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
                    String heureDeces, String causeDeces, String temoin1, String temoin2,
                    String mentionsMarginales, String observations, String origine, String referenceOrigine,
                    String agentSaisie, Instant dateSaisie, Instant dateModification) {
            this(id, typeActe, numeroActe, annee, statut, centre, commune, volume, folio, dateEvenement, lieuEvenement,
                    nom, prenom, sexe, dateNaissance, lieuNaissance, nomPere, prenomPere, nomMere, prenomMere,
                    nomConjoint, prenomConjoint, dateNaissanceConjoint, lieuNaissanceConjoint, heureDeces, causeDeces,
                    temoin1, temoin2, mentionsMarginales, observations, origine, referenceOrigine, agentSaisie, dateSaisie,
                    dateModification, null, null, null, null, null, null, null, null, null, List.of());
        }

        public boolean estValide() { return "VALIDE".equals(statut); }

        /** Profession et domicile réunis pour l'affichage (« commerçant, domicilié à Thiès »). */
        public static String situation(String profession, String domicile) {
            String p = profession == null ? "" : profession.trim(), d = domicile == null ? "" : domicile.trim();
            if (p.isEmpty()) return d.isEmpty() ? null : "domicilié(e) à " + d;
            return d.isEmpty() ? p : p + ", domicilié(e) à " + d;
        }

        public String libelleOrigine() {
            return "JUGEMENT_SUPPLETIF".equals(origine) ? "Jugement supplétif"
                    : "TRANSCRIPTION".equals(origine) ? "Transcription d'un acte étranger" : null;
        }

        public String libelleType() {
            return switch (typeActe) {
                case "NAISSANCE" -> "Naissance";
                case "MARIAGE" -> "Mariage";
                case "DIVORCE" -> "Divorce";
                case "RECONNAISSANCE" -> "Reconnaissance";
                default -> "Décès";
            };
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatistiquesRegistre(long total, long saisis, long valides) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Enfant(String numeroActe, String prenom, String nom, String sexe, LocalDate dateNaissance,
                         String lieuNaissance) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Complements(String modele, String motif, String constat, List<Enfant> enfants, LocalDate valableJusquAu,
                              Declaration declaration, String mention) {
        public Complements(String modele, String motif, String constat, List<Enfant> enfants) {
            this(modele, motif, constat, enfants, null, null, null);
        }

        /** Le document a cessé d'être valable (date de fin dépassée, heure de Dakar). */
        public boolean expire() {
            return valableJusquAu != null && java.time.LocalDate.now(java.time.ZoneId.of("Africa/Dakar")).isAfter(valableJusquAu);
        }
    }

    /** Identité et déclaration de la personne, pour un document établi sur comparution. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Declaration(String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
                              String profession, String residence, String temoin1, String temoin2, String precisions) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Verification(String libelleDocument, Acte acte, Complements complements, String signataire,
                               Instant dateSignature, String codeVerification, String empreinte) {}

    // ---------------------------------------------------------------- habilitations

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfilResume(String id, String nom) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Compte(String id, String email, String prenom, String nom, String nomComplet, boolean actif,
                         Instant dateCreation, List<ProfilResume> profils) {
        public boolean dansProfil(String profilId) {
            return profils != null && profils.stream().anyMatch(p -> p.id().equals(profilId));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Permission(String code, String libelle, String description, String avantage, String interdiction,
                             String famille) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Role(String code, String libelle, String description, java.util.Set<String> permissions, boolean deBase,
                       List<ProfilResume> profils) {
        public boolean accorde(String permission) { return permissions != null && permissions.contains(permission); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profil(String id, String nom, String description, java.util.Set<String> roles, boolean parDefaut,
                         int nombreMembres, List<Compte> membres) {
        public boolean comporte(String role) { return roles != null && roles.contains(role); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Operation(Instant date, String auteur, String type, String libelleType, String cible, String detail) {
        /** Teinte de la pastille dans le journal. */
        public String teinte() {
            if (type == null) return "secondary";
            if (type.endsWith("SUPPRIME") || type.equals("UTILISATEUR_DESACTIVE")) return "danger";
            if (type.endsWith("CREE") || type.equals("UTILISATEUR_ACTIVE")) return "success";
            if (type.equals("INITIALISATION")) return "dark";
            return "info";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FicheCompte(Compte utilisateur, List<Role> roles, List<Permission> permissions, List<Operation> historique) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Synthese(long utilisateurs, int profils, int roles, int permissions, List<Operation> dernieresOperations) {}

    // ---------------------------------------------------------------- registre : mentions, journal, référentiels

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MentionActe(String type, String libelleType, LocalDate date, String reference, String details,
                              String agent, String texte) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OperationActe(Instant date, String auteur, String action, String libelleAction, String detail) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Commune(String nom, String departement, String region) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TypeMention(String code, String libelle, List<String> actes) {}
}
