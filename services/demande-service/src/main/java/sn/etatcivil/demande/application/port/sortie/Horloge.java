package sn.etatcivil.demande.application.port.sortie;

import java.time.Instant;

public interface Horloge {
    Instant maintenant();
}
