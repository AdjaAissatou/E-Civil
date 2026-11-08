package sn.etatcivil.registre.adaptateur.entree.web;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.DonneesActe;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ActeReponse(
        UUID id, TypeActe typeActe, String numeroActe, Integer annee, StatutActe statut,
        String centre, String commune, String volume, String folio,
        LocalDate dateEvenement, String lieuEvenement,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String heureDeces, String causeDeces, String temoin1, String temoin2,
        String mentionsMarginales, String observations,
        String agentSaisie, Instant dateSaisie, Instant dateModification) {

    static ActeReponse de(Acte a) {
        DonneesActe d = a.donnees();
        return new ActeReponse(a.id(), d.typeActe(), a.numeroActe(), a.annee(), a.statut(),
                d.centre(), d.commune(), d.volume(), d.folio(), d.dateEvenement(), d.lieuEvenement(),
                d.nom(), d.prenom(), d.sexe(), d.dateNaissance(), d.lieuNaissance(),
                d.nomPere(), d.prenomPere(), d.nomMere(), d.prenomMere(),
                d.nomConjoint(), d.prenomConjoint(), d.dateNaissanceConjoint(), d.lieuNaissanceConjoint(),
                d.heureDeces(), d.causeDeces(), d.temoin1(), d.temoin2(), d.mentionsMarginales(), d.observations(),
                a.agentSaisie(), a.dateSaisie(), a.dateModification());
    }
}
