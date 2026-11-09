package sn.etatcivil.registre.domaine;

import java.util.List;

public record ResultatRecherche(Resultat resultat, List<Acte> actes) {

    /** TROUVE : un seul acte, délivrable. AMBIGU : plusieurs, un agent doit trancher. NON_TROUVE : aucun. */
    public enum Resultat { TROUVE, AMBIGU, NON_TROUVE }

    public static ResultatRecherche de(List<Acte> actes) {
        Resultat r = actes.isEmpty() ? Resultat.NON_TROUVE : actes.size() == 1 ? Resultat.TROUVE : Resultat.AMBIGU;
        return new ResultatRecherche(r, List.copyOf(actes));
    }
}
