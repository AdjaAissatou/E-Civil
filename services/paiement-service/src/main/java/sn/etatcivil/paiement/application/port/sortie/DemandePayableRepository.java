package sn.etatcivil.paiement.application.port.sortie;

import sn.etatcivil.paiement.domaine.DemandePayable;

import java.util.Optional;
import java.util.UUID;

public interface DemandePayableRepository {
    void enregistrer(DemandePayable demande);

    Optional<DemandePayable> parId(UUID demandeId);
}
