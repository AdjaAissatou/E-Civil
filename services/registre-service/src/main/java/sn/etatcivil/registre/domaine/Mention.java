package sn.etatcivil.registre.domaine;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Mention marginale : inscription portée en marge d'un acte pour le compléter ou le modifier (mariage, divorce,
 * légitimation…). Elle se rattache à un acte et n'en modifie jamais le contenu d'origine.
 *
 * @param reference décision ou acte à l'origine de la mention (jugement, acte de mariage…)
 */
public record Mention(TypeMention type, LocalDate date, String reference, String details, String agent,
                      Instant dateSaisie) {

    public Mention {
        if (type == null) throw new RegleMetierException("Le type de la mention est obligatoire");
        if (date == null) throw new RegleMetierException("La date de la mention est obligatoire");
        if (date.isAfter(LocalDate.now())) throw new RegleMetierException("La date de la mention ne peut pas être dans le futur");
        if (DonneesActe.vide(details) && type == TypeMention.AUTRE) {
            throw new RegleMetierException("Précisez le contenu de la mention");
        }
        if (details != null && details.length() > 1000) throw new RegleMetierException("La mention ne doit pas dépasser 1000 caractères");
        if (reference != null && reference.length() > 300) throw new RegleMetierException("La référence ne doit pas dépasser 300 caractères");
    }

    /** Texte de la mention tel qu'il figure en marge de l'acte. */
    public String texte() {
        StringBuilder s = new StringBuilder(type.libelle()).append(" du ")
                .append(String.format("%02d/%02d/%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear()));
        if (!DonneesActe.vide(reference)) s.append(" (").append(reference.trim()).append(')');
        if (!DonneesActe.vide(details)) s.append(" : ").append(details.trim());
        return s.toString();
    }
}
