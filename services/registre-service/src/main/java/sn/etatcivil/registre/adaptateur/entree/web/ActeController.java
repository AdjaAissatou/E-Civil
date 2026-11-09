package sn.etatcivil.registre.adaptateur.entree.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.registre.application.port.entree.*;
import sn.etatcivil.registre.domaine.ResultatRecherche;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;
import sn.etatcivil.socle.securite.UtilisateurJwt;

import java.util.List;
import java.util.UUID;

/** Adaptateur REST : traduit HTTP/JSON en appels aux cas d'usage, rien de plus. */
@RestController
@RequestMapping("/api/registre/actes")
class ActeController {

    private static final String AGENTS = "hasAnyRole('AGENT_REGISTRE','OFFICIER','ADMIN')";
    private static final String AGENTS_OU_SERVICES = "hasAnyRole('AGENT_REGISTRE','OFFICIER','ADMIN','SERVICE')";

    private final TranscrireActe transcription;
    private final ConsulterActes consultation;

    ActeController(TranscrireActe transcription, ConsulterActes consultation) {
        this.transcription = transcription;
        this.consultation = consultation;
    }

    public record ResultatRechercheReponse(ResultatRecherche.Resultat resultat, List<ActeReponse> actes) {}

    public record PageReponse<T>(List<T> contenu, int numero, int taille, long total, int nombrePages) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AGENTS)
    ActeReponse transcrire(@Valid @RequestBody ActeRequete requete, @AuthenticationPrincipal Jwt jwt) {
        UtilisateurJwt agent = UtilisateurJwt.depuis(jwt);
        return ActeReponse.de(transcription.transcrire(requete.versDomaine(), agent.nomComplet() + " <" + agent.email() + ">"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(AGENTS)
    ActeReponse corriger(@PathVariable UUID id, @Valid @RequestBody ActeRequete requete) {
        return ActeReponse.de(transcription.corriger(id, requete.versDomaine()));
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasAnyRole('OFFICIER','ADMIN')")
    ActeReponse valider(@PathVariable UUID id) {
        return ActeReponse.de(transcription.valider(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize(AGENTS_OU_SERVICES)
    ActeReponse obtenir(@PathVariable UUID id) {
        return ActeReponse.de(consultation.obtenir(id));
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize(AGENTS_OU_SERVICES)
    ActeReponse parNumero(@PathVariable String numero) {
        return ActeReponse.de(consultation.parNumero(numero));
    }

    @GetMapping
    @PreAuthorize(AGENTS)
    PageReponse<ActeReponse> lister(@RequestParam(required = false) TypeActe type,
                                    @RequestParam(required = false) StatutActe statut,
                                    @RequestParam(required = false) String nom,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int taille) {
        PageResultat<ActeReponse> p = consultation.lister(new FiltreActes(type, statut, nom), new PageDemandee(page, taille))
                .map(ActeReponse::de);
        return new PageReponse<>(p.contenu(), p.numero(), p.taille(), p.total(), p.nombrePages());
    }

    @PostMapping("/recherche")
    @PreAuthorize(AGENTS_OU_SERVICES)
    ResultatRechercheReponse rechercher(@Valid @RequestBody RechercheRequete requete) {
        ResultatRecherche r = consultation.rechercher(requete.versDomaine());
        return new ResultatRechercheReponse(r.resultat(), r.actes().stream().map(ActeReponse::de).toList());
    }

    @GetMapping("/statistiques")
    @PreAuthorize(AGENTS)
    Statistiques statistiques() {
        return consultation.statistiques();
    }
}
