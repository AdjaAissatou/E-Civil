package sn.etatcivil.paiement.adaptateur.entree.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.paiement.application.port.entree.PayerDemande;
import sn.etatcivil.paiement.domaine.Paiement;
import sn.etatcivil.paiement.domaine.RegleMetierException;
import sn.etatcivil.paiement.domaine.StatutPaiement;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
class PaiementController {

    private static final Logger log = LoggerFactory.getLogger(PaiementController.class);

    private static final java.util.Set<String> ROLES_AGENTS = java.util.Set.of("DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN");

    private final PayerDemande paiements;

    PaiementController(PayerDemande paiements) {
        this.paiements = paiements;
    }

    public record Redirection(String urlPaiement) {}
    public record Confirmation(UUID demandeId, StatutPaiement statut) {}
    public record PaiementReponse(UUID id, int montant, StatutPaiement statut, String urlRecu, Instant dateCreation,
                                  Instant dateConfirmation) {
        static PaiementReponse de(Paiement p) {
            return new PaiementReponse(p.id(), p.montant(), p.statut(), p.urlRecu(), p.dateCreation(), p.dateConfirmation());
        }
    }

    @GetMapping("/disponibilite")
    Map<String, Boolean> disponibilite() {
        return Map.of("disponible", paiements.disponible());
    }

    @PostMapping("/demandes/{demandeId}")
    @PreAuthorize("hasRole('CITOYEN')")
    Redirection initier(@PathVariable UUID demandeId, @AuthenticationPrincipal Jwt jwt) {
        return new Redirection(paiements.initier(demandeId, jwt.getSubject()));
    }

    @GetMapping("/demandes/{demandeId}")
    List<PaiementReponse> deLaDemande(@PathVariable UUID demandeId, @AuthenticationPrincipal Jwt jwt) {
        boolean agent = UtilisateurJwt.depuis(jwt).roles().stream().anyMatch(ROLES_AGENTS::contains);
        return paiements.paiements(demandeId, jwt.getSubject(), agent).stream().map(PaiementReponse::de).toList();
    }

    /** Retour du navigateur : le portail transmet le token reçu de PayDunya. */
    @PostMapping("/confirmation")
    Confirmation confirmer(@RequestParam String token) {
        Paiement p = paiements.confirmer(token);
        return new Confirmation(p.demandeId(), p.statut());
    }

    /** Notification instantanée (IPN) envoyée par les serveurs PayDunya, au format formulaire data[...]. */
    @PostMapping("/paydunya/ipn")
    ResponseEntity<String> notification(@RequestParam Map<String, String> donnees) {
        try {
            paiements.recevoirNotification(donnees.get("data[hash]"), donnees.get("data[invoice][token]"));
            return ResponseEntity.ok("OK");
        } catch (RegleMetierException e) {
            log.warn("Notification PayDunya refusée : {}", e.getMessage());
            return ResponseEntity.badRequest().body("REFUSE");
        }
    }
}
