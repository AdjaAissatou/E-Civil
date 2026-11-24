package sn.etatcivil.demande.application.port.entree;

import sn.etatcivil.demande.domaine.FormatDocument;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.time.LocalDate;
import java.util.Set;

/**
 * Filtres des files des agents (comme les écrans du délégué et de l'officier de la version 1) ; chaque critère
 * absent est ignoré. {@code statuts} vide = tous les statuts ; {@code du} et {@code au} bornent la date de dépôt.
 */
public record FiltreDemandes(Set<StatutDemande> statuts, String reference, Long typeDocumentId, FormatDocument format,
                             LocalDate du, LocalDate au) {
    public static FiltreDemandes statuts(Set<StatutDemande> statuts, String reference) {
        return new FiltreDemandes(statuts, reference, null, null, null, null);
    }
}
