package sn.etatcivil.registre.adaptateur.sortie.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import sn.etatcivil.registre.domaine.TypeMention;

import java.time.Instant;
import java.time.LocalDate;

@Embeddable
class MentionEmbarquee {
    @Enumerated(EnumType.STRING)
    TypeMention type;
    @Column(name = "date_mention")
    LocalDate date;
    String reference;
    String details;
    String agent;
    Instant dateSaisie;
}
