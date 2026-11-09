package sn.etatcivil.central.acte;

import java.util.Map;

public record Statistiques(long total, long saisis, long valides, Map<TypeActe, Long> parType) {
}
