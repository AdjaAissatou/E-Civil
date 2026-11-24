package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.*;

import java.util.List;

/** Centres d'état civil et registres papier qu'ils tiennent : le terrain du chargé de renseignement. */
public interface GererRegistresPapier {

    List<CentreEtatCivil> centres(String recherche, TypeCentre type);

    CentreEtatCivil centre(long id);

    CentreEtatCivil enregistrerCentre(Long id, String nom, TypeCentre type, String commune, String pays, Long rattachementId,
                                      boolean actif);

    /** Registres d'un centre, ou de tous les centres pour un type d'actes et une année (critères facultatifs). */
    List<RegistrePapier> registres(Long centreId, TypeActe typeActe, Integer annee);

    RegistrePapier registre(long id);

    RegistrePapier enregistrerRegistre(Long id, Long centreId, TypeActe typeActe, Integer annee, Exemplaire exemplaire,
                                       String volume, String lieuConservation, EtatRegistre etat, boolean numerise,
                                       String observations);
}
