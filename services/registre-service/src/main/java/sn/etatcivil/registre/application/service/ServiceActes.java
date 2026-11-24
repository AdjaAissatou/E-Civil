package sn.etatcivil.registre.application.service;

import sn.etatcivil.registre.application.CasDUsage;
import sn.etatcivil.registre.application.port.entree.*;
import sn.etatcivil.registre.application.port.sortie.ActeRepository;
import sn.etatcivil.registre.application.port.sortie.EvenementsRegistre;
import sn.etatcivil.registre.application.port.sortie.Horloge;
import sn.etatcivil.registre.application.port.sortie.Numerotation;
import sn.etatcivil.registre.application.port.sortie.JournalRegistre;
import sn.etatcivil.registre.application.port.sortie.ReferentielCommunes;
import sn.etatcivil.registre.domaine.*;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static sn.etatcivil.registre.domaine.Normalisation.normaliser;

@CasDUsage
public class ServiceActes implements TranscrireActe, ConsulterActes {

    private final ActeRepository actes;
    private final Numerotation numerotation;
    private final EvenementsRegistre evenements;
    private final Horloge horloge;
    private final JournalRegistre journal;
    private final ReferentielCommunes communes;

    public ServiceActes(ActeRepository actes, Numerotation numerotation, EvenementsRegistre evenements, Horloge horloge,
                        JournalRegistre journal, ReferentielCommunes communes) {
        this.actes = actes;
        this.numerotation = numerotation;
        this.evenements = evenements;
        this.horloge = horloge;
        this.journal = journal;
        this.communes = communes;
    }

    @Override
    public Acte transcrire(DonneesActe donnees, String agent) {
        if (donnees.typeActe() == null || donnees.dateEvenement() == null) {
            throw new RegleMetierException("Le type d'acte et la date de l'événement sont obligatoires");
        }
        String numero = numerotation.attribuer(donnees.typeActe(), donnees.dateEvenement().getYear());
        Acte acte = actes.enregistrer(Acte.transcrire(donnees, numero, agent, horloge.maintenant()));
        tracer(acte, agent, ActionJournal.TRANSCRIPTION, "Acte " + numero + " saisi");
        return acte;
    }

    @Override
    public Acte corriger(UUID id, DonneesActe donnees, String agent) {
        Acte acte = obtenir(id);
        DonneesActe avant = acte.donnees();
        acte.corriger(donnees, horloge.maintenant());
        List<String> changements = avant.differences(acte.donnees());
        Acte enregistre = actes.enregistrer(acte);
        if (!changements.isEmpty()) tracer(enregistre, agent, ActionJournal.CORRECTION, String.join("\n", changements));
        return enregistre;
    }

    @Override
    public Acte valider(UUID id, String agent) {
        Acte acte = obtenir(id);
        acte.valider(horloge.maintenant());
        Acte enregistre = actes.enregistrer(acte);
        tracer(enregistre, agent, ActionJournal.VALIDATION, "Acte validé : il est délivrable et ne peut plus être modifié");
        evenements.acteValide(enregistre);
        return enregistre;
    }

    @Override
    public Acte ajouterMention(UUID id, TypeMention type, java.time.LocalDate date, String reference, String details,
                               String agent) {
        Acte acte = obtenir(id);
        Mention mention = new Mention(type, date, reference, details, agent, horloge.maintenant());
        acte.ajouterMention(mention, horloge.maintenant());
        Acte enregistre = actes.enregistrer(acte);
        tracer(enregistre, agent, ActionJournal.MENTION, mention.texte());
        return enregistre;
    }

    @Override
    public List<EntreeJournal> journal(UUID id) {
        obtenir(id);
        return journal.pourActe(id);
    }

    @Override
    public List<Commune> communes() {
        return communes.toutes();
    }

    private void tracer(Acte acte, String agent, ActionJournal action, String detail) {
        journal.enregistrer(new EntreeJournal(acte.id(), horloge.maintenant(), agent, action, detail));
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
    public SituationCivile situation(Personne p) {
        return SituationCivile.etablir(p, valides(TypeActe.MARIAGE, p), valides(TypeActe.DIVORCE, p),
                valides(TypeActe.DECES, p), conjoint -> valides(TypeActe.DECES, conjoint));
    }

    @Override
    public List<Acte> enfants(Personne pere, Personne mere) {
        return actes.naissancesParNom(normaliser(pere.nom())).stream()
                .filter(a -> a.estEnfantDe(pere, mere))
                .sorted(Comparator.comparing(a -> a.donnees().dateEvenement()))
                .toList();
    }

    /** Actes validés du type donné où cette personne figure (titulaire ou conjoint). */
    private List<Acte> valides(TypeActe type, Personne p) {
        return actes.candidats(type, normaliser(p.nom()), normaliser(p.prenom())).stream()
                .filter(a -> a.statut() == StatutActe.VALIDE)
                .filter(a -> a.designe(p.nom(), p.prenom(), p.dateNaissance()))
                .toList();
    }

    @Override
    public Statistiques statistiques() {
        Map<TypeActe, Long> parType = new EnumMap<>(TypeActe.class);
        for (TypeActe t : TypeActe.values()) parType.put(t, actes.compterParType(t));
        return new Statistiques(actes.compter(), actes.compterParStatut(StatutActe.SAISI),
                actes.compterParStatut(StatutActe.VALIDE), parType);
    }
}
