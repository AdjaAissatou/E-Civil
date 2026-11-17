package sn.etatcivil.paiement.application.port.sortie;

import java.time.Instant;

public interface Horloge {
    Instant maintenant();
}
