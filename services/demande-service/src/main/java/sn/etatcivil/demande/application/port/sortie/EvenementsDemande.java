package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.ChangementStatut;
import sn.etatcivil.demande.domaine.Demande;

public interface EvenementsDemande {
    void statutChange(Demande demande, ChangementStatut changement);
}
