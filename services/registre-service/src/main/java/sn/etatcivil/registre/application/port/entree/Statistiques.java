package sn.etatcivil.registre.application.port.entree;

import sn.etatcivil.registre.domaine.TypeActe;

import java.util.Map;

public record Statistiques(long total, long saisis, long valides, Map<TypeActe, Long> parType) {
}
