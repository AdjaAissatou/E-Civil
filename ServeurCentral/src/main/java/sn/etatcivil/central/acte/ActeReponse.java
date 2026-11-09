package sn.etatcivil.central.acte;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ActeReponse(
        Long id, TypeActe typeActe, String numeroActe, Integer annee, StatutActe statut,
        String centre, String commune, String volume, String folio,
        LocalDate dateEvenement, String lieuEvenement,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateNaissanceConjoint, String lieuNaissanceConjoint,
        String heureDeces, String causeDeces, String temoin1, String temoin2,
        String mentionsMarginales, String observations,
        String agentSaisie, LocalDateTime dateSaisie, LocalDateTime dateModification) {

    public static ActeReponse de(Acte a) {
        return new ActeReponse(a.getId(), a.getTypeActe(), a.getNumeroActe(), a.getAnnee(), a.getStatut(),
                a.getCentre(), a.getCommune(), a.getVolume(), a.getFolio(),
                a.getDateEvenement(), a.getLieuEvenement(),
                a.getNom(), a.getPrenom(), a.getSexe(), a.getDateNaissance(), a.getLieuNaissance(),
                a.getNomPere(), a.getPrenomPere(), a.getNomMere(), a.getPrenomMere(),
                a.getNomConjoint(), a.getPrenomConjoint(), a.getDateNaissanceConjoint(), a.getLieuNaissanceConjoint(),
                a.getHeureDeces(), a.getCauseDeces(), a.getTemoin1(), a.getTemoin2(),
                a.getMentionsMarginales(), a.getObservations(),
                a.getAgentSaisie(), a.getDateSaisie(), a.getDateModification());
    }
}
