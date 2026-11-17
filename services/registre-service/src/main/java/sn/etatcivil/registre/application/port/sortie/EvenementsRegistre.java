package sn.etatcivil.registre.application.port.sortie;

import sn.etatcivil.registre.domaine.Acte;

/** Informe les autres services des faits survenus dans le registre. */
public interface EvenementsRegistre {
    void acteValide(Acte acte);
}
