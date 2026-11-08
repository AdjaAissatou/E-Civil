package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

/** Chaque critère est facultatif ; {@code nom} est comparé sous forme normalisée et partielle. */
public record FiltreActes(TypeActe type, StatutActe statut, String nom) {
}
