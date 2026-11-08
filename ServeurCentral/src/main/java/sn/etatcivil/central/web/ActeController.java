package sn.etatcivil.central.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sn.etatcivil.central.acte.*;

@RestController
@RequestMapping("/api/v1/actes")
public class ActeController {

    private final ActeService service;

    public ActeController(ActeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActeReponse creer(@Valid @RequestBody ActeRequete requete) {
        return service.creer(requete);
    }

    @PutMapping("/{id}")
    public ActeReponse modifier(@PathVariable Long id, @Valid @RequestBody ActeRequete requete) {
        return service.modifier(id, requete);
    }

    @PostMapping("/{id}/validation")
    public ActeReponse valider(@PathVariable Long id) {
        return service.valider(id);
    }

    @GetMapping("/{id}")
    public ActeReponse obtenir(@PathVariable Long id) {
        return service.obtenir(id);
    }

    @GetMapping("/numero/{numero}")
    public ActeReponse parNumero(@PathVariable String numero) {
        return service.parNumero(numero);
    }

    @GetMapping
    public Page<ActeReponse> lister(@RequestParam(required = false) TypeActe type,
                                    @RequestParam(required = false) StatutActe statut,
                                    @RequestParam(required = false) String nom,
                                    @PageableDefault(size = 20, sort = "dateSaisie", direction = Sort.Direction.DESC)
                                    Pageable pageable) {
        return service.lister(type, statut, nom, pageable);
    }

    @PostMapping("/recherche")
    public RechercheReponse rechercher(@Valid @RequestBody RechercheRequete requete) {
        return service.rechercher(requete);
    }

    @GetMapping("/statistiques")
    public Statistiques statistiques() {
        return service.statistiques();
    }
}
