package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.MissionDiplomatique;
import sn.etatcivil.demande.domaine.Pays;

import java.util.List;

/** Référentiel de la diaspora : pays, missions diplomatiques et leurs compétences. */
public interface ConsulterDiaspora {
    List<Pays> pays();

    List<String> typesMission();

    List<String> zones();

    /** Missions actives, ou celles qui couvrent ce pays. */
    List<MissionDiplomatique> missions(Long paysId, boolean activesSeulement);

    MissionDiplomatique enregistrerMission(MissionDiplomatique mission);

    Pays ajouterPays(String nom, String codeIso, String continent, String capitale, String zone);
}
