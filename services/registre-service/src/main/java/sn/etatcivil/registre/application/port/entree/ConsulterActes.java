package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.CriteresRecherche;
import sn.etatcivil.registre.domaine.Personne;
import sn.etatcivil.registre.domaine.ResultatRecherche;
import sn.etatcivil.registre.domaine.SituationCivile;

import java.util.List;
import java.util.UUID;

public interface ConsulterActes {
    Acte obtenir(UUID id);

    Acte parNumero(String numero);

    PageResultat<Acte> lister(FiltreActes filtre, PageDemandee page);

    /** Recherche d'un acte validé correspondant à une demande de citoyen. */
    ResultatRecherche rechercher(CriteresRecherche criteres);

    /** Mariages (dissous ou non) et décès d'une personne, d'après les actes validés. */
    SituationCivile situation(Personne personne);

    /** Naissances validées des enfants de ce couple (portant le nom de famille du père). */
    List<Acte> enfants(Personne pere, Personne mere);

    Statistiques statistiques();

    /** Opérations faites sur l'acte, de la plus récente à la plus ancienne. */
    List<sn.etatcivil.registre.domaine.EntreeJournal> journal(UUID id);

    /** Référentiel des communes, par région. */
    List<sn.etatcivil.registre.domaine.Commune> communes();
}
