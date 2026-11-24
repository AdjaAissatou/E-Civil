package sn.etatcivil.document.adaptateur.entree.web;

import sn.etatcivil.document.domaine.Declaration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.document.application.port.entree.DelivrerDocuments;
import sn.etatcivil.document.domaine.Complements;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;
import sn.etatcivil.socle.securite.Permissions;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
class DocumentController {


    private final DelivrerDocuments documents;

    DocumentController(DelivrerDocuments documents) {
        this.documents = documents;
    }

    public record Commande(@NotNull UUID demandeId, @NotBlank String reference, @NotBlank String demandeurId,
                           @NotBlank String libelleDocument, String modele, UUID acteId,
                           @NotBlank String format, @NotBlank String signataire, String motif, String constat,
                           PersonneJson personne, ComplementsJson complements, Integer dureeValiditeJours,
                           String typeMention) {

        Declaration declaration() {
            if (personne == null) return null;
            ComplementsJson c = complements == null ? new ComplementsJson(null, null, null, null, null) : complements;
            return new Declaration(personne.nom(), personne.prenom(), personne.sexe(), personne.dateNaissance(),
                    personne.lieuNaissance(), personne.nomPere(), personne.prenomPere(), personne.nomMere(),
                    personne.prenomMere(), c.profession(), c.residence(), c.temoin1(), c.temoin2(), c.precisions());
        }
    }

    /** Personne concernée et informations complémentaires de la demande, telles que demande-service les envoie. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonneJson(String nom, String prenom, String sexe, java.time.LocalDate dateNaissance, String lieuNaissance,
                               String nomPere, String prenomPere, String nomMere, String prenomMere) {}

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record ComplementsJson(String profession, String residence, String temoin1, String temoin2, String precisions) {}

    public record Delivrance(String codeVerification, String empreinte, Instant dateSignature) {}

    /** Ce qu'affiche la page publique de vérification. */
    public record Verification(String libelleDocument, ContenuActe acte, Complements complements, String signataire,
                               Instant dateSignature, String codeVerification, String empreinte) {
        static Verification de(DocumentDelivre d) {
            return new Verification(d.libelleDocument(), d.contenu(), d.complements(), d.signataire(), d.dateSignature(),
                    d.codeVerification(), d.empreinte());
        }
    }

    /** Appelé par demande-service (jeton de service) lorsque l'officier signe. */
    @PostMapping("/api/documents")
    @PreAuthorize(Permissions.A_SERVICE)
    Delivrance delivrer(@Valid @RequestBody Commande c) {
        DocumentDelivre d = documents.delivrer(new DelivrerDocuments.DocumentADelivrer(c.demandeId(), c.reference(),
                c.demandeurId(), c.libelleDocument(), c.modele(), c.acteId(), c.format(), c.signataire(), c.motif(),
                c.constat(), c.declaration(), c.dureeValiditeJours(), c.typeMention()));
        return new Delivrance(d.codeVerification(), d.empreinte(), d.dateSignature());
    }

    @GetMapping("/api/documents/demandes/{demandeId}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable UUID demandeId, @AuthenticationPrincipal Jwt jwt) {
        // un agent qui consulte toutes les demandes peut ouvrir le document (impression d'un document papier)
        boolean agent = UtilisateurJwt.depuis(jwt).a(Permissions.DEMANDES_CONSULTER);
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
