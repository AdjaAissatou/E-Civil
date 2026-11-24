package sn.etatcivil.demande.adaptateur.sortie.persistance;

import jakarta.persistence.*;

@Entity
@Table(name = "motif")
class MotifEntite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    String libelle;
    int ordre;
    @Column(name = "pieces_requises") String piecesRequises;
    @Column(name = "type_document_id") Long typeDocumentId;
}
