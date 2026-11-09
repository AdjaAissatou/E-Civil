package sn.etatcivil.portail.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sn.etatcivil.portail.catalogue.TypeActe;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatistiquesRegistre(long total, long saisis, long valides, Map<TypeActe, Long> parType) {
}
