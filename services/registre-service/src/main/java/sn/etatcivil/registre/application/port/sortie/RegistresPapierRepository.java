package sn.etatcivil.registre.application.port.sortie;

import sn.etatcivil.registre.domaine.*;

import java.util.List;
import java.util.Optional;

public interface RegistresPapierRepository {

    List<CentreEtatCivil> centres(String recherche, TypeCentre type);

    Optional<CentreEtatCivil> centre(long id);

    long enregistrerCentre(CentreEtatCivil centre);

    List<RegistrePapier> registres(Long centreId, TypeActe typeActe, Integer annee);

    Optional<RegistrePapier> registre(long id);

    /** Registre de même centre, type, année, exemplaire et volume, s'il existe déjà. */
    Optional<Long> doublon(RegistrePapier registre);

    long enregistrerRegistre(RegistrePapier registre);
}
