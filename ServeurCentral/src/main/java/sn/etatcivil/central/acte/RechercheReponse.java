package sn.etatcivil.central.acte;

import java.util.List;

/**
 * TROUVE : un seul acte correspond, il peut être délivré.
 * AMBIGU : plusieurs actes correspondent, un agent doit trancher.
 * NON_TROUVE : aucun acte validé ne correspond.
 */
public record RechercheReponse(Resultat resultat, List<ActeReponse> actes) {

    public enum Resultat { TROUVE, AMBIGU, NON_TROUVE }

    public static RechercheReponse de(List<ActeReponse> actes) {
        Resultat r = actes.isEmpty() ? Resultat.NON_TROUVE
                : actes.size() == 1 ? Resultat.TROUVE : Resultat.AMBIGU;
        return new RechercheReponse(r, actes);
    }
}
