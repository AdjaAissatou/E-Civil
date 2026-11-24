package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.PiecesIdentiteRepository;
import sn.etatcivil.demande.domaine.PieceIdentite;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
class PiecesIdentiteSql implements PiecesIdentiteRepository {

    private final JdbcClient sql;

    PiecesIdentiteSql(JdbcClient sql) {
        this.sql = sql;
    }

    @Override
    public void enregistrer(PieceIdentite p, byte[] contenu) {
        sql.sql("INSERT INTO piece_identite (id, demande_id, nature, type_piece, content_type, taille, empreinte, contenu, date_depot) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .params(p.id(), p.demandeId(), p.nature().name(), p.typePiece().name(), p.contentType(), p.taille(),
                        p.empreinte(), contenu, Timestamp.from(p.dateDepot()))
                .update();
    }

    @Override
    public List<PieceIdentite> pourDemande(UUID demandeId) {
        return sql.sql("SELECT id, demande_id, nature, type_piece, content_type, taille, empreinte, date_depot, date_purge "
                        + "FROM piece_identite WHERE demande_id = ? ORDER BY nature")
                .param(demandeId)
                .query((r, i) -> new PieceIdentite(r.getObject("id", UUID.class), r.getObject("demande_id", UUID.class),
                        PieceIdentite.Nature.valueOf(r.getString("nature")), PieceIdentite.TypePiece.valueOf(r.getString("type_piece")),
                        r.getString("content_type"), r.getInt("taille"), r.getString("empreinte"),
                        r.getTimestamp("date_depot").toInstant(),
                        r.getTimestamp("date_purge") == null ? null : r.getTimestamp("date_purge").toInstant()))
                .list().stream().sorted(Comparator.comparing(PieceIdentite::nature)).toList();
    }

    @Override
    public Optional<byte[]> contenu(UUID demandeId, UUID pieceId) {
        return sql.sql("SELECT contenu FROM piece_identite WHERE id = ? AND demande_id = ? AND contenu IS NOT NULL")
                .params(pieceId, demandeId).query((r, i) -> r.getBytes("contenu")).optional();
    }

    @Override
    public int purger(Set<StatutDemande> statuts, Instant avant, Instant maintenant) {
        if (statuts.isEmpty()) return 0;
        String liste = String.join(",", Collections.nCopies(statuts.size(), "?"));
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.from(maintenant));
        statuts.forEach(s -> params.add(s.name()));
        params.add(Timestamp.from(avant));
        return sql.sql("UPDATE piece_identite SET contenu = NULL, date_purge = ? WHERE contenu IS NOT NULL AND demande_id IN "
                        + "(SELECT id FROM demande WHERE statut IN (" + liste + ") AND date_modification < ?)")
                .params(params).update();
    }
}
