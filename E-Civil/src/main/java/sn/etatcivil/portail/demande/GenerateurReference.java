package sn.etatcivil.portail.demande;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/** Références lisibles et uniques : DEM-2026-000001, DEM-2026-000002… */
@Component
class GenerateurReference {

    private final EntityManager em;

    GenerateurReference(EntityManager em) {
        this.em = em;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    String suivante() {
        int annee = Year.now().getValue();
        em.createNativeQuery("INSERT INTO compteur_reference (annee, dernier_numero) VALUES (?1, 0) ON CONFLICT DO NOTHING")
                .setParameter(1, annee).executeUpdate();
        Number dernier = (Number) em.createNativeQuery("SELECT dernier_numero FROM compteur_reference WHERE annee = ?1 FOR UPDATE")
                .setParameter(1, annee).getSingleResult();
        long numero = dernier.longValue() + 1;
        em.createNativeQuery("UPDATE compteur_reference SET dernier_numero = ?1 WHERE annee = ?2")
                .setParameter(1, numero).setParameter(2, annee).executeUpdate();
        return String.format("DEM-%d-%06d", annee, numero);
    }
}
