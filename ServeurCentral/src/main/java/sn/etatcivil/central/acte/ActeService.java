package sn.etatcivil.central.acte;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.central.numerotation.NumerotationService;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static sn.etatcivil.central.acte.Normalisation.normaliser;

@Service
@Transactional
public class ActeService {

    private final ActeRepository repository;
    private final NumerotationService numerotation;

    public ActeService(ActeRepository repository, NumerotationService numerotation) {
        this.repository = repository;
        this.numerotation = numerotation;
    }

    public ActeReponse creer(ActeRequete requete) {
        verifierCoherence(requete);
        Acte acte = new Acte();
        acte.setTypeActe(requete.typeActe());
        acte.setAgentSaisie(requete.agent());
        appliquer(acte, requete);
        int annee = requete.dateEvenement().getYear();
        acte.setAnnee(annee);
        acte.setNumeroActe(numerotation.attribuer(requete.typeActe(), annee));
        acte.setStatut(StatutActe.SAISI);
        return ActeReponse.de(repository.save(acte));
    }

    /** Un acte saisi peut être corrigé librement ; un acte validé n'accepte plus que des mentions. */
    public ActeReponse modifier(Long id, ActeRequete requete) {
        Acte acte = trouver(id);
        if (acte.getTypeActe() != requete.typeActe()) {
            throw new RegleMetierException("Le type d'un acte ne peut pas être changé");
        }
        if (acte.getStatut() == StatutActe.VALIDE) {
            acte.setMentionsMarginales(requete.mentionsMarginales());
            acte.setObservations(requete.observations());
        } else {
            verifierCoherence(requete);
            if (requete.dateEvenement().getYear() != acte.getAnnee()) {
                throw new RegleMetierException("L'année de l'événement détermine le numéro de l'acte et ne peut pas changer");
            }
            appliquer(acte, requete);
        }
        return ActeReponse.de(acte);
    }

    public ActeReponse valider(Long id) {
        Acte acte = trouver(id);
        if (acte.getStatut() != StatutActe.SAISI) {
            throw new RegleMetierException("Cet acte est déjà validé");
        }
        acte.setStatut(StatutActe.VALIDE);
        return ActeReponse.de(acte);
    }

    @Transactional(readOnly = true)
    public ActeReponse obtenir(Long id) {
        return ActeReponse.de(trouver(id));
    }

    @Transactional(readOnly = true)
    public ActeReponse parNumero(String numero) {
        return repository.findByNumeroActe(numero.trim().toUpperCase())
                .map(ActeReponse::de)
                .orElseThrow(() -> new ActeIntrouvableException(numero));
    }

    @Transactional(readOnly = true)
    public Page<ActeReponse> lister(TypeActe type, StatutActe statut, String nom, Pageable pageable) {
        // Seuls les filtres renseignés sont ajoutés à la requête
        Specification<Acte> filtre = (racine, requete, cb) -> cb.conjunction();
        if (type != null) filtre = filtre.and((a, q, cb) -> cb.equal(a.get("typeActe"), type));
        if (statut != null) filtre = filtre.and((a, q, cb) -> cb.equal(a.get("statut"), statut));
        if (nom != null && !nom.isBlank()) {
            String motif = "%" + normaliser(nom) + "%";
            filtre = filtre.and((a, q, cb) -> cb.like(a.get("nomNormalise"), motif));
        }
        return repository.findAll(filtre, pageable).map(ActeReponse::de);
    }

    @Transactional(readOnly = true)
    public RechercheReponse rechercher(RechercheRequete r) {
        List<ActeReponse> resultats = repository.candidats(r.typeActe(), normaliser(r.nom()), normaliser(r.prenom()))
                .stream()
                .filter(a -> correspond(a, r))
                .map(ActeReponse::de)
                .toList();
        return RechercheReponse.de(resultats);
    }

    @Transactional(readOnly = true)
    public Statistiques statistiques() {
        Map<TypeActe, Long> parType = new EnumMap<>(TypeActe.class);
        for (TypeActe t : TypeActe.values()) parType.put(t, repository.countByTypeActe(t));
        return new Statistiques(repository.count(), repository.countByStatut(StatutActe.SAISI),
                repository.countByStatut(StatutActe.VALIDE), parType);
    }

    private Acte trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new ActeIntrouvableException(id));
    }

    // ------------------------------------------------------------------ règles

    private void verifierCoherence(ActeRequete r) {
        if (r.dateNaissance() != null && r.dateNaissance().isAfter(r.dateEvenement())) {
            throw new RegleMetierException("La date de naissance ne peut pas être postérieure à l'événement");
        }
        switch (r.typeActe()) {
            case NAISSANCE -> {
                if (r.sexe() == null) throw new RegleMetierException("Le sexe de l'enfant est obligatoire");
                if (r.dateNaissance() != null && !r.dateNaissance().equals(r.dateEvenement())) {
                    throw new RegleMetierException("Pour une naissance, la date de naissance est la date de l'événement");
                }
            }
            case MARIAGE, DIVORCE -> {
                if (estVide(r.nomConjoint()) || estVide(r.prenomConjoint())) {
                    throw new RegleMetierException("Le nom et le prénom du conjoint sont obligatoires");
                }
            }
            case DECES -> { /* aucune règle supplémentaire */ }
        }
    }

    private void appliquer(Acte a, ActeRequete r) {
        a.setCentre(r.centre());
        a.setCommune(r.commune());
        a.setVolume(r.volume());
        a.setFolio(r.folio());
        a.setDateEvenement(r.dateEvenement());
        a.setLieuEvenement(r.lieuEvenement());
        a.setNom(r.nom().trim());
        a.setPrenom(r.prenom().trim());
        a.setSexe(r.sexe());
        boolean naissance = r.typeActe() == TypeActe.NAISSANCE;
        a.setDateNaissance(naissance ? r.dateEvenement() : r.dateNaissance());
        a.setLieuNaissance(naissance && estVide(r.lieuNaissance()) ? r.lieuEvenement() : r.lieuNaissance());
        a.setNomPere(r.nomPere());
        a.setPrenomPere(r.prenomPere());
        a.setNomMere(r.nomMere());
        a.setPrenomMere(r.prenomMere());
        a.setNomConjoint(r.nomConjoint());
        a.setPrenomConjoint(r.prenomConjoint());
        a.setDateNaissanceConjoint(r.dateNaissanceConjoint());
        a.setLieuNaissanceConjoint(r.lieuNaissanceConjoint());
        a.setHeureDeces(r.heureDeces());
        a.setCauseDeces(r.causeDeces());
        a.setTemoin1(r.temoin1());
        a.setTemoin2(r.temoin2());
        a.setMentionsMarginales(r.mentionsMarginales());
        a.setObservations(r.observations());
    }

    /** Un critère facultatif écarte l'acte seulement s'il est fourni ET renseigné dans l'acte ET différent. */
    private boolean correspond(Acte a, RechercheRequete r) {
        boolean titulaire = Normalisation.equivalents(a.getNom(), r.nom())
                && Normalisation.equivalents(a.getPrenom(), r.prenom());
        LocalDate naissance = titulaire ? a.getDateNaissance() : a.getDateNaissanceConjoint();
        return concorde(naissance, r.dateNaissance())
                && concorde(a.getDateEvenement(), r.dateEvenement())
                && (!titulaire || lieuConcorde(a.getLieuNaissance(), r.lieuNaissance()))
                && (!titulaire || texteConcorde(a.getNomPere(), r.nomPere()))
                && (!titulaire || texteConcorde(a.getPrenomPere(), r.prenomPere()))
                && (!titulaire || texteConcorde(a.getNomMere(), r.nomMere()))
                && (!titulaire || texteConcorde(a.getPrenomMere(), r.prenomMere()))
                && texteConcorde(titulaire ? a.getNomConjoint() : a.getNom(), r.nomConjoint())
                && texteConcorde(titulaire ? a.getPrenomConjoint() : a.getPrenom(), r.prenomConjoint());
    }

    private static boolean concorde(Object valeurActe, Object critere) {
        return critere == null || valeurActe == null || Objects.equals(valeurActe, critere);
    }

    private static boolean texteConcorde(String valeurActe, String critere) {
        return estVide(critere) || estVide(valeurActe) || Normalisation.equivalents(valeurActe, critere);
    }

    /** « Dakar » concorde avec « Hôpital Principal, Dakar » : un lieu peut être plus ou moins précis. */
    private static boolean lieuConcorde(String valeurActe, String critere) {
        if (estVide(critere) || estVide(valeurActe)) return true;
        String acte = " " + normaliser(valeurActe) + " ";
        String cherche = " " + normaliser(critere) + " ";
        return acte.contains(cherche) || cherche.contains(acte);
    }

    private static boolean estVide(String s) {
        return s == null || s.isBlank();
    }
}
