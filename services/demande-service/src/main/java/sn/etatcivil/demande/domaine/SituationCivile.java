package sn.etatcivil.demande.domaine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Ce que le registre national sait des mariages et du décès d'une personne. */
public record SituationCivile(List<Mariage> mariages, List<String> actesDeces) {

    public record Mariage(String numeroActe, String conjoint, boolean dissous) {}

    /**
     * Ce qui empêche de délivrer un certificat, s'il y a lieu. Le refus n'est jamais automatique :
     * la demande passe à un agent, qui vérifie (homonymie, option de polygamie…) puis rattache ou rejette.
     */
    public Optional<String> obstacle(ModeleDocument modele) {
        if (!actesDeces.isEmpty()) {
            return Optional.of("Un acte de décès est inscrit au registre au nom de cette personne ("
                    + String.join(", ", actesDeces) + ").");
        }
        return switch (modele) {
            case CERTIFICAT_CELIBAT -> mariages.isEmpty() ? Optional.empty()
                    : Optional.of("Un mariage est inscrit au registre au nom de cette personne (" + liste(mariages)
                    + ") : le certificat de célibat ne peut pas être délivré.");
            case CERTIFICAT_CAPACITE_MATRIMONIALE -> {
                List<Mariage> enCours = mariages.stream().filter(m -> !m.dissous()).toList();
                yield enCours.isEmpty() ? Optional.empty()
                        : Optional.of("Mariage toujours en cours (" + liste(enCours) + ") : un officier doit vérifier "
                        + "l'option matrimoniale avant toute délivrance.");
            }
            default -> Optional.empty();
        };
    }

    /** Ce qui sera imprimé sur le certificat comme constat fait dans le registre. */
    public String constat(ModeleDocument modele) {
        return switch (modele) {
            case CERTIFICAT_CELIBAT -> "Aucun acte de mariage n'est inscrit au registre national au nom de cette personne.";
            case CERTIFICAT_CAPACITE_MATRIMONIALE -> mariages.isEmpty()
                    ? "Aucun acte de mariage n'est inscrit au registre national au nom de cette personne."
                    : "Les mariages inscrits au registre national ont été dissous (" + liste(mariages) + ").";
            case CERTIFICAT_VIE -> "Aucun acte de décès n'est inscrit au registre national au nom de cette personne.";
            default -> null;
        };
    }

    private static String liste(List<Mariage> m) {
        return m.stream().map(x -> x.numeroActe() + " avec " + x.conjoint()).collect(Collectors.joining(", "));
    }
}
