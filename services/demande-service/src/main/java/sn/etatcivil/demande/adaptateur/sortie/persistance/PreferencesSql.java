package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.PreferencesDemandeurs;

@Component
class PreferencesSql implements PreferencesDemandeurs {

    private final JdbcClient sql;

    PreferencesSql(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public boolean notifications(String demandeurId) {
        return sql.sql("SELECT notifications FROM preference_demandeur WHERE demandeur_id = ?").param(demandeurId)
                .query(Boolean.class).optional().orElse(true);
    }

    @Override
    public void definir(String demandeurId, boolean notifications) {
        int n = sql.sql("UPDATE preference_demandeur SET notifications = ? WHERE demandeur_id = ?")
                .params(notifications, demandeurId).update();
        if (n == 0) {
            sql.sql("INSERT INTO preference_demandeur (demandeur_id, notifications) VALUES (?, ?)")
                    .params(demandeurId, notifications).update();
        }
    }
}
