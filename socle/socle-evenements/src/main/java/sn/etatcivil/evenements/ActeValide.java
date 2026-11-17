package sn.etatcivil.evenements;

import java.time.Instant;

/** Un acte transcrit vient d'être validé par un officier : il peut désormais être retrouvé et délivré. */
public record ActeValide(
        String acteId,
        String numeroActe,
        String typeActe,
        Instant date) {
}
