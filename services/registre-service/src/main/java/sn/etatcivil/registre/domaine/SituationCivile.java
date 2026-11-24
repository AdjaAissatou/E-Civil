package sn.etatcivil.registre.domaine;

import java.util.Comparator;
import java.util.List;

/**
 * Ce que le registre national sait de la situation d'une personne : ses mariages (dissous ou non) et son décès.
 * Sert à délivrer les certificats de célibat, de capacité matrimoniale et de vie.
 */
public record SituationCivile(List<Mariage> mariages, List<Acte> deces) {

    /** Un mariage est dissous par un divorce du même couple ou par le décès du conjoint, postérieurs au mariage. */
    public record Mariage(Acte acte, Personne conjoint, Acte dissolution) {
        /** Dissous par un acte (divorce, décès du conjoint) ou par une mention de divorce en marge du mariage. */
        public boolean dissous() { return dissolution != null || mentionDivorce() != null; }

        public Mention mentionDivorce() {
            return acte.mentions(TypeMention.DIVORCE).stream().findFirst().orElse(null);
        }
    }

    /**
     * @param mariages      actes de mariage validés où la personne figure
     * @param divorces      actes de divorce validés où la personne figure
     * @param decesPersonne actes de décès validés de la personne
     * @param decesDe       recherche des actes de décès validés d'une autre personne (le conjoint)
     */
    public static SituationCivile etablir(Personne personne, List<Acte> mariages, List<Acte> divorces,
                                          List<Acte> decesPersonne, java.util.function.Function<Personne, List<Acte>> decesDe) {
        List<Mariage> resultat = mariages.stream()
                .sorted(Comparator.comparing(a -> a.donnees().dateEvenement()))
                .map(m -> {
                    Personne conjoint = m.autreConjoint(personne.nom(), personne.prenom());
                    Acte dissolution = divorces.stream()
                            .filter(d -> d.concerneLeCouple(personne, conjoint))
                            .filter(d -> !d.donnees().dateEvenement().isBefore(m.donnees().dateEvenement()))
                            .findFirst()
                            .orElseGet(() -> decesDe.apply(conjoint).stream()
                                    .filter(d -> !d.donnees().dateEvenement().isBefore(m.donnees().dateEvenement()))
                                    .findFirst().orElse(null));
                    return new Mariage(m, conjoint, dissolution);
                })
                .toList();
        return new SituationCivile(resultat, decesPersonne);
    }
}
