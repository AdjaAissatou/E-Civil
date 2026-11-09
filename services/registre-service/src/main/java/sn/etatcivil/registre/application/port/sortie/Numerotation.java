package sn.etatcivil.registre.application.port.sortie;

import sn.etatcivil.registre.domaine.TypeActe;

/** Attribue le prochain numéro d'acte du type et de l'année, sans doublon même en saisie simultanée. */
public interface Numerotation {
    String attribuer(TypeActe type, int annee);
}
