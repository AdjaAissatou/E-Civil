package sn.etatcivil.registre.adaptateur.entree.web;

import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.DonneesActe;
import sn.etatcivil.registre.domaine.OrigineActe;
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
        String mentionsMarginales, String observations, OrigineActe origine, String referenceOrigine,
        String agentSaisie, Instant dateSaisie, Instant dateModification,
        String profession, String domicile, String professionPere, String domicilePere,
        String professionMere, String domicileMere, String professionConjoint, String domicileConjoint, String medecin,
        java.util.List<MentionReponse> mentions) {

    public record MentionReponse(sn.etatcivil.registre.domaine.TypeMention type, String libelleType, LocalDate date,
                                 String reference, String details, String agent, String texte) {}

    static ActeReponse de(Acte a) {
        DonneesActe d = a.donnees();
        sn.etatcivil.registre.domaine.DetailsActe x = d.details() == null
                ? sn.etatcivil.registre.domaine.DetailsActe.AUCUN : d.details();
        return new ActeReponse(a.id(), d.typeActe(), a.numeroActe(), a.annee(), a.statut(),
                d.centre(), d.commune(), d.volume(), d.folio(), d.dateEvenement(), d.lieuEvenement(),
                d.nom(), d.prenom(), d.sexe(), d.dateNaissance(), d.lieuNaissance(),
                d.nomPere(), d.prenomPere(), d.nomMere(), d.prenomMere(),
                d.nomConjoint(), d.prenomConjoint(), d.dateNaissanceConjoint(), d.lieuNaissanceConjoint(),
                d.heureDeces(), d.causeDeces(), d.temoin1(), d.temoin2(), a.texteMentions(), d.observations(),
                d.origine(), d.referenceOrigine(),
                a.agentSaisie(), a.dateSaisie(), a.dateModification(),
                x.profession(), x.domicile(), x.professionPere(), x.domicilePere(), x.professionMere(), x.domicileMere(),
                x.professionConjoint(), x.domicileConjoint(), x.medecin(),
                a.mentions().stream().map(m -> new MentionReponse(m.type(), m.type().libelle(), m.date(), m.reference(),
                        m.details(), m.agent(), m.texte())).toList());
    }
}
