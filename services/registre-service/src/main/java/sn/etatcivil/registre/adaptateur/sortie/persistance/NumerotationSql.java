package sn.etatcivil.registre.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import sn.etatcivil.registre.application.port.sortie.Numerotation;
import sn.etatcivil.registre.domaine.TypeActe;

/**
 * Compteur par type et par année, verrouillé (SELECT … FOR UPDATE) jusqu'à la fin de la transaction
 * de saisie : deux agents qui transcrivent en même temps obtiennent des numéros distincts.
 */
@Component
class NumerotationSql implements Numerotation {

    private final JdbcTemplate jdbc;

    NumerotationSql(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String attribuer(TypeActe type, int annee) {
        jdbc.update("INSERT INTO numerotation (type_acte, annee, dernier_numero) VALUES (?, ?, 0) ON CONFLICT DO NOTHING",
                type.name(), annee);
        Long dernier = jdbc.queryForObject(
                "SELECT dernier_numero FROM numerotation WHERE type_acte = ? AND annee = ? FOR UPDATE",
                Long.class, type.name(), annee);
        long numero = dernier + 1;
        jdbc.update("UPDATE numerotation SET dernier_numero = ? WHERE type_acte = ? AND annee = ?", numero, type.name(), annee);
        return String.format("%s-%d-%06d", type.prefixe(), annee, numero);
    }
}
