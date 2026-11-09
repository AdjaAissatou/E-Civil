package sn.etatcivil.document.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.document.application.port.entree.DelivrerDocuments;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
class DocumentController {

    private static final Set<String> AGENTS = Set.of("DELEGUE", "AGENT_REGISTRE", "OFFICIER", "ADMIN");

    private final DelivrerDocuments documents;

    DocumentController(DelivrerDocuments documents) {
        this.documents = documents;
    }

    public record Commande(@NotNull UUID demandeId, @NotBlank String reference, @NotBlank String demandeurId,
                           @NotBlank String libelleDocument, boolean copieIntegrale, @NotNull UUID acteId,
                           @NotBlank String format, @NotBlank String signataire) {}

    public record Delivrance(String codeVerification, String empreinte, Instant dateSignature) {}

    /** Ce qu'affiche la page publique de vérification. */
    public record Verification(String libelleDocument, ContenuActe acte, String signataire, Instant dateSignature,
                               String codeVerification, String empreinte) {
        static Verification de(DocumentDelivre d) {
            return new Verification(d.libelleDocument(), d.contenu(), d.signataire(), d.dateSignature(),
                    d.codeVerification(), d.empreinte());
        }
    }

    /** Appelé par demande-service (jeton de service) lorsque l'officier signe. */
    @PostMapping("/api/documents")
    @PreAuthorize("hasRole('SERVICE')")
    Delivrance delivrer(@Valid @RequestBody Commande c) {
        DocumentDelivre d = documents.delivrer(new DelivrerDocuments.DocumentADelivrer(c.demandeId(), c.reference(),
                c.demandeurId(), c.libelleDocument(), c.copieIntegrale(), c.acteId(), c.format(), c.signataire()));
        return new Delivrance(d.codeVerification(), d.empreinte(), d.dateSignature());
    }

    @GetMapping("/api/documents/demandes/{demandeId}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable UUID demandeId, @AuthenticationPrincipal Jwt jwt) {
        boolean agent = UtilisateurJwt.depuis(jwt).roles().stream().anyMatch(AGENTS::contains);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("acte.pdf").build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(documents.pdf(demandeId, jwt.getSubject(), agent));
    }

    /** Public : une administration contrôle l'authenticité d'un document à partir de son code. */
    @GetMapping("/api/verification/{code}")
    ResponseEntity<Verification> verifier(@PathVariable String code) {
        return documents.verifier(code).map(Verification::de).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
