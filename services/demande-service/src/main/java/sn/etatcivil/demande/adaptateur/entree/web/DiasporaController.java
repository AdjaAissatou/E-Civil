package sn.etatcivil.demande.adaptateur.entree.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.demande.application.port.entree.ConsulterDiaspora;
import sn.etatcivil.demande.domaine.MissionDiplomatique;
import sn.etatcivil.demande.domaine.Pays;
import sn.etatcivil.socle.securite.Permissions;

import java.util.List;

/** Référentiel de la diaspora : lecture publique (inscription, demande), gestion par l'administration du catalogue. */
@RestController
@RequestMapping("/api/catalogue/diaspora")
class DiasporaController {

    private final ConsulterDiaspora diaspora;

    DiasporaController(ConsulterDiaspora diaspora) {
        this.diaspora = diaspora;
    }

    public record Referentiel(List<Pays> pays, List<String> typesMission, List<String> zones) {}

    public record MissionRequete(@NotBlank String type, @NotBlank @Size(max = 200) String nom, @NotNull Long paysId,
                                 @NotBlank @Size(max = 150) String ville, @Size(max = 20) String codePostal,
                                 @Size(max = 255) String competence, @NotBlank @Size(max = 255) String adresse,
                                 @Size(max = 50) String telephone, @Size(max = 150) String email,
                                 List<Long> paysCouverts, boolean active) {
        MissionDiplomatique versDomaine(Long id) {
            return new MissionDiplomatique(id, type, nom, paysId, null, ville, codePostal, competence, adresse, telephone,
                    email, paysCouverts, active);
        }
    }

    public record PaysRequete(@NotBlank @Size(max = 150) String nom, @NotBlank @Size(min = 3, max = 3) String codeIso,
                              @Size(max = 50) String continent, @Size(max = 100) String capitale, String zone) {}

    @GetMapping
    Referentiel referentiel() {
        return new Referentiel(diaspora.pays(), diaspora.typesMission(), diaspora.zones());
    }

    @GetMapping("/missions")
    List<MissionDiplomatique> missions(@RequestParam(required = false) Long pays) {
        return diaspora.missions(pays, true);
    }

    @GetMapping("/missions/toutes")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    List<MissionDiplomatique> toutes() {
        return diaspora.missions(null, false);
    }

    @PostMapping("/missions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    MissionDiplomatique creer(@Valid @RequestBody MissionRequete r) {
        return diaspora.enregistrerMission(r.versDomaine(null));
    }

    @PutMapping("/missions/{id}")
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    MissionDiplomatique modifier(@PathVariable Long id, @Valid @RequestBody MissionRequete r) {
        return diaspora.enregistrerMission(r.versDomaine(id));
    }

    @PostMapping("/pays")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_CATALOGUE_GERER)
    Pays ajouterPays(@Valid @RequestBody PaysRequete r) {
        return diaspora.ajouterPays(r.nom(), r.codeIso(), r.continent(), r.capitale(), r.zone());
    }
}
