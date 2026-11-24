package sn.etatcivil.habilitation.adaptateur.sortie.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "initialisation")
class MarqueurEntite {
    @Id String etape;
    Instant date;
}
