package sn.etatcivil.demande.domaine;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static sn.etatcivil.demande.domaine.StatutDemande.*;

/**
 * Agrégat : une demande de document d'état civil et toutes les règles de son traitement.
 * Chaque changement de statut passe par {@link #changer}, qui vérifie la transition et l'historise.
 */
public class Demande {

    private final UUID id;
    private final String reference;
    private final Demandeur demandeur;
    private final Long typeDocumentId;
    private final String libelleDocument;
    private final TypeActe typeActe;
    private final ModeleDocument modele;
    private final OrigineActe origine;
    private final int montant;
    private final Long motifId;
    private final String libelleMotif;
    private final LienDemandeur lien;
    private final FormatDocument format;
    private final String adresseLivraison;
    private final String villeLivraison;
    private final PersonneConcernee personne;
    private final ComplementsDemande complements;
    private final Integer dureeValiditeJours;
    private final Instant dateCreation;

    private StatutDemande statut;
    private UUID acteId;
    private String numeroActe;
    private String noteRecherche;
    private String signataire;
    private Instant dateSignature;
    private String codeVerification;
    private String motifRejet;
    private Instant dateModification;
    private final Integer version;
    private final List<ChangementStatut> historique;
    private final List<ChangementStatut> nouveauxChangements = new ArrayList<>();

    public Demande(UUID id, String reference, Demandeur demandeur, Long typeDocumentId, String libelleDocument,
                   TypeActe typeActe, ModeleDocument modele, OrigineActe origine, int montant, Long motifId, String libelleMotif,
                   LienDemandeur lien, FormatDocument format, String adresseLivraison, String villeLivraison,
                   PersonneConcernee personne, Instant dateCreation, StatutDemande statut, UUID acteId,
                   String numeroActe, String noteRecherche, String signataire, Instant dateSignature,
                   String codeVerification, String motifRejet, Instant dateModification, Integer version,
                   List<ChangementStatut> historique) {
        this(id, reference, demandeur, typeDocumentId, libelleDocument, typeActe, modele, origine, montant, motifId,
                libelleMotif, lien, format, adresseLivraison, villeLivraison, personne, dateCreation, statut, acteId,
                numeroActe, noteRecherche, signataire, dateSignature, codeVerification, motifRejet, dateModification,
                version, historique, ComplementsDemande.AUCUN, null);
    }

    /** Reconstitution depuis le stockage. */
    public Demande(UUID id, String reference, Demandeur demandeur, Long typeDocumentId, String libelleDocument,
                   TypeActe typeActe, ModeleDocument modele, OrigineActe origine, int montant, Long motifId, String libelleMotif,
                   LienDemandeur lien, FormatDocument format, String adresseLivraison, String villeLivraison,
                   PersonneConcernee personne, Instant dateCreation, StatutDemande statut, UUID acteId,
                   String numeroActe, String noteRecherche, String signataire, Instant dateSignature,
                   String codeVerification, String motifRejet, Instant dateModification, Integer version,
                   List<ChangementStatut> historique, ComplementsDemande complements, Integer dureeValiditeJours) {
        this.id = id;
        this.reference = reference;
        this.demandeur = demandeur;
        this.typeDocumentId = typeDocumentId;
        this.libelleDocument = libelleDocument;
        this.typeActe = typeActe;
        this.modele = modele;
        this.origine = origine;
        this.montant = montant;
        this.motifId = motifId;
        this.libelleMotif = libelleMotif;
        this.lien = lien;
        this.format = format;
        this.adresseLivraison = adresseLivraison;
        this.villeLivraison = villeLivraison;
        this.personne = personne;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.acteId = acteId;
        this.numeroActe = numeroActe;
        this.noteRecherche = noteRecherche;
        this.signataire = signataire;
        this.dateSignature = dateSignature;
        this.codeVerification = codeVerification;
        this.motifRejet = motifRejet;
        this.dateModification = dateModification;
        this.version = version;
        this.historique = new ArrayList<>(historique);
        this.complements = complements == null ? ComplementsDemande.AUCUN : complements;
        this.dureeValiditeJours = dureeValiditeJours;
    }

    /** Dépôt par un citoyen : le tarif du catalogue est figé dans la demande. */
    public static Demande deposer(String reference, Demandeur demandeur, TypeDocument type, Motif motif,
                                  String precisionMotif, LienDemandeur lien, FormatDocument format,
                                  String adresse, String ville, PersonneConcernee personne, Instant maintenant) {
        return deposer(reference, demandeur, type, motif, precisionMotif, lien, format, adresse, ville, personne,
                ComplementsDemande.AUCUN, maintenant);
    }

    /** Dépôt par un citoyen : le tarif, la durée de validité et les rubriques de la variété sont figés dans la demande. */
    public static Demande deposer(String reference, Demandeur demandeur, TypeDocument type, Motif motif,
                                  String precisionMotif, LienDemandeur lien, FormatDocument format,
                                  String adresse, String ville, PersonneConcernee personne,
                                  ComplementsDemande complements, Instant maintenant) {
        if (!type.actif()) throw new RegleMetierException("Ce document n'est pas disponible");
        if (!motif.concerne(type.id())) throw new RegleMetierException("Ce motif ne s'applique pas à ce document");
        if (lien == null || format == null) throw new RegleMetierException("Indiquez le lien avec la personne et le format");
        String precision = PersonneConcernee.t(precisionMotif);
        if (motif.estAutre() && precision == null) throw new RegleMetierException("Précisez le motif de votre demande");
        boolean papier = format == FormatDocument.PHYSIQUE;
        if (papier && (PersonneConcernee.vide(adresse) || PersonneConcernee.vide(ville))) {
            throw new RegleMetierException("L'adresse et la ville de livraison sont obligatoires pour un document papier");
        }
        PersonneConcernee p = personne.pour(type.typeActe());
        LocalDate aujourdhui = LocalDate.ofInstant(maintenant, ZoneId.of("Africa/Dakar"));
        p.verifier(type.typeActe(), aujourdhui);
        ComplementsDemande c = (complements == null ? ComplementsDemande.AUCUN : complements).pour(type);
        c.verifier(type, aujourdhui);

        Demande d = new Demande(UUID.randomUUID(), reference, demandeur, type.id(), type.libelle(), type.typeActe(),
                type.modele(), type.origine(), type.tarif(), motif.id(),
                motif.estAutre() ? precision : motif.libelle(), lien, format,
                papier ? adresse.trim() : null, papier ? ville.trim() : null, p, maintenant, SOUMISE,
                null, null, null, null, null, null, null, maintenant, null, List.of(), c, type.dureeValiditeJours());
        d.tracer(null, SOUMISE, Acteur.citoyen(demandeur), null, maintenant);
        return d;
    }

    // ---------------------------------------------------------------- citoyen

    public void annuler(String demandeurId, Instant maintenant) {
        if (!demandeur.id().equals(demandeurId)) throw new DemandeIntrouvableException();
        changer(ANNULEE, Acteur.citoyen(demandeur), null, maintenant);
    }

    public boolean visiblePar(String utilisateurId, boolean estAgent) {
        return estAgent || demandeur.id().equals(utilisateurId);
    }

    // ---------------------------------------------------------------- délégué

    public void valider(Acteur delegue, Instant maintenant) {
        exiger(delegue, Habilitation.DEMANDES_VALIDER);
        changer(A_PAYER, delegue, null, maintenant);
    }

    /**
     * Rejet avant paiement (validation des demandes), quand l'acte n'existe pas (recherche des actes), ou à toute
     * étape (supervision).
     */
    public void rejeter(String motif, Acteur agent, Instant maintenant) {
        String m = PersonneConcernee.t(motif);
        if (m == null) throw new RegleMetierException("Indiquez le motif du rejet");
        boolean autorise = agent.peut(Habilitation.DEMANDES_SUPERVISER)
                || (agent.peut(Habilitation.DEMANDES_VALIDER) && (statut == SOUMISE || statut == A_PAYER))
                || (agent.peut(Habilitation.DEMANDES_RECHERCHER) && statut == ACTE_INTROUVABLE);
        if (!autorise) throw new RegleMetierException("Vos habilitations ne permettent pas de rejeter cette demande à ce stade");
        motifRejet = m;
        changer(REJETEE, agent, m, maintenant);
    }

    public void marquerLivree(Acteur delegue, Instant maintenant) {
        exiger(delegue, Habilitation.DOCUMENTS_LIVRER);
        if (format != FormatDocument.PHYSIQUE) {
            throw new RegleMetierException("Un document numérique est délivré par téléchargement");
        }
        changer(LIVREE, delegue, null, maintenant);
    }

    // ---------------------------------------------------------------- paiement

    /** @return {@code false} si le paiement était déjà enregistré ou arrive trop tard (idempotent). */
    public boolean enregistrerPaiement(int montantPaye, String detail, Instant maintenant) {
        if (statut != A_PAYER) return false;
        if (montantPaye != montant) {
            throw new RegleMetierException("Montant payé (" + montantPaye + ") différent du montant dû (" + montant + ")");
        }
        changer(PAYEE, Acteur.SYSTEME, detail, maintenant);
        return true;
    }

    // ---------------------------------------------------------------- registre

    public boolean enAttenteDeRecherche() {
        return statut == PAYEE || statut == ACTE_INTROUVABLE;
    }

    public void rattacherActe(ActeRetrouve acte, Acteur auteur, String commentaire, Instant maintenant) {
        if (!enAttenteDeRecherche()) throw new RegleMetierException("Cette demande n'est pas en attente de recherche");
        if (acte.typeActe() != typeActe) {
            throw new RegleMetierException("L'acte " + acte.numero() + " est un acte de " + acte.typeActe().libelle().toLowerCase()
                    + ", pas de " + typeActe.libelle().toLowerCase());
        }
        if (!acte.valide()) throw new RegleMetierException("L'acte " + acte.numero() + " n'est pas encore validé par un officier");
        acteId = acte.id();
        numeroActe = acte.numero();
        noteRecherche = null;
        changer(ACTE_TROUVE, auteur, commentaire, maintenant);
    }

    /** Document établi sur comparution (non-appartenance, coutume) : aucun acte n'est à rechercher. */
    public void dispenserDActe(Acteur auteur, Instant maintenant) {
        if (modele.exigeActe()) throw new RegleMetierException("Ce document s'appuie sur un acte du registre");
        if (!enAttenteDeRecherche()) throw new RegleMetierException("Cette demande n'est pas en attente de recherche");
        noteRecherche = null;
        changer(ACTE_TROUVE, auteur, "Document établi sur comparution devant l'officier : aucun acte à rechercher", maintenant);
    }

    public void marquerIntrouvable(String note, Acteur auteur, Instant maintenant) {
        if (!enAttenteDeRecherche()) throw new RegleMetierException("Cette demande n'est pas en attente de recherche");
        noteRecherche = note;
        if (statut == ACTE_INTROUVABLE) {
            tracer(ACTE_INTROUVABLE, ACTE_INTROUVABLE, auteur, note, maintenant); // nouvelle tentative, sans notification
        } else {
            changer(ACTE_INTROUVABLE, auteur, note, maintenant);
        }
    }

    // ---------------------------------------------------------------- officier

    public void contesterActe(String raison, Acteur officier, Instant maintenant) {
        exiger(officier, Habilitation.DOCUMENTS_SIGNER);
        String r = PersonneConcernee.t(raison);
        if (r == null) throw new RegleMetierException("Expliquez pourquoi l'acte ne convient pas");
        if (statut != ACTE_TROUVE) throw new RegleMetierException("Aucun acte n'est rattaché à cette demande");
        noteRecherche = "Rattachement contesté par l'officier : " + r;
        acteId = null;
        numeroActe = null;
        changer(ACTE_INTROUVABLE, officier, r, maintenant);
    }

    public void verifierSignable(Acteur officier) {
        exiger(officier, Habilitation.DOCUMENTS_SIGNER);
        if (statut != ACTE_TROUVE) throw new RegleMetierException("Cette demande n'est pas prête à être signée");
    }

    public void signer(String code, Acteur officier, Instant maintenant) {
        verifierSignable(officier);
        signataire = officier.nomComplet();
        dateSignature = maintenant;
        codeVerification = code;
        changer(SIGNEE, officier, "Code de vérification " + code, maintenant);
    }

    // ---------------------------------------------------------------- interne

    private static void exiger(Acteur acteur, Habilitation habilitation) {
        if (!acteur.peut(habilitation)) throw new RegleMetierException("Vos habilitations ne permettent pas cette action");
    }

    private void changer(StatutDemande nouveau, Acteur auteur, String commentaire, Instant maintenant) {
        if (!statut.peutPasserA(nouveau)) {
            throw new RegleMetierException("Impossible de passer une demande « " + statut.libelle()
                    + " » à « " + nouveau.libelle() + " »");
        }
        StatutDemande ancien = statut;
        statut = nouveau;
        tracer(ancien, nouveau, auteur, commentaire, maintenant);
    }

    private void tracer(StatutDemande ancien, StatutDemande nouveau, Acteur auteur, String commentaire, Instant maintenant) {
        ChangementStatut c = new ChangementStatut(UUID.randomUUID(), ancien, nouveau, auteur.signature(), commentaire, maintenant);
        historique.add(c);
        nouveauxChangements.add(c);
        dateModification = maintenant;
    }

    /** Changements survenus depuis le chargement, à enregistrer et à publier ; la liste est vidée. */
    public List<ChangementStatut> extraireNouveauxChangements() {
        List<ChangementStatut> copie = List.copyOf(nouveauxChangements);
        nouveauxChangements.clear();
        return copie;
    }

    public List<ChangementStatut> nouveauxChangements() { return List.copyOf(nouveauxChangements); }

    public UUID id() { return id; }
    public String reference() { return reference; }
    public Demandeur demandeur() { return demandeur; }
    public Long typeDocumentId() { return typeDocumentId; }
    public String libelleDocument() { return libelleDocument; }
    public TypeActe typeActe() { return typeActe; }
    public ModeleDocument modele() { return modele; }
    public OrigineActe origine() { return origine; }
    public boolean copieIntegrale() { return modele == ModeleDocument.COPIE_INTEGRALE; }
    public int montant() { return montant; }
    public Long motifId() { return motifId; }
    public String libelleMotif() { return libelleMotif; }
    public LienDemandeur lien() { return lien; }
    public FormatDocument format() { return format; }
    public String adresseLivraison() { return adresseLivraison; }
    public String villeLivraison() { return villeLivraison; }
    public PersonneConcernee personne() { return personne; }
    public ComplementsDemande complements() { return complements; }
    public Integer dureeValiditeJours() { return dureeValiditeJours; }
    public Instant dateCreation() { return dateCreation; }
    public StatutDemande statut() { return statut; }
    public UUID acteId() { return acteId; }
    public String numeroActe() { return numeroActe; }
    public String noteRecherche() { return noteRecherche; }
    public String signataire() { return signataire; }
    public Instant dateSignature() { return dateSignature; }
    public String codeVerification() { return codeVerification; }
    public String motifRejet() { return motifRejet; }
    public Instant dateModification() { return dateModification; }
    public Integer version() { return version; }
    public List<ChangementStatut> historique() { return List.copyOf(historique); }
}
