package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.PieceIdentite;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PiecesIdentiteRepository {
    void enregistrer(PieceIdentite piece, byte[] contenu);

    List<PieceIdentite> pourDemande(UUID demandeId);

    /** Contenu de la photo, s'il n'a pas encore été effacé. */
    Optional<byte[]> contenu(UUID demandeId, UUID pieceId);

    /**
     * Efface le contenu des photos des demandes dans l'un de ces statuts et inchangées depuis {@code avant}.
     * @return le nombre de photos effacées
     */
    int purger(Set<StatutDemande> statuts, Instant avant, Instant maintenant);
}
