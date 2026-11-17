package sn.etatcivil.document.domaine;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ce qui figure sur le document en plus de l'acte : le modèle (extrait, copie, certificat…), le motif de la demande,
 * le constat fait dans le registre (certificats) et les enfants du couple (livret de famille). Figé à la signature.
 */
public record Complements(String modele, String motif, String constat, List<Enfant> enfants) {

    public static final String COPIE_INTEGRALE = "COPIE_INTEGRALE";

    /** Une naissance inscrite au registre, reprise dans le livret de famille. */
    public record Enfant(String numeroActe, String prenom, String nom, String sexe, LocalDate dateNaissance,
                         String lieuNaissance) {}

    public Complements {
        enfants = enfants == null ? List.of() : List.copyOf(enfants);
    }

    public boolean estCertificat() { return modele != null && modele.startsWith("CERTIFICAT_"); }

    public boolean estLivret() { return "LIVRET_FAMILLE".equals(modele); }

    String texteCanonique() {
        String e = enfants.stream().map(x -> String.join(",", Stream.of(x.numeroActe(), x.prenom(), x.nom(), x.sexe(),
                x.dateNaissance(), x.lieuNaissance()).map(v -> Objects.toString(v, "")).toList()))
                .collect(Collectors.joining(";"));
        return String.join("|", Objects.toString(modele, ""), Objects.toString(motif, ""), Objects.toString(constat, ""), e);
    }
}
