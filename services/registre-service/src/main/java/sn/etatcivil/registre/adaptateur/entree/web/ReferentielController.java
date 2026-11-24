package sn.etatcivil.registre.adaptateur.entree.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.etatcivil.registre.application.port.entree.ConsulterActes;
import sn.etatcivil.registre.domaine.Commune;
import sn.etatcivil.registre.domaine.TypeMention;

import java.util.Arrays;
import java.util.List;

/** Référentiels publics : communes du Sénégal et types de mentions marginales. */
@RestController
class ReferentielController {

    private final ConsulterActes consultation;

    ReferentielController(ConsulterActes consultation) {
        this.consultation = consultation;
    }

    @GetMapping("/api/registre/communes")
    List<Commune> communes() {
        return consultation.communes();
    }

    public record TypeMentionReponse(String code, String libelle, List<String> actes) {}

    @GetMapping("/api/registre/types-mentions")
    List<TypeMentionReponse> typesMentions() {
        return Arrays.stream(TypeMention.values()).map(t -> new TypeMentionReponse(t.name(), t.libelle(),
                Arrays.stream(sn.etatcivil.registre.domaine.TypeActe.values()).filter(t::sApplique).map(Enum::name).toList()))
                .toList();
    }
}
