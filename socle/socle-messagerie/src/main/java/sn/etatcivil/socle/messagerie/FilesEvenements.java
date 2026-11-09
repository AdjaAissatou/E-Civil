package sn.etatcivil.socle.messagerie;

import org.springframework.amqp.core.*;
import sn.etatcivil.evenements.Routages;

import java.util.ArrayList;
import java.util.List;

/**
 * Déclare la file d'un service abonnée à certaines clés de routage, avec sa file d'erreurs (DLQ) :
 * un message qui échoue après toutes les tentatives y est rangé au lieu d'être perdu.
 */
public final class FilesEvenements {

    public static final String ECHANGE_ERREURS = Routages.ECHANGE + ".erreurs";

    private FilesEvenements() {}

    public static Declarables file(String nom, String... routages) {
        TopicExchange echange = new TopicExchange(Routages.ECHANGE, true, false);
        DirectExchange erreurs = new DirectExchange(ECHANGE_ERREURS, true, false);
        Queue file = QueueBuilder.durable(nom)
                .deadLetterExchange(ECHANGE_ERREURS)
                .deadLetterRoutingKey(nom)
                .build();
        Queue fileErreurs = QueueBuilder.durable(nom + ".erreurs").build();
        List<Declarable> elements = new ArrayList<>(List.of(echange, erreurs, file, fileErreurs,
                BindingBuilder.bind(fileErreurs).to(erreurs).with(nom)));
        for (String routage : routages) {
            elements.add(BindingBuilder.bind(file).to(echange).with(routage));
        }
        return new Declarables(elements);
    }
}
