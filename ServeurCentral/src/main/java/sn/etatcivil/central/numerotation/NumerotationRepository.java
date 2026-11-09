package sn.etatcivil.central.numerotation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NumerotationRepository extends JpaRepository<Numerotation, Numerotation.Cle> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Numerotation n WHERE n.typeActe = :type AND n.annee = :annee")
    Optional<Numerotation> verrouiller(@Param("type") String typeActe, @Param("annee") Integer annee);

    /** Crée le compteur s'il n'existe pas ; une création concurrente attend puis ne fait rien. */
    @Modifying
    @Query(value = "INSERT INTO numerotation (type_acte, annee, dernier_numero) VALUES (:type, :annee, 0) "
            + "ON CONFLICT DO NOTHING", nativeQuery = true)
    void creerSiAbsent(@Param("type") String typeActe, @Param("annee") Integer annee);
}
