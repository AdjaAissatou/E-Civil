package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.ActeRetrouve;
import sn.etatcivil.demande.domaine.PersonneConcernee;
import sn.etatcivil.demande.domaine.TypeActe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Le registre national des actes (registre-service). */
public interface RegistreCivil {

    enum Resultat { TROUVE, AMBIGU, NON_TROUVE }

    record ResultatRecherche(Resultat resultat, List<ActeRetrouve> actes) {}

    /** @throws RegistreIndisponibleException si le registre ne répond pas */
    ResultatRecherche rechercher(TypeActe type, PersonneConcernee personne);

    Optional<ActeRetrouve> parNumero(String numero);

    Optional<ActeRetrouve> parId(UUID id);
}
