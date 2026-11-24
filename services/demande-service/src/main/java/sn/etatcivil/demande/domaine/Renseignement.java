package sn.etatcivil.demande.domaine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Dossier de renseignement : la recherche, par le chargé de renseignement, d'un acte que le registre national
 * ne contient pas encore. Il garde la trace de chaque registre papier consulté et de ce qui y a été constaté ;
 * s'il conclut que l'acte n'existe pas, la demande est rejetée avec ce motif (le citoyen peut alors saisir le
 * tribunal d'un jugement d'autorisation d'inscription).
 */
public class Renseignement {

    /** Registre papier consulté : son identifiant et son libellé viennent du registre (copie au moment de la consultation). */
    public record Consultation(Long registreId, String registre, ResultatConsultation resultat, String folio, String note,
                               String agent, Instant date) {}

    private final UUID demandeId;
    private final Instant dateOuverture;
    private boolean urgent;
    private String agentId;
    private String agentNom;
    private Instant datePriseEnCharge;
    private String motifNonTrouve;
    private String agentConclusion;
    private Instant dateConclusion;
    private final List<Consultation> consultations;

    public Renseignement(UUID demandeId, Instant dateOuverture, boolean urgent, String agentId, String agentNom,
                         Instant datePriseEnCharge, String motifNonTrouve, String agentConclusion, Instant dateConclusion,
                         List<Consultation> consultations) {
        this.demandeId = demandeId;
        this.dateOuverture = dateOuverture;
        this.urgent = urgent;
        this.agentId = agentId;
        this.agentNom = agentNom;
        this.datePriseEnCharge = datePriseEnCharge;
        this.motifNonTrouve = motifNonTrouve;
        this.agentConclusion = agentConclusion;
        this.dateConclusion = dateConclusion;
        this.consultations = new ArrayList<>(consultations == null ? List.of() : consultations);
    }

    /** Le dossier s'ouvre au premier geste du chargé de renseignement sur une demande dont l'acte manque. */
    public static Renseignement ouvrir(Demande demande, Instant maintenant) {
        if (!demande.enAttenteDeRecherche()) throw new RegleMetierException("Cette demande n'est pas en attente de recherche");
        return new Renseignement(demande.id(), maintenant, false, null, null, null, null, null, null, List.of());
    }

    /** L'état se lit sur la demande : retrouvé si un acte lui est rattaché, en cours tant qu'elle attend la recherche. */
    public EtatRenseignement etat(Demande demande) {
        if (dateConclusion != null) return EtatRenseignement.NON_TROUVE;
        if (demande.enAttenteDeRecherche()) return EtatRenseignement.EN_COURS;
        if (demande.numeroActe() != null) return EtatRenseignement.TROUVE;
        return EtatRenseignement.CLOS;
    }

    public void prendreEnCharge(Acteur agent, Demande demande, Instant maintenant) {
        exigerEnCours(agent, demande);
        agentId = agent.id();
        agentNom = agent.nomComplet();
        datePriseEnCharge = maintenant;
    }

    public void definirPriorite(boolean urgent, Acteur agent, Demande demande) {
        exigerEnCours(agent, demande);
        this.urgent = urgent;
    }

    public void consigner(Long registreId, String registre, ResultatConsultation resultat, String folio, String note,
                          Acteur agent, Demande demande, Instant maintenant) {
        exigerEnCours(agent, demande);
        if (registreId == null || registre == null || registre.isBlank()) throw new RegleMetierException("Choisissez le registre consulté");
        if (resultat == null) throw new RegleMetierException("Indiquez ce que vous avez constaté");
        String f = net(folio, 30, "Le folio");
        String n = net(note, 1000, "La note");
        if (resultat != ResultatConsultation.ABSENT && resultat != ResultatConsultation.ACTE_PRESENT && n == null) {
            throw new RegleMetierException("Décrivez l'état du registre ou de la page dans la note");
        }
        consultations.add(new Consultation(registreId, registre.trim(), resultat, f, n, agent.signature(), maintenant));
        if (agentId == null) prendreEnCharge(agent, demande, maintenant);
    }

    /** Conclusion négative : au moins un registre a été consulté et le motif est expliqué au citoyen. */
    public String conclureNonTrouve(String motif, Acteur agent, Demande demande, Instant maintenant) {
        exigerEnCours(agent, demande);
        if (demande.statut() != StatutDemande.ACTE_INTROUVABLE) {
            throw new RegleMetierException("La recherche automatique dans le registre national doit d'abord avoir échoué");
        }
        if (consultations.isEmpty()) throw new RegleMetierException("Consignez au moins un registre consulté avant de conclure");
        if (consultations.stream().anyMatch(c -> c.resultat() == ResultatConsultation.ACTE_PRESENT)) {
            throw new RegleMetierException("Un registre contient l'acte : transcrivez-le puis rattachez-le à la demande");
        }
        String m = net(motif, 1000, "Le motif");
        if (m == null) throw new RegleMetierException("Expliquez au demandeur pourquoi l'acte n'a pas été trouvé");
        motifNonTrouve = m;
        agentConclusion = agent.signature();
        dateConclusion = maintenant;
        return "Acte introuvable dans les registres papier (" + consultations.size()
                + (consultations.size() > 1 ? " registres consultés" : " registre consulté") + ") : " + m;
    }

    private void exigerEnCours(Acteur agent, Demande demande) {
        if (!agent.peut(Habilitation.DEMANDES_RECHERCHER)) {
            throw new RegleMetierException("Vos habilitations ne permettent pas de rechercher les actes");
        }
        if (!demande.id().equals(demandeId)) throw new IllegalArgumentException("Dossier d'une autre demande");
        if (etat(demande) != EtatRenseignement.EN_COURS) throw new RegleMetierException("Ce dossier de renseignement est clos");
    }

    private static String net(String s, int max, String quoi) {
        if (s == null || s.isBlank()) return null;
        if (s.trim().length() > max) throw new RegleMetierException(quoi + " ne doit pas dépasser " + max + " caractères");
        return s.trim();
    }

    public UUID demandeId() { return demandeId; }
    public Instant dateOuverture() { return dateOuverture; }
    public boolean urgent() { return urgent; }
    public String agentId() { return agentId; }
    public String agentNom() { return agentNom; }
    public Instant datePriseEnCharge() { return datePriseEnCharge; }
    public String motifNonTrouve() { return motifNonTrouve; }
    public String agentConclusion() { return agentConclusion; }
    public Instant dateConclusion() { return dateConclusion; }
    public List<Consultation> consultations() { return Collections.unmodifiableList(consultations); }
}
