package sn.etatcivil.paiement.application.port.sortie;

import sn.etatcivil.paiement.domaine.DemandePayable;
import sn.etatcivil.paiement.domaine.Paiement;

public interface EvenementsPaiement {
    void paiementConfirme(Paiement paiement, DemandePayable demande);
}
