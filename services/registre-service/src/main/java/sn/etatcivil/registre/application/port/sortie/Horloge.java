package sn.etatcivil.registre.application.port.sortie;

import java.time.Instant;

public interface Horloge {
    Instant maintenant();
}
