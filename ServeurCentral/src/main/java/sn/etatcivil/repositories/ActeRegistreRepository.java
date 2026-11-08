package sn.etatcivil.repositories;

import sn.etatcivil.Modeles.ActeRegistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActeRegistreRepository extends JpaRepository<ActeRegistre, Integer> {
	// Dans ActeRegistreRepository.java
	long countByNumeroPersonneIsNotNull();
	long countByNumeroCoupleIsNotNull();
	
    @Query("SELECT a FROM ActeRegistre a WHERE " +
           "(:typeActe IS NULL OR a.typeActe = :typeActe) AND " +
           "(:statut IS NULL OR a.statut = :statut) AND " +
           "(:dateDebut IS NULL OR a.dateSaisie >= :dateDebut) AND " +
           "(:dateFin IS NULL OR a.dateSaisie <= :dateFin) " +
           "ORDER BY a.dateSaisie DESC")
    List<ActeRegistre> findActesWithFilters(@Param("typeActe") String typeActe,
                                           @Param("statut") String statut,
                                           @Param("dateDebut") LocalDateTime dateDebut,
                                           @Param("dateFin") LocalDateTime dateFin);

    // Méthodes pour les statistiques
    long countByStatut(String statut);
    long countByAnnee(String annee);

    @Query("SELECT COUNT(a) FROM ActeRegistre a WHERE a.statut = 'saisi'")
    long countSaisis();

    @Query("SELECT COUNT(a) FROM ActeRegistre a WHERE a.statut = 'vérifié'")
    long countVerifies();

    @Query("SELECT COUNT(a) FROM ActeRegistre a WHERE a.statut = 'validé'")
    long countValides();

    // Recherche par numéro d'acte
    boolean existsByNumeroActeAndAnnee(String numeroActe, String annee);

    // Recherche par nom et prénom
    List<ActeRegistre> findByNomAndPrenom(String nom, String prenom);
    
    // Recherche par couple
    @Query("SELECT a FROM ActeRegistre a WHERE " +
           "(a.nom = :nom1 AND a.prenom = :prenom1 AND a.nomConjoint = :nom2 AND a.prenomConjoint = :prenom2) OR " +
           "(a.nom = :nom2 AND a.prenom = :prenom2 AND a.nomConjoint = :nom1 AND a.prenomConjoint = :prenom1)")
    List<ActeRegistre> findByNomAndPrenomAndNomConjointAndPrenomConjoint(
            @Param("nom1") String nom1, @Param("prenom1") String prenom1,
            @Param("nom2") String nom2, @Param("prenom2") String prenom2);
    
    // Recherche par numéro de personne
    List<ActeRegistre> findByNumeroPersonne(String numeroPersonne);
    
    // Recherche par numéro de couple
    List<ActeRegistre> findByNumeroCouple(String numeroCouple);
}