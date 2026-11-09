package sn.etatcivil.portail.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultatRecherche(Resultat resultat, List<ActeRegistre> actes) {
    public enum Resultat { TROUVE, AMBIGU, NON_TROUVE }
}
