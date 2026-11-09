package sn.etatcivil.central.numerotation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.central.acte.TypeActe;

/**
 * Attribue les numéros d'acte (ex. N-2026-000042). Le compteur est verrouillé en écriture
 * jusqu'à la fin de la transaction : deux saisies simultanées ne peuvent pas obtenir le même numéro,
 * et un numéro n'est consommé que si l'acte est réellement enregistré.
 */
@Service
public class NumerotationService {

    private final NumerotationRepository repository;
    public NumerotationService(NumerotationRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String attribuer(TypeActe type, int annee) {
        Numerotation compteur = repository.verrouiller(type.name(), annee).orElseGet(() -> {
            repository.creerSiAbsent(type.name(), annee);
            return repository.verrouiller(type.name(), annee).orElseThrow();
        });
        return String.format("%s-%d-%06d", type.prefixe(), annee, compteur.suivant());
    }
}
