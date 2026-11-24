package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.entree.RenseignerDemandes;
import sn.etatcivil.demande.application.port.sortie.RenseignementsRepository;
import sn.etatcivil.demande.domaine.EtatRenseignement;
import sn.etatcivil.demande.domaine.Renseignement;
import sn.etatcivil.demande.domaine.ResultatConsultation;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Dossiers de renseignement et registres consultés (tables renseignement et consultation_registre). */
@Component
class RenseignementsSql implements RenseignementsRepository {

    /** Statuts de demande pour lesquels la recherche de l'acte n'est pas terminée. */
    private static final String EN_ATTENTE = "('PAYEE', 'ACTE_INTROUVABLE')";

    private final JdbcClient sql;

    RenseignementsSql(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public Optional<Renseignement> parDemande(UUID demandeId) {
        List<Renseignement.Consultation> consultations = sql.sql(
                        "SELECT * FROM consultation_registre WHERE demande_id = ? ORDER BY date_consultation, id")
                .param(demandeId)
                .query((r, i) -> new Renseignement.Consultation(r.getLong("registre_id"), r.getString("registre"),
                        ResultatConsultation.valueOf(r.getString("resultat")), r.getString("folio"), r.getString("note"),
                        r.getString("agent"), r.getTimestamp("date_consultation").toInstant()))
                .list();
        return sql.sql("SELECT * FROM renseignement WHERE demande_id = ?").param(demandeId)
                .query((r, i) -> new Renseignement(demandeId, instant(r.getTimestamp("date_ouverture")), r.getBoolean("urgent"),
                        r.getString("agent_id"), r.getString("agent_nom"), instant(r.getTimestamp("date_prise_en_charge")),
                        r.getString("motif_non_trouve"), r.getString("agent_conclusion"),
                        instant(r.getTimestamp("date_conclusion")), consultations))
                .optional();
    }

    @Override
    public void enregistrer(Renseignement r) {
        int n = sql.sql("UPDATE renseignement SET urgent = ?, agent_id = ?, agent_nom = ?, date_prise_en_charge = ?, "
                        + "motif_non_trouve = ?, agent_conclusion = ?, date_conclusion = ? WHERE demande_id = ?")
                .params(r.urgent(), r.agentId(), r.agentNom(), ts(r.datePriseEnCharge()), r.motifNonTrouve(),
                        r.agentConclusion(), ts(r.dateConclusion()), r.demandeId())
                .update();
        if (n == 0) {
            sql.sql("INSERT INTO renseignement (demande_id, date_ouverture, urgent, agent_id, agent_nom, date_prise_en_charge, "
                            + "motif_non_trouve, agent_conclusion, date_conclusion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    .params(r.demandeId(), ts(r.dateOuverture()), r.urgent(), r.agentId(), r.agentNom(),
                            ts(r.datePriseEnCharge()), r.motifNonTrouve(), r.agentConclusion(), ts(r.dateConclusion()))
                    .update();
        }
        int dejaEnregistrees = sql.sql("SELECT COUNT(*) FROM consultation_registre WHERE demande_id = ?")
                .param(r.demandeId()).query(Integer.class).single();
        for (Renseignement.Consultation c : r.consultations().subList(dejaEnregistrees, r.consultations().size())) {
            sql.sql("INSERT INTO consultation_registre (demande_id, registre_id, registre, resultat, folio, note, agent, "
                            + "date_consultation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                    .params(r.demandeId(), c.registreId(), c.registre(), c.resultat().name(), c.folio(), c.note(), c.agent(),
                            ts(c.date()))
                    .update();
        }
    }

    @Override
    public List<RenseignerDemandes.Resume> enCours() {
        return sql.sql("SELECT r.demande_id, r.urgent, r.agent_nom, COUNT(c.id) AS nb, MAX(c.date_consultation) AS derniere "
                        + "FROM renseignement r JOIN demande d ON d.id = r.demande_id "
                        + "LEFT JOIN consultation_registre c ON c.demande_id = r.demande_id "
                        + "WHERE r.date_conclusion IS NULL AND d.statut IN " + EN_ATTENTE + " "
                        + "GROUP BY r.demande_id, r.urgent, r.agent_nom")
                .query((x, i) -> new RenseignerDemandes.Resume(x.getObject("demande_id", UUID.class), x.getBoolean("urgent"),
                        x.getString("agent_nom"), x.getInt("nb"), instant(x.getTimestamp("derniere")), EtatRenseignement.EN_COURS))
                .list();
    }

    @Override
    public RenseignerDemandes.Statistiques statistiques() {
        return sql.sql("SELECT "
                        + "(SELECT COUNT(*) FROM demande WHERE statut = 'ACTE_INTROUVABLE') AS en_cours, "
                        + "(SELECT COUNT(*) FROM renseignement r JOIN demande d ON d.id = r.demande_id "
                        + "  WHERE r.urgent AND r.date_conclusion IS NULL AND d.statut IN " + EN_ATTENTE + ") AS urgents, "
                        + "(SELECT COUNT(*) FROM renseignement r JOIN demande d ON d.id = r.demande_id "
                        + "  WHERE r.date_conclusion IS NULL AND d.numero_acte IS NOT NULL AND d.statut NOT IN " + EN_ATTENTE + ") AS trouves, "
                        + "(SELECT COUNT(*) FROM renseignement WHERE date_conclusion IS NOT NULL) AS non_trouves")
                .query((x, i) -> new RenseignerDemandes.Statistiques(x.getLong("en_cours"), x.getLong("urgents"),
                        x.getLong("trouves"), x.getLong("non_trouves")))
                .single();
    }

    private static Timestamp ts(Instant i) {
        return i == null ? null : Timestamp.from(i);
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }
}
