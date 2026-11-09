package sn.etatcivil.registre.adaptateur.sortie.persistance;

import jakarta.persistence.*;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Représentation relationnelle d'un acte ; n'est jamais exposée hors de l'adaptateur. */
@Entity
@Table(name = "acte")
class ActeEntite {

    @Id
    UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acte")
    TypeActe typeActe;

    @Column(name = "numero_acte")
    String numeroActe;

    Integer annee;

    @Enumerated(EnumType.STRING)
    StatutActe statut;

    @Column(name = "centre")
    String centre;
    @Column(name = "commune")
    String commune;
    @Column(name = "volume")
    String volume;
    @Column(name = "folio")
    String folio;
    @Column(name = "date_evenement")
    LocalDate dateEvenement;
    @Column(name = "lieu_evenement")
    String lieuEvenement;
    @Column(name = "nom")
    String nom;
    @Column(name = "prenom")
    String prenom;
    @Column(name = "sexe")
    String sexe;
    @Column(name = "date_naissance")
    LocalDate dateNaissance;
    @Column(name = "lieu_naissance")
    String lieuNaissance;
    @Column(name = "nom_pere")
    String nomPere;
    @Column(name = "prenom_pere")
    String prenomPere;
    @Column(name = "nom_mere")
    String nomMere;
    @Column(name = "prenom_mere")
    String prenomMere;
    @Column(name = "nom_conjoint")
    String nomConjoint;
    @Column(name = "prenom_conjoint")
    String prenomConjoint;
    @Column(name = "date_naissance_conjoint")
    LocalDate dateNaissanceConjoint;
    @Column(name = "lieu_naissance_conjoint")
    String lieuNaissanceConjoint;
    @Column(name = "heure_deces")
    String heureDeces;
    @Column(name = "cause_deces")
    String causeDeces;
    @Column(name = "temoin1")
    String temoin1;
    @Column(name = "temoin2")
    String temoin2;
    @Column(name = "mentions_marginales")
    String mentionsMarginales;
    @Column(name = "observations")
    String observations;
    @Column(name = "nom_normalise")
    String nomNormalise;
    @Column(name = "prenom_normalise")
    String prenomNormalise;
    @Column(name = "nom_conjoint_normalise")
    String nomConjointNormalise;
    @Column(name = "prenom_conjoint_normalise")
    String prenomConjointNormalise;

    @Column(name = "agent_saisie")
    String agentSaisie;

    @Column(name = "date_saisie")
    Instant dateSaisie;

    @Column(name = "date_modification")
    Instant dateModification;

    @Version
    Integer version;
}
