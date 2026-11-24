package sn.etatcivil.demande.adaptateur.entree.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.demande.application.port.entree.RenseignerDemandes;
import sn.etatcivil.demande.domaine.Renseignement;
import sn.etatcivil.demande.domaine.ResultatConsultation;
import sn.etatcivil.socle.securite.Permissions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Dossiers de renseignement : registres papier consultés pour retrouver l'acte d'une demande. */
@RestController
@RequestMapping("/api/demandes")
class RenseignementController {

    private static final String LIRE = "hasAnyRole('DEMANDES_RECHERCHER','DEMANDES_SUPERVISER')";
    private static final String AGIR = Permissions.A_DEMANDES_RECHERCHER;

    private final RenseignerDemandes renseignements;

    RenseignementController(RenseignerDemandes renseignements) {
        this.renseignements = renseignements;
    }

    public record ConsultationReponse(Long registreId, String registre, String resultat, String libelleResultat, String folio,
                                      String note, String agent, Instant date) {}

    public record DossierReponse(UUID demandeId, String etat, String libelleEtat, boolean urgent, String agentId,
                                 String agentNom, Instant dateOuverture, Instant datePriseEnCharge, String motifNonTrouve,
                                 String agentConclusion, Instant dateConclusion, List<ConsultationReponse> consultations) {
        static DossierReponse de(RenseignerDemandes.Dossier d) {
            Renseignement r = d.renseignement();
            return new DossierReponse(r.demandeId(), d.etat().name(), d.etat().libelle(), r.urgent(), r.agentId(), r.agentNom(),
                    r.dateOuverture(), r.datePriseEnCharge(), r.motifNonTrouve(), r.agentConclusion(), r.dateConclusion(),
                    r.consultations().stream().map(c -> new ConsultationReponse(c.registreId(), c.registre(), c.resultat().name(),
                            c.resultat().libelle(), c.folio(), c.note(), c.agent(), c.date())).toList());
        }
    }

    public record ConsultationRequete(Long registreId, String registre, ResultatConsultation resultat, String folio, String note) {}
    public record PrioriteRequete(boolean urgent) {}
    public record ConclusionRequete(String motif) {}

    @GetMapping("/renseignements")
    @PreAuthorize(LIRE)
    List<RenseignerDemandes.Resume> enCours() {
        return renseignements.dossiersEnCours();
    }

    @GetMapping("/renseignements/statistiques")
    @PreAuthorize(LIRE)
    RenseignerDemandes.Statistiques statistiques() {
        return renseignements.statistiques();
    }

    /** 204 tant qu'aucun dossier n'a été ouvert pour cette demande. */
    @GetMapping("/{id}/renseignement")
    @PreAuthorize(LIRE)
    ResponseEntity<DossierReponse> dossier(@PathVariable UUID id) {
        return renseignements.dossier(id).map(DossierReponse::de).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/renseignement/prise-en-charge")
    @PreAuthorize(AGIR)
    DossierReponse prendreEnCharge(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DossierReponse.de(renseignements.prendreEnCharge(id, Identite.acteur(jwt)));
    }

    @PostMapping("/{id}/renseignement/priorite")
    @PreAuthorize(AGIR)
    DossierReponse priorite(@PathVariable UUID id, @RequestBody PrioriteRequete r, @AuthenticationPrincipal Jwt jwt) {
        return DossierReponse.de(renseignements.definirPriorite(id, r.urgent(), Identite.acteur(jwt)));
    }

    @PostMapping("/{id}/renseignement/consultations")
    @PreAuthorize(AGIR)
    DossierReponse consigner(@PathVariable UUID id, @RequestBody ConsultationRequete r, @AuthenticationPrincipal Jwt jwt) {
        return DossierReponse.de(renseignements.consigner(id, r.registreId(), r.registre(), r.resultat(), r.folio(), r.note(),
                Identite.acteur(jwt)));
    }

    @PostMapping("/{id}/renseignement/conclusion")
    @PreAuthorize(AGIR)
    DossierReponse conclure(@PathVariable UUID id, @RequestBody ConclusionRequete r, @AuthenticationPrincipal Jwt jwt) {
        return DossierReponse.de(renseignements.conclureNonTrouve(id, r.motif(), Identite.acteur(jwt)));
    }
}
