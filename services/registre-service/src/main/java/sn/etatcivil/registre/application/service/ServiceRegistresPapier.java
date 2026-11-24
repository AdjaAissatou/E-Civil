package sn.etatcivil.registre.application.service;

import sn.etatcivil.registre.application.CasDUsage;
import sn.etatcivil.registre.application.port.entree.GererRegistresPapier;
import sn.etatcivil.registre.application.port.sortie.Horloge;
import sn.etatcivil.registre.application.port.sortie.RegistresPapierRepository;
import sn.etatcivil.registre.domaine.*;

import java.time.ZoneId;
import java.util.List;

@CasDUsage
public class ServiceRegistresPapier implements GererRegistresPapier {

    private static final ZoneId DAKAR = ZoneId.of("Africa/Dakar");

    private final RegistresPapierRepository depot;
    private final Horloge horloge;

    public ServiceRegistresPapier(RegistresPapierRepository depot, Horloge horloge) {
        this.depot = depot;
        this.horloge = horloge;
    }

    @Override
    public List<CentreEtatCivil> centres(String recherche, TypeCentre type) {
        return depot.centres(recherche == null || recherche.isBlank() ? null : recherche.trim(), type);
    }

    @Override
    public CentreEtatCivil centre(long id) {
        return depot.centre(id).orElseThrow(() -> new RegleMetierException("Centre d'état civil inconnu"));
    }

    @Override
    public CentreEtatCivil enregistrerCentre(Long id, String nom, TypeCentre type, String commune, String pays,
                                             Long rattachementId, boolean actif) {
        if (id != null) centre(id);
        CentreEtatCivil c = CentreEtatCivil.nouveau(id, nom, type, commune, pays, rattachementId, actif);
        if (c.rattachementId() != null && centre(c.rattachementId()).type() != TypeCentre.PRINCIPAL) {
            throw new RegleMetierException("Un centre secondaire est rattaché à un centre principal");
        }
        boolean homonyme = depot.centres(c.nom(), null).stream()
                .anyMatch(x -> x.nom().equalsIgnoreCase(c.nom()) && !x.id().equals(id));
        if (homonyme) throw new RegleMetierException("Un centre porte déjà ce nom");
        return centre(depot.enregistrerCentre(c));
    }

    @Override
    public List<RegistrePapier> registres(Long centreId, TypeActe typeActe, Integer annee) {
        return depot.registres(centreId, typeActe, annee);
    }

    @Override
    public RegistrePapier registre(long id) {
        return depot.registre(id).orElseThrow(() -> new RegleMetierException("Registre inconnu"));
    }

    @Override
    public RegistrePapier enregistrerRegistre(Long id, Long centreId, TypeActe typeActe, Integer annee, Exemplaire exemplaire,
                                              String volume, String lieuConservation, EtatRegistre etat, boolean numerise,
                                              String observations) {
        if (id != null) registre(id);
        centre(centreId == null ? -1 : centreId);
        int anneeCourante = horloge.maintenant().atZone(DAKAR).getYear();
        RegistrePapier r = RegistrePapier.nouveau(id, centreId, typeActe, annee, exemplaire, volume, lieuConservation, etat,
                numerise, observations, anneeCourante);
        depot.doublon(r).filter(autre -> !autre.equals(id)).ifPresent(autre -> {
            throw new RegleMetierException("Ce registre est déjà enregistré pour ce centre");
        });
        return registre(depot.enregistrerRegistre(r));
    }
}
