package sn.etatcivil.notification.application;

import java.lang.annotation.*;

/**
 * Marque une implémentation de cas d'usage. La configuration l'enregistre comme composant et rend
 * chacune de ses méthodes transactionnelle, sans que la couche application ne dépende de Spring.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CasDUsage {
}
