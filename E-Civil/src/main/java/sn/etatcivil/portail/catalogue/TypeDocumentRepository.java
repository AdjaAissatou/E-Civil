package sn.etatcivil.portail.catalogue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Long> {
    List<TypeDocument> findByActifTrueOrderByIdAsc();
    List<TypeDocument> findAllByOrderByIdAsc();
}
