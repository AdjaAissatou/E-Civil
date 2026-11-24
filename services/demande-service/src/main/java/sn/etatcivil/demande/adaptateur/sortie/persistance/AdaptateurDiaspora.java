package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.ReferentielDiaspora;
import sn.etatcivil.demande.domaine.MissionDiplomatique;
import sn.etatcivil.demande.domaine.Pays;

import java.util.*;

@Component
class AdaptateurDiaspora implements ReferentielDiaspora {

    private final JdbcClient sql;

    AdaptateurDiaspora(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public List<Pays> pays() {
        return sql.sql("SELECT p.id, p.nom, p.code_iso, p.continent, p.capitale, z.nom AS zone FROM pays p "
                        + "LEFT JOIN zone_competence z ON z.id = p.zone_id ORDER BY p.nom")
                .query((r, i) -> new Pays(r.getLong("id"), r.getString("nom"), r.getString("code_iso"),
                        r.getString("continent"), r.getString("capitale"), r.getString("zone"))).list();
    }

    @Override
    public List<String> typesMission() {
        return sql.sql("SELECT libelle FROM type_mission ORDER BY id").query(String.class).list();
    }

    @Override
    public List<String> zones() {
        return sql.sql("SELECT nom FROM zone_competence ORDER BY id").query(String.class).list();
    }

    @Override
    public List<MissionDiplomatique> missions() {
        Map<Long, List<Long>> couverts = new HashMap<>();
        sql.sql("SELECT mission_id, pays_id FROM mission_pays").query((r, i) -> {
            couverts.computeIfAbsent(r.getLong("mission_id"), k -> new ArrayList<>()).add(r.getLong("pays_id"));
            return null;
        }).list();
        return sql.sql("SELECT m.*, t.libelle AS type, p.nom AS pays FROM mission_diplomatique m "
                        + "JOIN type_mission t ON t.id = m.type_id JOIN pays p ON p.id = m.pays_id ORDER BY p.nom, m.nom")
                .query((r, i) -> new MissionDiplomatique(r.getLong("id"), r.getString("type"), r.getString("nom"),
                        r.getLong("pays_id"), r.getString("pays"), r.getString("ville"), r.getString("code_postal"),
                        r.getString("competence"), r.getString("adresse"), r.getString("telephone"), r.getString("email"),
                        couverts.getOrDefault(r.getLong("id"), List.of()), r.getBoolean("active"))).list();
    }

    @Override
    public Optional<MissionDiplomatique> mission(Long id) {
        return missions().stream().filter(m -> m.id().equals(id)).findFirst();
    }

    @Override
    public Long enregistrer(MissionDiplomatique m) {
        Long type = sql.sql("SELECT id FROM type_mission WHERE libelle = ?").param(m.type()).query(Long.class).single();
        Long id = m.id();
        if (id == null) {
            var cle = new GeneratedKeyHolder();
            sql.sql("INSERT INTO mission_diplomatique (type_id, nom, pays_id, ville, code_postal, competence, adresse, telephone, email, active) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    .params(type, m.nom().trim(), m.paysId(), m.ville().trim(), m.codePostal(), m.competence(), m.adresse().trim(),
                            m.telephone(), m.email(), m.active())
                    .update(cle, "id");
            id = cle.getKey().longValue();
        } else {
            sql.sql("UPDATE mission_diplomatique SET type_id = ?, nom = ?, pays_id = ?, ville = ?, code_postal = ?, competence = ?, "
                            + "adresse = ?, telephone = ?, email = ?, active = ? WHERE id = ?")
                    .params(type, m.nom().trim(), m.paysId(), m.ville().trim(), m.codePostal(), m.competence(), m.adresse().trim(),
                            m.telephone(), m.email(), m.active(), id)
                    .update();
            sql.sql("DELETE FROM mission_pays WHERE mission_id = ?").param(id).update();
        }
        for (Long p : new LinkedHashSet<>(m.paysCouverts())) {
            sql.sql("INSERT INTO mission_pays (mission_id, pays_id) VALUES (?, ?)").params(id, p).update();
        }
        return id;
    }

    @Override
    public Long ajouterPays(String nom, String codeIso, String continent, String capitale, String zone) {
        var cle = new GeneratedKeyHolder();
        sql.sql("INSERT INTO pays (zone_id, nom, code_iso, continent, capitale) VALUES "
                        + "((SELECT id FROM zone_competence WHERE nom = ?), ?, ?, ?, ?)")
                .params(zone, nom, codeIso, continent, capitale).update(cle, "id");
        return cle.getKey().longValue();
    }
}
