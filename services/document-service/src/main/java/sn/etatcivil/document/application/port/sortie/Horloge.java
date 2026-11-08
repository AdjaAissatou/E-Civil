package sn.etatcivil.document.application.port.sortie;

import java.time.Instant;

public interface Horloge {
    Instant maintenant();
}
