package sn.etatcivil.registre.domaine;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import static sn.etatcivil.registre.domaine.Normalisation.*;

/** Agrégat : un acte du registre national, avec ses règles de saisie, de correction et de validation. */
public class Acte {

    private final UUID id;
    private final String numeroActe;
    private final int annee;
    private StatutActe statut;
    private DonneesActe donnees;
    private final String agentSaisie;
    private final Instant dateSaisie;
    private Instant dateModification;
    private final Integer version;

    /** Reconstitution depuis le stockage (aucune règle n'est rejouée). */
    public Acte(UUID id, String numeroActe, int annee, StatutActe statut, DonneesActe donnees,
                String agentSaisie, Instant dateSaisie, Instant dateModification, Integer version) {
        this.id = Objects.requireNonNull(id);
        this.numeroActe = numeroActe;
        this.annee = annee;
        this.statut = statut;
        this.donnees = donnees;
        this.agentSaisie = agentSaisie;
        this.dateSaisie = dateSaisie;
        this.dateModification = dateModification;
        this.version = version;
    }

    /** Transcription d'un acte du registre papier ; le numéro est attribué par le registre. */
    public static Acte transcrire(DonneesActe donnees, String numeroActe, String agent, Instant maintenant) {
        donnees.verifierCoherence();
        return new Acte(UUID.randomUUID(), numeroActe, donnees.dateEvenement().getYear(), StatutActe.SAISI,
                donnees.completer(), agent, maintenant, maintenant, null);
    }

    /** Un acte saisi se corrige librement ; un acte validé n'accepte plus que des mentions marginales. */
    public void corriger(DonneesActe nouvelles, Instant maintenant) {
        if (nouvelles.typeActe() != donnees.typeActe()) {
            throw new RegleMetierException("Le type d'un acte ne peut pas être changé");
        }
        if (statut == StatutActe.VALIDE) {
            donnees = donnees.avecMentions(nouvelles.mentionsMarginales(), nouvelles.observations());
        } else {
            nouvelles.verifierCoherence();
            if (nouvelles.dateEvenement().getYear() != annee) {
                throw new RegleMetierException("L'année de l'événement détermine le numéro de l'acte et ne peut pas changer");
            }
            donnees = nouvelles.completer();
        }
        dateModification = maintenant;
    }

    public void valider(Instant maintenant) {
        if (statut == StatutActe.VALIDE) throw new RegleMetierException("Cet acte est déjà validé");
        statut = StatutActe.VALIDE;
        dateModification = maintenant;
    }

    /**
     * Un acte validé correspond si la personne recherchée en est le titulaire (ou le conjoint pour
     * un mariage/divorce) et si chaque critère facultatif fourni concorde avec ce que l'acte indique.
     */
    public boolean correspond(CriteresRecherche r) {
        if (statut != StatutActe.VALIDE || r.typeActe() != donnees.typeActe()) return false;
        DonneesActe a = donnees;
        boolean titulaire = equivalents(a.nom(), r.nom()) && equivalents(a.prenom(), r.prenom());
        boolean conjoint = a.typeActe().concerneUnCouple()
                && equivalents(a.nomConjoint(), r.nom()) && equivalents(a.prenomConjoint(), r.prenom());
        if (!titulaire && !conjoint) return false;
        LocalDate naissance = titulaire ? a.dateNaissance() : a.dateNaissanceConjoint();
        String lieu = titulaire ? a.lieuNaissance() : a.lieuNaissanceConjoint();
        return concorde(naissance, r.dateNaissance())
                && concorde(a.dateEvenement(), r.dateEvenement())
                && (vide(lieu) || vide(r.lieuNaissance()) || lieuxCompatibles(lieu, r.lieuNaissance()))
                && (!titulaire || texte(a.nomPere(), r.nomPere()))
                && (!titulaire || texte(a.prenomPere(), r.prenomPere()))
                && (!titulaire || texte(a.nomMere(), r.nomMere()))
                && (!titulaire || texte(a.prenomMere(), r.prenomMere()))
                && texte(titulaire ? a.nomConjoint() : a.nom(), r.nomConjoint())
                && texte(titulaire ? a.prenomConjoint() : a.prenom(), r.prenomConjoint());
    }

    private static boolean concorde(Object valeurActe, Object critere) {
        return critere == null || valeurActe == null || valeurActe.equals(critere);
    }

    private static boolean texte(String valeurActe, String critere) {
        return vide(critere) || vide(valeurActe) || equivalents(valeurActe, critere);
    }

    private static boolean vide(String s) {
        return DonneesActe.vide(s);
    }

    public String nomNormalise() { return normaliser(donnees.nom()); }
    public String prenomNormalise() { return normaliser(donnees.prenom()); }
    public String nomConjointNormalise() { return normaliser(donnees.nomConjoint()); }
    public String prenomConjointNormalise() { return normaliser(donnees.prenomConjoint()); }

    public UUID id() { return id; }
    public String numeroActe() { return numeroActe; }
    public int annee() { return annee; }
    public StatutActe statut() { return statut; }
    public DonneesActe donnees() { return donnees; }
    public TypeActe typeActe() { return donnees.typeActe(); }
    public String agentSaisie() { return agentSaisie; }
    public Instant dateSaisie() { return dateSaisie; }
    public Instant dateModification() { return dateModification; }
    public Integer version() { return version; }
}
