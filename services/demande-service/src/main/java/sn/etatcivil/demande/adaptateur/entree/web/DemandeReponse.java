package sn.etatcivil.demande.adaptateur.entree.web;

import sn.etatcivil.demande.domaine.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Vue JSON d'une demande ; l'historique n'est fourni que sur la fiche détaillée. */
public record DemandeReponse(
        UUID id, String reference, String statut, String libelleStatut, String explicationStatut, boolean terminee,
        String demandeurId, String demandeurNom, String demandeurEmail,
        Long typeDocumentId, String libelleDocument, TypeActe typeActe, boolean copieIntegrale, int montant,
        String motif, LienDemandeur lien, FormatDocument format, String adresseLivraison, String villeLivraison,
        String nom, String prenom, String sexe, LocalDate dateNaissance, String lieuNaissance,
        String nomPere, String prenomPere, String nomMere, String prenomMere,
        String nomConjoint, String prenomConjoint, LocalDate dateEvenement,
        UUID acteId, String numeroActe, String noteRecherche, String signataire, Instant dateSignature,
        String codeVerification, String motifRejet, Instant dateCreation, Instant dateModification,
        List<Historique> historique) {

    public record Historique(String nouveauStatut, String libelle, String auteur, String commentaire, Instant date) {}

    static DemandeReponse de(Demande d, boolean avecHistorique) {
        PersonneConcernee p = d.personne();
        List<Historique> h = avecHistorique
                ? d.historique().stream().map(c -> new Historique(c.nouveau().name(), c.nouveau().libelle(), c.auteur(),
                        c.commentaire(), c.date())).toList()
                : List.of();
        return new DemandeReponse(d.id(), d.reference(), d.statut().name(), d.statut().libelle(),
                d.statut().explication(), d.statut().estTerminal(), d.demandeur().id(), d.demandeur().nomComplet(),
                d.demandeur().email(), d.typeDocumentId(), d.libelleDocument(), d.typeActe(), d.copieIntegrale(),
                d.montant(), d.libelleMotif(), d.lien(), d.format(), d.adresseLivraison(), d.villeLivraison(),
                p.nom(), p.prenom(), p.sexe(), p.dateNaissance(), p.lieuNaissance(), p.nomPere(), p.prenomPere(),
                p.nomMere(), p.prenomMere(), p.nomConjoint(), p.prenomConjoint(), p.dateEvenement(),
                d.acteId(), d.numeroActe(), d.noteRecherche(), d.signataire(), d.dateSignature(), d.codeVerification(),
                d.motifRejet(), d.dateCreation(), d.dateModification(), h);
    }
}
