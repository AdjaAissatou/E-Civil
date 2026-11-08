package sn.etatcivil.portail.catalogue;

import jakarta.persistence.*;

@Entity
@Table(name = "motif")
public class Motif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String libelle;

    @Column(nullable = false)
    private int ordre;

    public Long getId() { return id; }
    public String getLibelle() { return libelle; }

    public boolean estAutre() { return "Autre".equals(libelle); }
}
