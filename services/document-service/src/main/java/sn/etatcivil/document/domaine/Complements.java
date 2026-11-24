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
public record Complements(String modele, String motif, String constat, List<Enfant> enfants,
                          LocalDate valableJusquAu, Declaration declaration, String mention) {

    public Complements(String modele, String motif, String constat, List<Enfant> enfants) {
        this(modele, motif, constat, enfants, null, null, null);
    }

    public static final String COPIE_INTEGRALE = "COPIE_INTEGRALE";

    /** Une naissance inscrite au registre, reprise dans le livret de famille. */
    public record Enfant(String numeroActe, String prenom, String nom, String sexe, LocalDate dateNaissance,
                         String lieuNaissance) {}

    public Complements {
        enfants = enfants == null ? List.of() : List.copyOf(enfants);
    }

    public boolean estCertificat() { return modele != null && modele.startsWith("CERTIFICAT_"); }

    /** Certificat établi d'après l'acte de naissance de la personne (situation, nationalité, domicile). */
    public boolean estCertificatDePersonne() {
        return estCertificat() && !"CERTIFICAT_ACTE".equals(modele) && !etabliSurComparution();
    }

    /** Établi par l'officier sur comparution de la personne, sans acte du registre. */
    public boolean etabliSurComparution() {
        return "CERTIFICAT_NON_APPARTENANCE".equals(modele) || "CERTIFICAT_COUTUME".equals(modele);
    }

    public boolean estExtraitDeMention() { return "EXTRAIT_MENTION".equals(modele); }

    public boolean estActeOriginal() { return "ACTE_ORIGINAL".equals(modele); }

    /** Le document a-t-il cessé d'être valable à cette date ? */
    public boolean expire(LocalDate aujourdhui) { return valableJusquAu != null && aujourdhui.isAfter(valableJusquAu); }

    public boolean estLivret() { return "LIVRET_FAMILLE".equals(modele); }

    String texteCanonique() {
        String e = enfants.stream().map(x -> String.join(",", Stream.of(x.numeroActe(), x.prenom(), x.nom(), x.sexe(),
                x.dateNaissance(), x.lieuNaissance()).map(v -> Objects.toString(v, "")).toList()))
                .collect(Collectors.joining(";"));
        String base = String.join("|", Objects.toString(modele, ""), Objects.toString(motif, ""), Objects.toString(constat, ""), e);
        // ajoutés seulement s'ils existent : l'empreinte des documents antérieurs ne change pas
        if (valableJusquAu == null && declaration == null && mention == null) return base;
        return base + "|" + Objects.toString(valableJusquAu, "") + "|"
                + (declaration == null ? "" : declaration.texteCanonique()) + "|" + Objects.toString(mention, "");
    }
}
