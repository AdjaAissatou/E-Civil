package sn.etatcivil.habilitation.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.habilitation.domaine.TypeOperation;

import java.time.Instant;

@Entity
@Table(name = "operation")
class OperationEntite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    Instant date;
    String auteur;
    @Enumerated(EnumType.STRING) TypeOperation type;
    String cible;
    String detail;
}
