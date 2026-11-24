package sn.etatcivil.registre.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import sn.etatcivil.registre.application.port.sortie.RegistresPapierRepository;
import sn.etatcivil.registre.domaine.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/** Centres d'état civil et registres papier (tables centre_etat_civil et registre_papier). */
@Component
class RegistresPapierSql implements RegistresPapierRepository {

    private static final String CENTRE = "SELECT c.id, c.nom, c.type, c.commune, c.pays, c.rattachement_id, r.nom AS nom_rattachement, "
            + "c.actif FROM centre_etat_civil c LEFT JOIN centre_etat_civil r ON r.id = c.rattachement_id ";

    private static final String REGISTRE = "SELECT g.*, c.nom AS nom_centre FROM registre_papier g "
            + "JOIN centre_etat_civil c ON c.id = g.centre_id ";

    private final JdbcClient sql;

    RegistresPapierSql(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public List<CentreEtatCivil> centres(String recherche, TypeCentre type) {
        StringBuilder q = new StringBuilder(CENTRE + "WHERE 1 = 1 ");
        Map<String, Object> p = new HashMap<>();
        if (recherche != null) {
            q.append("AND (LOWER(c.nom) LIKE :r OR LOWER(c.commune) LIKE :r OR LOWER(c.pays) LIKE :r) ");
            p.put("r", "%" + recherche.toLowerCase(Locale.ROOT) + "%");
        }
        if (type != null) {
            q.append("AND c.type = :t ");
            p.put("t", type.name());
        }
        q.append("ORDER BY c.type, c.nom");
        return sql.sql(q.toString()).params(p).query(this::lireCentre).list();
    }

    @Override
    public Optional<CentreEtatCivil> centre(long id) {
        return sql.sql(CENTRE + "WHERE c.id = ?").param(id).query(this::lireCentre).optional();
    }

    @Override
    public long enregistrerCentre(CentreEtatCivil c) {
        if (c.id() != null) {
            sql.sql("UPDATE centre_etat_civil SET nom = ?, type = ?, commune = ?, pays = ?, rattachement_id = ?, actif = ? WHERE id = ?")
                    .params(c.nom(), c.type().name(), c.commune(), c.pays(), c.rattachementId(), c.actif(), c.id()).update();
            return c.id();
        }
        var cle = new GeneratedKeyHolder();
        sql.sql("INSERT INTO centre_etat_civil (nom, type, commune, pays, rattachement_id, actif) VALUES (?, ?, ?, ?, ?, ?)")
                .params(c.nom(), c.type().name(), c.commune(), c.pays(), c.rattachementId(), c.actif())
                .update(cle, "id");
        return Objects.requireNonNull(cle.getKey()).longValue();
    }

    @Override
    public List<RegistrePapier> registres(Long centreId, TypeActe typeActe, Integer annee) {
        StringBuilder q = new StringBuilder(REGISTRE + "WHERE 1 = 1 ");
        Map<String, Object> p = new HashMap<>();
        if (centreId != null) { q.append("AND g.centre_id = :c "); p.put("c", centreId); }
        if (typeActe != null) { q.append("AND g.type_acte = :t "); p.put("t", typeActe.name()); }
        if (annee != null) { q.append("AND g.annee = :a "); p.put("a", annee); }
        q.append("ORDER BY g.annee DESC, g.type_acte, c.nom, g.exemplaire, g.volume LIMIT 500");
        return sql.sql(q.toString()).params(p).query(this::lireRegistre).list();
    }

    @Override
    public Optional<RegistrePapier> registre(long id) {
        return sql.sql(REGISTRE + "WHERE g.id = ?").param(id).query(this::lireRegistre).optional();
    }

    @Override
    public Optional<Long> doublon(RegistrePapier r) {
        return sql.sql("SELECT id FROM registre_papier WHERE centre_id = ? AND type_acte = ? AND annee = ? AND exemplaire = ? "
                        + "AND volume = ?")
                .params(r.centreId(), r.typeActe().name(), r.annee(), r.exemplaire().name(), r.volume() == null ? "" : r.volume())
                .query(Long.class).optional();
    }

    @Override
    public long enregistrerRegistre(RegistrePapier r) {
        if (r.id() != null) {
            sql.sql("UPDATE registre_papier SET centre_id = ?, type_acte = ?, annee = ?, exemplaire = ?, volume = ?, "
                            + "lieu_conservation = ?, etat = ?, numerise = ?, observations = ? WHERE id = ?")
                    .params(r.centreId(), r.typeActe().name(), r.annee(), r.exemplaire().name(), vol(r), r.lieuConservation(),
                            r.etat().name(), r.numerise(), r.observations(), r.id()).update();
            return r.id();
        }
        var cle = new GeneratedKeyHolder();
        sql.sql("INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, volume, lieu_conservation, etat, numerise, "
                        + "observations) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .params(r.centreId(), r.typeActe().name(), r.annee(), r.exemplaire().name(), vol(r), r.lieuConservation(),
                        r.etat().name(), r.numerise(), r.observations())
                .update(cle, "id");
        return Objects.requireNonNull(cle.getKey()).longValue();
    }

    private static String vol(RegistrePapier r) {
        return r.volume() == null ? "" : r.volume();
    }

    private CentreEtatCivil lireCentre(ResultSet r, int i) throws SQLException {
        long rattachement = r.getLong("rattachement_id");
        Long rattache = r.wasNull() ? null : rattachement;
        return new CentreEtatCivil(r.getLong("id"), r.getString("nom"), TypeCentre.valueOf(r.getString("type")),
                r.getString("commune"), r.getString("pays"), rattache,
                r.getString("nom_rattachement"), r.getBoolean("actif"));
    }

    private RegistrePapier lireRegistre(ResultSet r, int i) throws SQLException {
        return new RegistrePapier(r.getLong("id"), r.getLong("centre_id"), r.getString("nom_centre"),
                TypeActe.valueOf(r.getString("type_acte")), r.getInt("annee"), Exemplaire.valueOf(r.getString("exemplaire")),
                r.getString("volume").isEmpty() ? null : r.getString("volume"), r.getString("lieu_conservation"), EtatRegistre.valueOf(r.getString("etat")),
                r.getBoolean("numerise"), r.getString("observations"));
    }
}
