package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.demande.application.port.entree.*;
import sn.etatcivil.demande.domaine.StatutDemande;

import java.util.*;

@RestController
@RequestMapping("/api/demandes")
class DemandeController {

    private static final String AGENTS = "hasAnyRole('DELEGUE','AGENT_REGISTRE','OFFICIER','ADMIN')";
    private static final String REGISTRE = "hasAnyRole('AGENT_REGISTRE','OFFICIER','ADMIN')";

    private final GererMesDemandes citoyen;
    private final ConsulterDemandes consultation;
    private final InstruireDemandes instruction;
    private final RechercherActes recherche;
    private final SignerDemandes signature;

    DemandeController(GererMesDemandes citoyen, ConsulterDemandes consultation, InstruireDemandes instruction,
                      RechercherActes recherche, SignerDemandes signature) {
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
    @PreAuthorize("hasRole('CITOYEN')")
    DemandeReponse deposer(@Valid @RequestBody DepotRequete requete, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(citoyen.deposer(requete.versCommande(), Identite.demandeur(jwt)), true);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('CITOYEN')")
    List<DemandeReponse> mesDemandes(@AuthenticationPrincipal Jwt jwt) {
        return citoyen.mesDemandes(jwt.getSubject()).stream().map(d -> DemandeReponse.de(d, false)).toList();
    }

    @PostMapping("/{id}/annulation")
    @PreAuthorize("hasRole('CITOYEN')")
    void annuler(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        citoyen.annuler(id, jwt.getSubject());
    }

    @GetMapping("/{id}")
    DemandeReponse consulter(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(consultation.consulter(id, jwt.getSubject(), Identite.role(jwt).estAgent()), true);
    }

    // ---------------------------------------------------------------- agents

    @GetMapping
    @PreAuthorize(AGENTS)
    PageReponse<DemandeReponse> rechercher(@RequestParam(required = false) Set<StatutDemande> statut,
                                           @RequestParam(required = false) String reference,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "25") int taille) {
        var p = consultation.rechercher(statut == null ? Set.of() : statut, reference, new PageDemandee(page, taille))
                .map(d -> DemandeReponse.de(d, false));
        return new PageReponse<>(p.contenu(), p.numero(), p.taille(), p.total(), p.nombrePages());
    }

    @GetMapping("/statistiques")
    @PreAuthorize(AGENTS)
    Map<StatutDemande, Long> statistiques() {
        return consultation.nombreParStatut();
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasAnyRole('DELEGUE','ADMIN')")
    void valider(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        instruction.valider(id, Identite.acteur(jwt));
    }

    @PostMapping("/{id}/rejet")
    @PreAuthorize(AGENTS)
    void rejeter(@PathVariable UUID id, @Valid @RequestBody Motif corps, @AuthenticationPrincipal Jwt jwt) {
        instruction.rejeter(id, corps.motif(), Identite.acteur(jwt));
    }

    @PostMapping("/{id}/livraison")
    @PreAuthorize("hasAnyRole('DELEGUE','ADMIN')")
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
    @PreAuthorize("hasAnyRole('OFFICIER','ADMIN')")
    void contester(@PathVariable UUID id, @Valid @RequestBody Raison corps, @AuthenticationPrincipal Jwt jwt) {
        recherche.contester(id, corps.raison(), Identite.acteur(jwt));
    }

    @PostMapping("/{id}/signature")
    @PreAuthorize("hasAnyRole('OFFICIER','ADMIN')")
    DemandeReponse signer(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(signature.signer(id, Identite.acteur(jwt)), true);
    }
}
