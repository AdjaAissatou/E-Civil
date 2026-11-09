package sn.etatcivil.registre.adaptateur.sortie.persistance;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.DonneesActe;

/** Conversion entre l'agrégat du domaine et l'entité JPA. */
final class CorrespondanceActe {
    private CorrespondanceActe() {}

    static Acte versDomaine(ActeEntite e) {
        DonneesActe d = new DonneesActe(e.typeActe, e.centre, e.commune, e.volume, e.folio, e.dateEvenement, e.lieuEvenement, e.nom, e.prenom, e.sexe, e.dateNaissance, e.lieuNaissance, e.nomPere, e.prenomPere, e.nomMere, e.prenomMere, e.nomConjoint, e.prenomConjoint, e.dateNaissanceConjoint, e.lieuNaissanceConjoint, e.heureDeces, e.causeDeces, e.temoin1, e.temoin2, e.mentionsMarginales, e.observations);
        return new Acte(e.id, e.numeroActe, e.annee, e.statut, d, e.agentSaisie, e.dateSaisie,
                e.dateModification, e.version);
    }

    static void copier(Acte a, ActeEntite e) {
        DonneesActe d = a.donnees();
        e.id = a.id();
        e.typeActe = d.typeActe();
        e.numeroActe = a.numeroActe();
        e.annee = a.annee();
        e.statut = a.statut();
        e.centre = d.centre();
        e.commune = d.commune();
        e.volume = d.volume();
        e.folio = d.folio();
        e.dateEvenement = d.dateEvenement();
        e.lieuEvenement = d.lieuEvenement();
        e.nom = d.nom();
        e.prenom = d.prenom();
        e.sexe = d.sexe();
        e.dateNaissance = d.dateNaissance();
        e.lieuNaissance = d.lieuNaissance();
        e.nomPere = d.nomPere();
        e.prenomPere = d.prenomPere();
        e.nomMere = d.nomMere();
        e.prenomMere = d.prenomMere();
        e.nomConjoint = d.nomConjoint();
        e.prenomConjoint = d.prenomConjoint();
        e.dateNaissanceConjoint = d.dateNaissanceConjoint();
        e.lieuNaissanceConjoint = d.lieuNaissanceConjoint();
        e.heureDeces = d.heureDeces();
        e.causeDeces = d.causeDeces();
        e.temoin1 = d.temoin1();
        e.temoin2 = d.temoin2();
        e.mentionsMarginales = d.mentionsMarginales();
        e.observations = d.observations();
        e.nomNormalise = a.nomNormalise();
        e.prenomNormalise = a.prenomNormalise();
        e.nomConjointNormalise = a.nomConjointNormalise();
        e.prenomConjointNormalise = a.prenomConjointNormalise();
        e.agentSaisie = a.agentSaisie();
        e.dateSaisie = a.dateSaisie();
        e.dateModification = a.dateModification();
    }
}
