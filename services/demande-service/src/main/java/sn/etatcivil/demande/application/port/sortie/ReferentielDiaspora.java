package sn.etatcivil.demande.application.port.sortie;

import sn.etatcivil.demande.domaine.MissionDiplomatique;
import sn.etatcivil.demande.domaine.Pays;

import java.util.List;
import java.util.Optional;

public interface ReferentielDiaspora {
    List<Pays> pays();

    List<String> typesMission();

    List<String> zones();

    List<MissionDiplomatique> missions();

    Optional<MissionDiplomatique> mission(Long id);

    Long enregistrer(MissionDiplomatique mission);

    Long ajouterPays(String nom, String codeIso, String continent, String capitale, String zone);
}
