package sn.etatcivil.demande.application.service;

import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.entree.ConsulterPiecesIdentite;
import sn.etatcivil.demande.application.port.sortie.Horloge;
import sn.etatcivil.demande.application.port.sortie.PiecesIdentiteRepository;
import sn.etatcivil.demande.domaine.*;

import java.util.List;
import java.util.UUID;

@CasDUsage
public class ServicePiecesIdentite implements ConsulterPiecesIdentite {

    private final PiecesIdentiteRepository pieces;
    private final Horloge horloge;

    public ServicePiecesIdentite(PiecesIdentiteRepository pieces, Horloge horloge) {
        this.pieces = pieces;
        this.horloge = horloge;
    }

    @Override
    public List<PieceIdentite> pieces(UUID demandeId) {
        return pieces.pourDemande(demandeId);
    }

    /** Seuls ceux qui vérifient l'identité voient les photos : validation, signature, supervision. */
    @Override
    public byte[] contenu(UUID demandeId, UUID pieceId, Acteur agent) {
        if (!agent.peut(Habilitation.DEMANDES_VALIDER) && !agent.peut(Habilitation.DOCUMENTS_SIGNER)
                && !agent.peut(Habilitation.DEMANDES_SUPERVISER)) {
            throw new RegleMetierException("Vos habilitations ne permettent pas de consulter les pièces d'identité");
        }
        return pieces.contenu(demandeId, pieceId)
                .orElseThrow(() -> new RegleMetierException("Cette photo n'existe pas ou a été effacée après la clôture de la demande"));
    }

    @Override
    public int purger() {
        var maintenant = horloge.maintenant();
        return pieces.purger(ServiceDemandes.CLOSES, maintenant.minus(ServiceDemandes.CONSERVATION_PIECES), maintenant);
    }
}
