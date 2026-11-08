package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.demande.application.port.entree.GererCatalogue;
import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.TypeActe;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;

/** Catalogue public des documents (lecture sans authentification) ; modification réservée à l'administrateur. */
@RestController
@RequestMapping("/api/catalogue")
class CatalogueController {

    private final GererCatalogue catalogue;

    CatalogueController(GererCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    public record DocumentReponse(Long id, String code, String libelle, String description, TypeActe typeActe,
                                  boolean concerneUnCouple, boolean copieIntegrale, int tarif, int delaiJours, boolean actif) {
        static DocumentReponse de(TypeDocument t) {
            return new DocumentReponse(t.id(), t.code(), t.libelle(), t.description(), t.typeActe(),
                    t.typeActe().concerneUnCouple(), t.copieIntegrale(), t.tarif(), t.delaiJours(), t.actif());
        }
    }

    public record MotifReponse(Long id, String libelle, boolean autre) {}

    public record Modification(@Min(200) int tarif, @Min(1) @Max(60) int delaiJours, boolean actif) {}

    @GetMapping("/documents")
    List<DocumentReponse> disponibles() {
        return catalogue.documents(true).stream().map(DocumentReponse::de).toList();
    }

    @GetMapping("/documents/tous")
    @PreAuthorize("hasRole('ADMIN')")
    List<DocumentReponse> tous() {
        return catalogue.documents(false).stream().map(DocumentReponse::de).toList();
    }

    @GetMapping("/documents/{id}")
    DocumentReponse document(@PathVariable Long id) {
        return DocumentReponse.de(catalogue.document(id));
    }

    @PutMapping("/documents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    DocumentReponse modifier(@PathVariable Long id, @Valid @RequestBody Modification m) {
        return DocumentReponse.de(catalogue.modifier(id, m.tarif(), m.delaiJours(), m.actif()));
    }

    @GetMapping("/motifs")
    List<MotifReponse> motifs() {
        return catalogue.motifs().stream().map(m -> new MotifReponse(m.id(), m.libelle(), m.estAutre())).toList();
    }
}
