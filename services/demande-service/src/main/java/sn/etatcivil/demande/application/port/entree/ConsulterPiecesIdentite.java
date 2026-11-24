package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.Acteur;
import sn.etatcivil.demande.domaine.PieceIdentite;

import java.util.List;
import java.util.UUID;

/** Photos d'identité d'une demande : consultées par les agents qui vérifient l'identité, effacées après la clôture. */
public interface ConsulterPiecesIdentite {
    List<PieceIdentite> pieces(UUID demandeId);

    /** Contenu d'une photo ; réservé au délégué, à l'officier et à la supervision. */
    byte[] contenu(UUID demandeId, UUID pieceId, Acteur agent);

    /** Efface les photos des demandes closes depuis plus de 30 jours. @return nombre de photos effacées */
    int purger();
}
