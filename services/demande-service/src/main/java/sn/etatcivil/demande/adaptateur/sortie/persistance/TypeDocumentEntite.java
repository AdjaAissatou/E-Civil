package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.demande.domaine.TypeActe;

@Entity
@Table(name = "type_document")
class TypeDocumentEntite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    String code;
    String libelle;
    String description;
    @Enumerated(EnumType.STRING) @Column(name = "type_acte") TypeActe typeActe;
    @Column(name = "copie_integrale") boolean copieIntegrale;
    int tarif;
    @Column(name = "delai_jours") int delaiJours;
    boolean actif;
}
