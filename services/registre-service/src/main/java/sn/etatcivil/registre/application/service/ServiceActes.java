package sn.etatcivil.registre.application.service;

import sn.etatcivil.registre.application.CasDUsage;
import sn.etatcivil.registre.application.port.entree.*;
import sn.etatcivil.registre.application.port.sortie.ActeRepository;
import sn.etatcivil.registre.application.port.sortie.EvenementsRegistre;
import sn.etatcivil.registre.application.port.sortie.Horloge;
import sn.etatcivil.registre.application.port.sortie.Numerotation;
import sn.etatcivil.registre.domaine.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static sn.etatcivil.registre.domaine.Normalisation.normaliser;

@CasDUsage
public class ServiceActes implements TranscrireActe, ConsulterActes {

    private final ActeRepository actes;
    private final Numerotation numerotation;
    private final EvenementsRegistre evenements;
    private final Horloge horloge;

    public ServiceActes(ActeRepository actes, Numerotation numerotation, EvenementsRegistre evenements, Horloge horloge) {
        this.actes = actes;
        this.numerotation = numerotation;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    @Override
    public Acte transcrire(DonneesActe donnees, String agent) {
        if (donnees.typeActe() == null || donnees.dateEvenement() == null) {
            throw new RegleMetierException("Le type d'acte et la date de l'événement sont obligatoires");
        }
        String numero = numerotation.attribuer(donnees.typeActe(), donnees.dateEvenement().getYear());
        return actes.enregistrer(Acte.transcrire(donnees, numero, agent, horloge.maintenant()));
    }

    @Override
    public Acte corriger(UUID id, DonneesActe donnees) {
        Acte acte = obtenir(id);
        acte.corriger(donnees, horloge.maintenant());
        return actes.enregistrer(acte);
    }

    @Override
    public Acte valider(UUID id) {
        Acte acte = obtenir(id);
        acte.valider(horloge.maintenant());
        Acte enregistre = actes.enregistrer(acte);
        evenements.acteValide(enregistre);
        return enregistre;
    }

    @Override
    public Acte obtenir(UUID id) {
        return actes.parId(id).orElseThrow(() -> new ActeIntrouvableException(id));
    }

    @Override
    public Acte parNumero(String numero) {
        return actes.parNumero(numero.trim().toUpperCase()).orElseThrow(() -> new ActeIntrouvableException(numero));
    }

    @Override
    public PageResultat<Acte> lister(FiltreActes filtre, PageDemandee page) {
        return actes.lister(filtre, page);
    }

    @Override
    public ResultatRecherche rechercher(CriteresRecherche c) {
        return ResultatRecherche.de(actes.candidats(c.typeActe(), normaliser(c.nom()), normaliser(c.prenom()))
                .stream().filter(a -> a.correspond(c)).toList());
    }

    @Override
    public Statistiques statistiques() {
        Map<TypeActe, Long> parType = new EnumMap<>(TypeActe.class);
        for (TypeActe t : TypeActe.values()) parType.put(t, actes.compterParType(t));
        return new Statistiques(actes.compter(), actes.compterParStatut(StatutActe.SAISI),
                actes.compterParStatut(StatutActe.VALIDE), parType);
    }
}
