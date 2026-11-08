package sn.etatcivil.portail.catalogue;

import jakarta.persistence.*;

@Entity
@Table(name = "type_document")
public class TypeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private String libelle;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acte", nullable = false, updatable = false)
    private TypeActe typeActe;

    @Column(name = "copie_integrale", nullable = false)
    private boolean copieIntegrale;

    /** En francs CFA. */
    @Column(nullable = false)
    private int tarif;

    @Column(name = "delai_jours", nullable = false)
    private int delaiJours;

    @Column(nullable = false)
    private boolean actif = true;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TypeActe getTypeActe() { return typeActe; }
    public boolean isCopieIntegrale() { return copieIntegrale; }
    public int getTarif() { return tarif; }
    public void setTarif(int tarif) { this.tarif = tarif; }
    public int getDelaiJours() { return delaiJours; }
    public void setDelaiJours(int delaiJours) { this.delaiJours = delaiJours; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
