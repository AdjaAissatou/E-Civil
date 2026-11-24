package sn.etatcivil.demande.application.service;

import sn.etatcivil.demande.application.CasDUsage;
import sn.etatcivil.demande.application.port.entree.ConsulterDiaspora;
import sn.etatcivil.demande.application.port.sortie.ReferentielDiaspora;
import sn.etatcivil.demande.domaine.MissionDiplomatique;
import sn.etatcivil.demande.domaine.Pays;
import sn.etatcivil.demande.domaine.RegleMetierException;

import java.util.List;
import java.util.Locale;

@CasDUsage
public class ServiceDiaspora implements ConsulterDiaspora {

    private final ReferentielDiaspora referentiel;

    public ServiceDiaspora(ReferentielDiaspora referentiel) {
        this.referentiel = referentiel;
    }

    @Override
    public List<Pays> pays() {
        return referentiel.pays();
    }

    @Override
    public List<String> typesMission() {
        return referentiel.typesMission();
    }

    @Override
    public List<String> zones() {
        return referentiel.zones();
    }

    @Override
    public List<MissionDiplomatique> missions(Long paysId, boolean activesSeulement) {
        return referentiel.missions().stream()
                .filter(m -> !activesSeulement || m.active())
                .filter(m -> paysId == null || m.couvre(paysId))
                .toList();
    }

    @Override
    public MissionDiplomatique enregistrerMission(MissionDiplomatique m) {
        if (!typesMission().contains(m.type())) throw new RegleMetierException("Type de mission inconnu : " + m.type());
        if (pays().stream().noneMatch(p -> p.id().equals(m.paysId()))) throw new RegleMetierException("Pays inconnu");
        Long id = referentiel.enregistrer(m);
        return referentiel.mission(id).orElseThrow();
    }

    @Override
    public Pays ajouterPays(String nom, String codeIso, String continent, String capitale, String zone) {
        if (nom == null || nom.isBlank()) throw new RegleMetierException("Le nom du pays est obligatoire");
        String code = codeIso == null ? "" : codeIso.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z]{3}")) throw new RegleMetierException("Le code ISO du pays compte trois lettres (ex. MAR)");
        if (pays().stream().anyMatch(p -> p.codeIso().equals(code) || p.nom().equalsIgnoreCase(nom.trim()))) {
            throw new RegleMetierException("Ce pays existe déjà");
        }
        if (zone != null && !zone.isBlank() && !zones().contains(zone)) throw new RegleMetierException("Zone de compétence inconnue");
        Long id = referentiel.ajouterPays(nom.trim(), code, continent, capitale, zone);
        return pays().stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow();
    }
}
