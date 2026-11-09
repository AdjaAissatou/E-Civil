package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.CriteresRecherche;
import sn.etatcivil.registre.domaine.ResultatRecherche;

import java.util.UUID;

public interface ConsulterActes {
    Acte obtenir(UUID id);

    Acte parNumero(String numero);

    PageResultat<Acte> lister(FiltreActes filtre, PageDemandee page);

    /** Recherche d'un acte validé correspondant à une demande de citoyen. */
    ResultatRecherche rechercher(CriteresRecherche criteres);

    Statistiques statistiques();
}
