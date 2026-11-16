package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.ActeRetrouve;
import sn.etatcivil.demande.domaine.OrigineActe;
import sn.etatcivil.demande.domaine.SituationCivile;
import sn.etatcivil.demande.domaine.PersonneConcernee;
import sn.etatcivil.demande.domaine.TypeActe;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Le registre national des actes (registre-service). */
public interface RegistreCivil {

    enum Resultat { TROUVE, AMBIGU, NON_TROUVE }

    record ResultatRecherche(Resultat resultat, List<ActeRetrouve> actes) {}

    /**
     * @param origine origine exigée de l'acte, ou {@code null} : toute origine
     * @throws RegistreIndisponibleException si le registre ne répond pas
     */
    ResultatRecherche rechercher(TypeActe type, PersonneConcernee personne, OrigineActe origine);

    /** Mariages et décès inscrits au nom de cette personne. @throws RegistreIndisponibleException */
    SituationCivile situation(String nom, String prenom, LocalDate dateNaissance);

    Optional<ActeRetrouve> parNumero(String numero);

    Optional<ActeRetrouve> parId(UUID id);
}
