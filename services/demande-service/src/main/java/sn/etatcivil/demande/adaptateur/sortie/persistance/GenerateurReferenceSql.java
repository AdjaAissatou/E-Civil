package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.GenerateurReference;

/** Compteur annuel verrouillé pendant la transaction de dépôt : pas de référence en double. */
@Component
class GenerateurReferenceSql implements GenerateurReference {

    private final JdbcTemplate jdbc;

    GenerateurReferenceSql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String suivante(int annee) {
        jdbc.update("INSERT INTO compteur_reference (annee, dernier_numero) VALUES (?, 0) ON CONFLICT DO NOTHING", annee);
        Long dernier = jdbc.queryForObject("SELECT dernier_numero FROM compteur_reference WHERE annee = ? FOR UPDATE",
                Long.class, annee);
        long numero = dernier + 1;
        jdbc.update("UPDATE compteur_reference SET dernier_numero = ? WHERE annee = ?", numero, annee);
        return String.format("DEM-%d-%06d", annee, numero);
    }
}
