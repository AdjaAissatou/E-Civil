package sn.etatcivil.registre.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import sn.etatcivil.registre.application.port.sortie.JournalRegistre;
import sn.etatcivil.registre.application.port.sortie.ReferentielCommunes;
import sn.etatcivil.registre.domaine.ActionJournal;
import sn.etatcivil.registre.domaine.Commune;
import sn.etatcivil.registre.domaine.EntreeJournal;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/** Journal des opérations sur les actes et référentiel des communes (tables en lecture/ajout seulement). */
@Component
class JournalSql implements JournalRegistre, ReferentielCommunes {

    private final JdbcClient sql;

    JournalSql(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public void enregistrer(EntreeJournal e) {
        sql.sql("INSERT INTO journal_acte (acte_id, date_operation, auteur, action, detail) VALUES (?, ?, ?, ?, ?)")
                .params(e.acteId(), Timestamp.from(e.date()), e.auteur(), e.action().name(), e.detail()).update();
    }

    @Override
    public List<EntreeJournal> pourActe(UUID acteId) {
        return sql.sql("SELECT acte_id, date_operation, auteur, action, detail FROM journal_acte WHERE acte_id = ? "
                        + "ORDER BY date_operation DESC, id DESC")
                .param(acteId)
                .query((r, i) -> new EntreeJournal(r.getObject("acte_id", UUID.class), r.getTimestamp("date_operation").toInstant(),
                        r.getString("auteur"), ActionJournal.valueOf(r.getString("action")), r.getString("detail")))
                .list();
    }

    @Override
    public List<Commune> toutes() {
        return sql.sql("SELECT nom, departement, region FROM commune ORDER BY region, nom")
                .query((r, i) -> new Commune(r.getString("nom"), r.getString("departement"), r.getString("region")))
                .list();
    }
}
