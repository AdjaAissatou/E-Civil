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
    @Column(name = "nature_id") Long natureId;
    @Enumerated(EnumType.STRING) @Column(name = "type_acte") TypeActe typeActe;
    @Enumerated(EnumType.STRING) ModeleDocument modele;
    @Enumerated(EnumType.STRING) @Column(name = "origine_acte") OrigineActe origine;
    @Column(name = "type_mention") String typeMention;
    /** Rubriques du formulaire, séparées par des virgules (PARENTS,CONJOINT…). */
    String champs;
    int tarif;
    @Column(name = "delai_jours") int delaiJours;
    @Column(name = "duree_validite_jours") Integer dureeValiditeJours;
    @Column(name = "mots_cles") String motsCles;
    @Column(name = "pieces_a_fournir") String piecesAFournir;
    @Column(name = "est_original") boolean estOriginal;
    @Column(name = "copie_de_id") Long copieDeId;
    boolean actif;
    @Column(name = "verification_identite") boolean verificationIdentite;
}
