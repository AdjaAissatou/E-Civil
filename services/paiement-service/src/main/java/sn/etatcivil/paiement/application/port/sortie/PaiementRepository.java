package sn.etatcivil.paiement.application.port.sortie;

import sn.etatcivil.paiement.domaine.Paiement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaiementRepository {
    void enregistrer(Paiement paiement);

    Optional<Paiement> parToken(String token);

    List<Paiement> deLaDemande(UUID demandeId);
}
