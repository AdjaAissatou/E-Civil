package sn.etatcivil.registre.application.port.sortie;

import sn.etatcivil.registre.application.port.entree.FiltreActes;
import sn.etatcivil.registre.application.port.entree.PageDemandee;
import sn.etatcivil.registre.application.port.entree.PageResultat;
import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActeRepository {
    Acte enregistrer(Acte acte);

    Optional<Acte> parId(UUID id);

    Optional<Acte> parNumero(String numero);

    /** Actes du type donné dont le titulaire ou le conjoint porte exactement ces nom et prénom normalisés. */
    List<Acte> candidats(TypeActe type, String nomNormalise, String prenomNormalise);

    PageResultat<Acte> lister(FiltreActes filtre, PageDemandee page);

    long compter();

    long compterParStatut(StatutActe statut);

    long compterParType(TypeActe type);
}
