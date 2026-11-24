package sn.etatcivil.document.domaine;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Constat imprimé sur les certificats qui ne dépendent pas de la situation civile (calculée par demande-service) :
 * certificats d'acte, de nationalité, de domicile, et documents établis sur comparution.
 */
public final class Constats {
    private Constats() {}

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    public static String pour(String modele, ContenuActe a, Declaration d, String mention) {
        return switch (modele) {
            case "CERTIFICAT_ACTE" -> switch (a.typeActe()) {
                case "NAISSANCE" -> "La naissance de " + a.prenom() + " " + a.nom() + ", survenue le " + jour(a)
                        + " à " + a.lieuEvenement() + ", est inscrite au registre national sous le n° " + a.numeroActe() + ".";
                case "MARIAGE" -> "Le mariage de " + a.prenom() + " " + a.nom() + " et de " + a.prenomConjoint() + " "
                        + a.nomConjoint() + ", célébré le " + jour(a) + " à " + a.lieuEvenement()
                        + ", est inscrit au registre national sous le n° " + a.numeroActe() + ".";
                case "DECES" -> "Le décès de " + a.prenom() + " " + a.nom() + ", survenu le " + jour(a) + " à "
                        + a.lieuEvenement() + ", est inscrit au registre national sous le n° " + a.numeroActe() + ".";
                case "RECONNAISSANCE" -> "La reconnaissance de l'enfant " + a.prenom() + " " + a.nom() + ", faite le "
                        + jour(a) + ", est inscrite au registre national sous le n° " + a.numeroActe() + ".";
                default -> "L'acte n° " + a.numeroActe() + " est inscrit au registre national.";
            };
            case "CERTIFICAT_NATIONALITE" -> a.prenom() + " " + a.nom() + ", dont la naissance est inscrite au registre "
                    + "national sous le n° " + a.numeroActe() + ", est de nationalité sénégalaise par filiation"
                    + (a.nomPere() != null || a.nomMere() != null ? " (" + parents(a) + ")" : "") + ".";
            case "CERTIFICAT_DOMICILE" -> a.prenom() + " " + a.nom() + " a déclaré, en présence"
                    + (d != null && d.temoin2() != null ? " des témoins " + d.temoin1() + " et " + d.temoin2()
                    : d != null && d.temoin1() != null ? " du témoin " + d.temoin1() : " de l'officier")
                    + ", être domicilié(e) à : " + (d == null ? "" : d.residence()) + ".";
            case "CERTIFICAT_NON_APPARTENANCE" -> nomDeclare(d) + ", comparant(e) en personne, a justifié ne pas "
                    + "appartenir à la nationalité sénégalaise. Aucun acte de naissance n'est requis : le présent "
                    + "certificat est établi sur sa déclaration" + (d != null && d.precisions() != null
                    ? " (" + d.precisions() + ")" : "") + ".";
            case "CERTIFICAT_COUTUME" -> "Au vu de la déclaration de " + nomDeclare(d) + ", la coutume applicable à son "
                    + "mariage est la suivante : " + (d == null ? "" : d.precisions()) + ".";
            case "EXTRAIT_MENTION" -> mention == null ? null : "En marge de l'acte n° " + a.numeroActe() + " figure : " + mention;
            default -> null;
        };
    }

    private static String jour(ContenuActe a) {
        return a.dateEvenement() == null ? "" : a.dateEvenement().format(JOUR);
    }

    private static String parents(ContenuActe a) {
        String pere = a.nomPere() == null ? null : "fils/fille de " + (a.prenomPere() == null ? "" : a.prenomPere() + " ") + a.nomPere();
        String mere = a.nomMere() == null ? null : (pere == null ? "fils/fille de " : "et de ")
                + (a.prenomMere() == null ? "" : a.prenomMere() + " ") + a.nomMere();
        return pere == null ? mere : mere == null ? pere : pere + " " + mere;
    }

    private static String nomDeclare(Declaration d) {
        return d == null ? "La personne" : (d.prenom() + " " + d.nom()).trim();
    }
}
