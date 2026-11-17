package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.demande.domaine.ModeleDocument;
import sn.etatcivil.demande.domaine.OrigineActe;
import sn.etatcivil.demande.domaine.TypeActe;

@Entity
@Table(name = "type_document")
class TypeDocumentEntite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    String code;
    String libelle;
    String description;
    @Enumerated(EnumType.STRING) @Column(name = "type_acte") TypeActe typeActe;
    @Enumerated(EnumType.STRING) ModeleDocument modele;
    @Enumerated(EnumType.STRING) @Column(name = "origine_acte") OrigineActe origine;
    int tarif;
    @Column(name = "delai_jours") int delaiJours;
    boolean actif;
}
