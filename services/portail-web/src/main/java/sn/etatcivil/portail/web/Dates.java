package sn.etatcivil.portail.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Affichage des horodatages des services (UTC) à l'heure de Dakar ; utilisé dans les pages via ${@dates…}. */
@Component("dates")
public class Dates {
    private static final ZoneId DAKAR = ZoneId.of("Africa/Dakar");
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH).withZone(DAKAR);
    private static final DateTimeFormatter HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH).withZone(DAKAR);

    public String jour(Instant i) { return i == null ? "" : JOUR.format(i); }

    public String heure(Instant i) { return i == null ? "" : HEURE.format(i); }
}
