package sn.etatcivil.document.adaptateur.sortie.persistance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import sn.etatcivil.document.application.port.sortie.DocumentRepository;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;

import java.util.Optional;
import java.util.UUID;

@Component
class AdaptateurDocuments implements DocumentRepository {

    private final DocumentsJpa jpa;
    private final ObjectMapper json;

    AdaptateurDocuments(DocumentsJpa jpa, ObjectMapper json) {
        this.jpa = jpa;
        this.json = json;
    }

    @Override
    public void enregistrer(DocumentDelivre d) {
        DocumentEntite e = new DocumentEntite();
        e.id = d.id();
        e.demandeId = d.demandeId();
        e.reference = d.reference();
        e.demandeurId = d.demandeurId();
        e.libelleDocument = d.libelleDocument();
        e.copieIntegrale = d.copieIntegrale();
        e.format = d.format();
        try {
            e.contenu = json.writeValueAsString(d.contenu());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        e.signataire = d.signataire();
        e.dateSignature = d.dateSignature();
        e.codeVerification = d.codeVerification();
        e.empreinte = d.empreinte();
        jpa.saveAndFlush(e);
    }

    @Override
    public Optional<DocumentDelivre> parDemande(UUID demandeId) {
        return jpa.findByDemandeId(demandeId).map(this::versDomaine);
    }

    @Override
    public Optional<DocumentDelivre> parCode(String code) {
        return jpa.findByCodeVerification(code).map(this::versDomaine);
    }

    private DocumentDelivre versDomaine(DocumentEntite e) {
        try {
            return new DocumentDelivre(e.id, e.demandeId, e.reference, e.demandeurId, e.libelleDocument, e.copieIntegrale,
                    e.format, json.readValue(e.contenu, ContenuActe.class), e.signataire, e.dateSignature,
                    e.codeVerification, e.empreinte);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Contenu illisible pour " + e.reference, ex);
        }
    }
}
