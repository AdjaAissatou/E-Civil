package sn.etatcivil.registre.adaptateur.entree.web;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.registre.application.port.entree.GererRegistresPapier;
import sn.etatcivil.registre.domaine.*;
import sn.etatcivil.socle.securite.Permissions;

import java.util.List;

/**
 * Centres d'état civil et registres papier. Les consulter sert au chargé de renseignement qui cherche un acte ;
 * les modifier revient aux agents qui saisissent le registre.
 */
@RestController
@RequestMapping("/api/registre")
class RegistresPapierController {

    private static final String CONSULTER = "hasAnyRole('REGISTRE_CONSULTER','DEMANDES_RECHERCHER')";
    /** Le chargé de renseignement signale aussi l'état d'un registre qu'il a consulté. */
    private static final String TENIR_REGISTRES = "hasAnyRole('REGISTRE_SAISIR','DEMANDES_RECHERCHER')";

    private final GererRegistresPapier registres;

    RegistresPapierController(GererRegistresPapier registres) {
        this.registres = registres;
    }

    public record CentreReponse(Long id, String nom, String type, String libelleType, String commune, String pays,
                                Long rattachementId, String nomRattachement, boolean actif) {
        static CentreReponse de(CentreEtatCivil c) {
            return new CentreReponse(c.id(), c.nom(), c.type().name(), c.type().libelle(), c.commune(), c.pays(),
                    c.rattachementId(), c.nomRattachement(), c.actif());
        }
    }

    public record CentreRequete(String nom, @NotNull TypeCentre type, String commune, String pays, Long rattachementId,
                                Boolean actif) {}

    public record RegistreReponse(Long id, Long centreId, String nomCentre, String typeActe, int annee, String exemplaire,
                                  String libelleExemplaire, String volume, String lieuConservation, String etat,
                                  String libelleEtat, boolean consultable, boolean numerise, String observations, String libelle) {
        static RegistreReponse de(RegistrePapier r) {
            return new RegistreReponse(r.id(), r.centreId(), r.nomCentre(), r.typeActe().name(), r.annee(), r.exemplaire().name(),
                    r.exemplaire().libelle(), r.volume(), r.lieuConservation(), r.etat().name(), r.etat().libelle(),
                    r.etat().consultable(), r.numerise(), r.observations(), r.libelle());
        }
    }

    public record RegistreRequete(Long centreId, TypeActe typeActe, Integer annee, Exemplaire exemplaire, String volume,
                                  String lieuConservation, EtatRegistre etat, Boolean numerise, String observations) {}

    @GetMapping("/centres")
    @PreAuthorize(CONSULTER)
    List<CentreReponse> centres(@RequestParam(required = false) String recherche, @RequestParam(required = false) TypeCentre type) {
        return registres.centres(recherche, type).stream().map(CentreReponse::de).toList();
    }

    @GetMapping("/centres/{id}")
    @PreAuthorize(CONSULTER)
    CentreReponse centre(@PathVariable long id) {
        return CentreReponse.de(registres.centre(id));
    }

    @PostMapping("/centres")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Permissions.A_REGISTRE_SAISIR)
    CentreReponse creerCentre(@RequestBody @jakarta.validation.Valid CentreRequete r) {
        return CentreReponse.de(registres.enregistrerCentre(null, r.nom(), r.type(), r.commune(), r.pays(), r.rattachementId(),
                r.actif() == null || r.actif()));
    }

    @PutMapping("/centres/{id}")
    @PreAuthorize(Permissions.A_REGISTRE_SAISIR)
    CentreReponse modifierCentre(@PathVariable long id, @RequestBody @jakarta.validation.Valid CentreRequete r) {
        return CentreReponse.de(registres.enregistrerCentre(id, r.nom(), r.type(), r.commune(), r.pays(), r.rattachementId(),
                r.actif() == null || r.actif()));
    }

    @GetMapping("/registres-papier")
    @PreAuthorize(CONSULTER)
    List<RegistreReponse> liste(@RequestParam(required = false) Long centre, @RequestParam(required = false) TypeActe typeActe,
                                @RequestParam(required = false) Integer annee) {
        return registres.registres(centre, typeActe, annee).stream().map(RegistreReponse::de).toList();
    }

    @GetMapping("/registres-papier/{id}")
    @PreAuthorize(CONSULTER)
    RegistreReponse registre(@PathVariable long id) {
        return RegistreReponse.de(registres.registre(id));
    }

    @PostMapping("/registres-papier")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(TENIR_REGISTRES)
    RegistreReponse creer(@RequestBody RegistreRequete r) {
        return RegistreReponse.de(enregistrer(null, r));
    }

    @PutMapping("/registres-papier/{id}")
    @PreAuthorize(TENIR_REGISTRES)
    RegistreReponse modifier(@PathVariable long id, @RequestBody RegistreRequete r) {
        return RegistreReponse.de(enregistrer(id, r));
    }

    private RegistrePapier enregistrer(Long id, RegistreRequete r) {
        return registres.enregistrerRegistre(id, r.centreId(), r.typeActe(), r.annee(), r.exemplaire(), r.volume(),
                r.lieuConservation(), r.etat(), Boolean.TRUE.equals(r.numerise()), r.observations());
    }
}
