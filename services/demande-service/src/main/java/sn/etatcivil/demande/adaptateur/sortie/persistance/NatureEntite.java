package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;

@Entity
@Table(name = "nature")
class NatureEntite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    String code;
    String libelle;
    String description;
    int ordre;
    boolean active;
}
