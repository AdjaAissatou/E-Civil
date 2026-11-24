package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.demande.application.port.entree.GererCatalogue;
import sn.etatcivil.demande.domaine.*;
import sn.etatcivil.socle.securite.Permissions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Catalogue public des documents (lecture sans authentification) : natures, variétés, motifs et pièces à fournir.
 * Création et modification réservées à la gestion du catalogue.
 */
@RestController
@RequestMapping("/api/catalogue")
class CatalogueController {

    private final GererCatalogue catalogue;

    CatalogueController(GererCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    public record DocumentReponse(Long id, String code, String libelle, String description, TypeActe typeActe,
                                  boolean concerneUnCouple, boolean copieIntegrale, ModeleDocument modele,
                                  OrigineActe origine, int tarif, int delaiJours, boolean actif,
                                  Long natureId, String libelleNature, String libelleModele, String typeMention,
                                  Set<Champ> champs, Integer dureeValiditeJours, String motsCles, String piecesAFournir,
                                  boolean estOriginal, Long copieDeId, boolean exigeActe) {
        static DocumentReponse de(TypeDocument t) {
            return new DocumentReponse(t.id(), t.code(), t.libelle(), t.description(), t.typeActe(),
                    t.typeActe().concerneUnCouple(), t.copieIntegrale(), t.modele(), t.origine(), t.tarif(),
                    t.delaiJours(), t.actif(), t.natureId(), t.libelleNature(), t.modele().libelle(), t.typeMention(),
                    t.champs(), t.dureeValiditeJours(), t.motsCles(), t.piecesAFournir(), t.estOriginal(), t.copieDeId(),
                    t.modele().exigeActe());
        }
    }

    public record NatureReponse(Long id, String code, String libelle, String description, int ordre, boolean active) {
        static NatureReponse de(Nature n) {
            return new NatureReponse(n.id(), n.code(), n.libelle(), n.description(), n.ordre(), n.active());
        }
    }

    public record MotifReponse(Long id, String libelle, boolean autre, int ordre, String piecesRequises, Long typeDocumentId) {
        static MotifReponse de(Motif m) {
            return new MotifReponse(m.id(), m.libelle(), m.estAutre(), m.ordre(), m.piecesRequises(), m.typeDocumentId());
        }
    }

    public record Modification(@Min(200) int tarif, @Min(1) @Max(60) int delaiJours, boolean actif) {}

    public record DocumentRequete(@Size(max = 60) String code, @NotBlank @Size(max = 150) String libelle,
                                  @Size(max = 500) String description, @NotNull Long natureId, @NotNull TypeActe typeActe,
                                  @NotNull ModeleDocument modele, OrigineActe origine, @Size(max = 30) String typeMention,
                                  Set<Champ> champs, @Min(200) int tarif, @Min(1) @Max(60) int delaiJours,
                                  @Min(1) @Max(3650) Integer dureeValiditeJours, @Size(max = 255) String motsCles,
                                  @Size(max = 1000) String piecesAFournir, boolean estOriginal, Long copieDeId, boolean actif) {
        TypeDocument.Caracteristiques caracteristiques() {
            return new TypeDocument.Caracteristiques(libelle, description, natureId, typeActe, modele, origine, typeMention,
                    champs, tarif, delaiJours, dureeValiditeJours, motsCles, piecesAFournir, estOriginal, copieDeId, actif);
        }
    }

    public record NatureRequete(@Size(max = 40) String code, @NotBlank @Size(max = 100) String libelle,
                                @Size(max = 500) String description, @Min(0) @Max(999) int ordre, boolean active) {}

    public record MotifRequete(@NotBlank @Size(max = 150) String libelle, @Min(0) @Max(999) int ordre,
                               @Size(max = 500) String piecesRequises, Long typeDocumentId) {}

    public record Libelle(String code, String libelle) {}

    public record Referentiels(List<Libelle> modeles, List<Libelle> champs, List<Libelle> typesActe, List<Libelle> origines) {}

    // ------------------------------------------------------------------ lecture publique

    @GetMapping("/documents")
    List<DocumentReponse> disponibles() {
        return catalogue.documents(true).stream().map(DocumentReponse::de).toList();
    }

    @GetMapping("/documents/tous")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    List<DocumentReponse> tous() {
        return catalogue.documents(false).stream().map(DocumentReponse::de).toList();
    }

    @GetMapping("/documents/{id}")
    DocumentReponse document(@PathVariable Long id) {
        return DocumentReponse.de(catalogue.document(id));
    }

    @GetMapping("/natures")
    List<NatureReponse> natures(@RequestParam(defaultValue = "true") boolean actives) {
        return catalogue.natures(actives).stream().map(NatureReponse::de).toList();
    }

    /** Motifs proposés pour une variété (communs et propres), ou motifs communs. */
    @GetMapping("/motifs")
    List<MotifReponse> motifs(@RequestParam(required = false) Long document) {
        return catalogue.motifs(document).stream().map(MotifReponse::de).toList();
    }

    @GetMapping("/referentiels")
    Referentiels referentiels() {
        return new Referentiels(
                Arrays.stream(ModeleDocument.values()).map(m -> new Libelle(m.name(), m.libelle())).toList(),
                Arrays.stream(Champ.values()).map(c -> new Libelle(c.name(), c.libelle())).toList(),
                Arrays.stream(TypeActe.values()).map(t -> new Libelle(t.name(), t.libelle())).toList(),
                Arrays.stream(OrigineActe.values()).map(o -> new Libelle(o.name(), o.name())).toList());
    }

    // ------------------------------------------------------------------ administration

    @PutMapping("/documents/{id}")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    DocumentReponse modifier(@PathVariable Long id, @Valid @RequestBody Modification m) {
        return DocumentReponse.de(catalogue.modifier(id, m.tarif(), m.delaiJours(), m.actif()));
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    DocumentReponse creer(@Valid @RequestBody DocumentRequete r) {
        return DocumentReponse.de(catalogue.creerDocument(r.code(), r.caracteristiques()));
    }

    @PutMapping("/documents/{id}/caracteristiques")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    DocumentReponse modifierTout(@PathVariable Long id, @Valid @RequestBody DocumentRequete r) {
        return DocumentReponse.de(catalogue.modifierDocument(id, r.caracteristiques()));
    }

    @PostMapping("/natures")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    NatureReponse creerNature(@Valid @RequestBody NatureRequete r) {
        return NatureReponse.de(catalogue.creerNature(r.code(), r.libelle(), r.description(), r.ordre()));
    }

    @PutMapping("/natures/{id}")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    NatureReponse modifierNature(@PathVariable Long id, @Valid @RequestBody NatureRequete r) {
        return NatureReponse.de(catalogue.modifierNature(id, r.libelle(), r.description(), r.ordre(), r.active()));
    }

    @GetMapping("/motifs/tous")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    List<MotifReponse> tousLesMotifs() {
        return catalogue.tousLesMotifs().stream().map(MotifReponse::de).toList();
    }

    @PostMapping("/motifs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    MotifReponse creerMotif(@Valid @RequestBody MotifRequete r) {
        return MotifReponse.de(catalogue.creerMotif(r.libelle(), r.ordre(), r.piecesRequises(), r.typeDocumentId()));
    }

    @PutMapping("/motifs/{id}")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    MotifReponse modifierMotif(@PathVariable Long id, @Valid @RequestBody MotifRequete r) {
        return MotifReponse.de(catalogue.modifierMotif(id, r.libelle(), r.ordre(), r.piecesRequises(), r.typeDocumentId()));
    }
}
