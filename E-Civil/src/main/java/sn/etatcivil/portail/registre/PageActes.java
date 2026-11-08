package sn.etatcivil.portail.registre;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageActes(List<ActeRegistre> content, Infos page) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Infos(int size, int number, long totalElements, int totalPages) {}
}
