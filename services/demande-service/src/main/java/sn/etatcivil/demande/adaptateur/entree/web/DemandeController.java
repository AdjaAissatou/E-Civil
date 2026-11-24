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
import sn.etatcivil.demande.domaine.PieceIdentite;
import sn.etatcivil.demande.domaine.PiecesDeposees;

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
    private final ConsulterPiecesIdentite piecesIdentite;

    DemandeController(GererMesDemandes citoyen, ConsulterDemandes consultation, InstruireDemandes instruction,
                      RechercherActes recherche, SignerDemandes signature, GererPreferences preferences,
                      ConsulterPiecesIdentite piecesIdentite) {
        this.piecesIdentite = piecesIdentite;
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

    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_DEMANDES_DEPOSER)
    DemandeReponse deposer(@Valid @RequestBody DepotRequete requete, @AuthenticationPrincipal Jwt jwt) {
        return DemandeReponse.de(citoyen.deposer(requete.versCommande(), Identite.demandeur(jwt)), true);
    }

    /** Dépôt avec la pièce d'identité (recto, verso) et le selfie, pour les documents qui exigent la vérification. */
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_DEMANDES_DEPOSER)
    DemandeReponse deposerAvecPieces(@Valid @RequestPart("demande") DepotRequete requete,
                                     @RequestParam(required = false) PieceIdentite.TypePiece typePiece,
                                     @RequestPart(required = false) org.springframework.web.multipart.MultipartFile recto,
                                     @RequestPart(required = false) org.springframework.web.multipart.MultipartFile verso,
                                     @RequestPart(required = false) org.springframework.web.multipart.MultipartFile selfie,
                                     @AuthenticationPrincipal Jwt jwt) throws java.io.IOException {
        java.util.Map<PieceIdentite.Nature, PiecesDeposees.Photo> photos = new java.util.EnumMap<>(PieceIdentite.Nature.class);
        photos.put(PieceIdentite.Nature.RECTO, photo(recto));
        photos.put(PieceIdentite.Nature.VERSO, photo(verso));
        photos.put(PieceIdentite.Nature.SELFIE, photo(selfie));
        return DemandeReponse.de(citoyen.deposer(requete.versCommande(), Identite.demandeur(jwt),
                new PiecesDeposees(typePiece, photos)), true);
    }

    private static PiecesDeposees.Photo photo(org.springframework.web.multipart.MultipartFile f) throws java.io.IOException {
        return f == null || f.isEmpty() ? null : new PiecesDeposees.Photo(f.getBytes(), f.getContentType());
    }

    public record PieceReponse(UUID id, PieceIdentite.Nature nature, String libelle, PieceIdentite.TypePiece typePiece,
                               String libelleTypePiece, int taille, java.time.Instant dateDepot, java.time.Instant datePurge) {}

    /** Liste des photos d'identité (sans leur contenu) : le demandeur et les agents qui consultent les demandes. */
    @GetMapping("/{id}/pieces")
    List<PieceReponse> pieces(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        consultation.consulter(id, jwt.getSubject(), Identite.consulteToutes(jwt));
        return piecesIdentite.pieces(id).stream().map(p -> new PieceReponse(p.id(), p.nature(), p.nature().libelle(),
                p.typePiece(), p.typePiece().libelle(), p.taille(), p.dateDepot(), p.datePurge())).toList();
    }

    /** Contenu d'une photo : délégué, officier ou supervision ; jamais mis en cache. */
    @GetMapping("/{id}/pieces/{pieceId}")
    @PreAuthorize("hasAnyRole('DEMANDES_VALIDER','DOCUMENTS_SIGNER','DEMANDES_SUPERVISER')")
    org.springframework.http.ResponseEntity<byte[]> piece(@PathVariable UUID id, @PathVariable UUID pieceId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(piecesIdentite.contenu(id, pieceId, Identite.acteur(jwt)));
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
    void valider(@PathVariable UUID id, @RequestBody(required = false) Validation corps, @AuthenticationPrincipal Jwt jwt) {
        instruction.valider(id, Identite.acteur(jwt), corps != null && corps.identiteVerifiee());
    }

    /** @param identiteVerifiee le délégué a comparé le selfie à la pièce d'identité */
    public record Validation(boolean identiteVerifiee) {}

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
