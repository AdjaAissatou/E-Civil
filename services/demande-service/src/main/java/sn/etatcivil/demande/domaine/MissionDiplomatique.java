package sn.etatcivil.demande.domaine;

import java.util.List;

/**
 * Ambassade, consulat ou représentation du Sénégal à l'étranger. Un document papier peut y être retiré
 * par un Sénégalais résidant dans un des pays qu'elle couvre.
 */
public record MissionDiplomatique(Long id, String type, String nom, Long paysId, String pays, String ville,
                                  String codePostal, String competence, String adresse, String telephone, String email,
                                  List<Long> paysCouverts, boolean active) {

    public MissionDiplomatique {
        if (nom == null || nom.isBlank()) throw new RegleMetierException("Le nom de la mission est obligatoire");
        if (adresse == null || adresse.isBlank()) throw new RegleMetierException("L'adresse de la mission est obligatoire");
        if (ville == null || ville.isBlank()) throw new RegleMetierException("La ville de la mission est obligatoire");
        if (type == null || type.isBlank()) throw new RegleMetierException("Le type de mission est obligatoire");
        if (paysId == null) throw new RegleMetierException("Le pays hôte est obligatoire");
        paysCouverts = paysCouverts == null ? List.of() : List.copyOf(paysCouverts);
    }

    /** Adresse de retrait imprimée sur la demande. */
    public String adresseDeRetrait() {
        return "Retrait à la mission : " + nom + ", " + adresse;
    }

    public boolean couvre(Long pays) { return paysId.equals(pays) || paysCouverts.contains(pays); }
}
