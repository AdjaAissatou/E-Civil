package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.socle.securite.Permissions;
import sn.etatcivil.demande.application.port.entree.*;
import sn.etatcivil.demande.domaine.StatutDemande;
import sn.etatcivil.demande.domaine.FormatDocument;

import java.util.*;

@RestController
@RequestMapping("/api/demandes")
class DemandeController {

    private static final String AGENTS = Permissions.A_DEMANDES_CONSULTER;
    /** Rejeter : avant paiement, quand l'acte manque, ou à toute étape ; le domaine précise selon le statut. */
    private static final String REJET = "hasAnyRole('DEMANDES_VALIDER','DEMANDES_RECHERCHER','DEMANDES_SUPERVISER')";
    private static final String REGISTRE = Permissions.A_DEMANDES_RECHERCHER;

    private final GererMesDemandes citoyen;
    private final ConsulterDemandes consultation;
    private final InstruireDemandes instruction;
    private final RechercherActes recherche;
    private final SignerDemandes signature;

    private final GererPreferences preferences;

    DemandeController(GererMesDemandes citoyen, ConsulterDemandes consultation, InstruireDemandes instruction,
                      RechercherActes recherche, SignerDemandes signature, GererPreferences preferences) {
        this.preferences = preferences;
        this.citoyen = citoyen;
        this.consultation = consultation;
        this.instruction = instruction;
        this.recherche = recherche;
        this.signature = signature;
    }

    public record Motif(@NotBlank String motif) {}
    public record Raison(@NotBlank String raison) {}
    public record Numero(@NotBlank String numeroActe) {}
    public record PageReponse<T>(List<T> contenu, int numero, int taille, long total, int nombrePages) {}

    // ---------------------------------------------------------------- citoyen

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_DEMANDES_DEPOSER)
    DemandeReponse deposer(@Valid @RequestBody DepotRequete requete, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(citoyen.deposer(requete.versCommande(), Identite.demandeur(jwt)), true);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize(Permissions.A_DEMANDES_DEPOSER)
    List<DemandeReponse> mesDemandes(@AuthenticationPrincipal Jwt jwt) {
        return citoyen.mesDemandes(jwt.getSubject()).stream().map(d -> DemandeReponse.de(d, false)).toList();
    }

    @PostMapping("/{id}/annulation")
    @PreAuthorize(Permissions.A_DEMANDES_DEPOSER)
    void annuler(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        citoyen.annuler(id, jwt.getSubject());
    }

    public record Preferences(boolean notifications) {}

    /** Préférences de notification du demandeur connecté (enregistrées depuis « Mon compte »). */
    @PutMapping("/preferences")
    Preferences preferences(@RequestBody Preferences p, @AuthenticationPrincipal Jwt jwt) {
        preferences.definirNotifications(jwt.getSubject(), p.notifications());
        return p;
    }

    @GetMapping("/{id}")
    DemandeReponse consulter(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(consultation.consulter(id, jwt.getSubject(), Identite.consulteToutes(jwt)), true);
    }

    // ---------------------------------------------------------------- agents

    @GetMapping
    @PreAuthorize(AGENTS)
    PageReponse<DemandeReponse> rechercher(@RequestParam(required = false) Set<StatutDemande> statut,
                                           @RequestParam(required = false) String reference,
                                           @RequestParam(required = false) Long typeDocument,
                                           @RequestParam(required = false) FormatDocument format,
                                           @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate du,
                                           @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate au,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "25") int taille) {
        var filtre = new FiltreDemandes(statut == null ? Set.of() : statut, reference, typeDocument, format, du, au);
        var p = consultation.rechercher(filtre, new PageDemandee(page, taille))
                .map(d -> DemandeReponse.de(d, false));
        return new PageReponse<>(p.contenu(), p.numero(), p.taille(), p.total(), p.nombrePages());
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('DEMANDES_CONSULTER','STATISTIQUES_CONSULTER')")
    Map<StatutDemande, Long> statistiques(@RequestParam(required = false) Long typeDocument,
                                          @RequestParam(required = false) FormatDocument format,
                                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate du,
                                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate au) {
        return consultation.nombreParStatut(new FiltreDemandes(Set.of(), null, typeDocument, format, du, au));
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize(Permissions.A_DEMANDES_VALIDER)
    void valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        instruction.valider(id, Identite.acteur(jwt));
    }

    @PostMapping("/{id}/rejet")
    @PreAuthorize(REJET)
    void rejeter(@PathVariable UUID id, @Valid @RequestBody Motif corps, @AuthenticationPrincipal Jwt jwt) {
        instruction.rejeter(id, corps.motif(), Identite.acteur(jwt));
    }

    @PostMapping("/{id}/livraison")
    @PreAuthorize(Permissions.A_DOCUMENTS_LIVRER)
    void livrer(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        instruction.marquerLivree(id, Identite.acteur(jwt));
    }

    @PostMapping("/{id}/recherche")
    @PreAuthorize(REGISTRE)
    DemandeReponse relancerRecherche(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(recherche.rechercher(id, Identite.acteur(jwt)), true);
    }

    @PostMapping("/{id}/rattachement")
    @PreAuthorize(REGISTRE)
    DemandeReponse rattacher(@PathVariable UUID id, @Valid @RequestBody Numero corps, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(recherche.rattacher(id, corps.numeroActe(), Identite.acteur(jwt)), true);
    }

    @PostMapping("/{id}/contestation")
    @PreAuthorize(Permissions.A_DOCUMENTS_SIGNER)
    void contester(@PathVariable UUID id, @Valid @RequestBody Raison corps, @AuthenticationPrincipal Jwt jwt) {
        recherche.contester(id, corps.raison(), Identite.acteur(jwt));
    }

    @PostMapping("/{id}/signature")
    @PreAuthorize(Permissions.A_DOCUMENTS_SIGNER)
    DemandeReponse signer(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(signature.signer(id, Identite.acteur(jwt)), true);
    }
}
