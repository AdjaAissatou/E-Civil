package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.DonneesActe;

import java.util.UUID;

/** Cas d'usage des agents : transcrire un acte papier, le corriger, puis le faire valider par un officier. */
public interface TranscrireActe {
    Acte transcrire(DonneesActe donnees, String agent);

    Acte corriger(UUID id, DonneesActe donnees);

    Acte valider(UUID id);
}
