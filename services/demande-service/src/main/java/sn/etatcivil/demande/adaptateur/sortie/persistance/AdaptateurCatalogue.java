package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.CatalogueRepository;
import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.List;
import java.util.Optional;

@Component
class AdaptateurCatalogue implements CatalogueRepository {

    private final TypesDocumentJpa types;
    private final MotifsJpa motifs;

    AdaptateurCatalogue(TypesDocumentJpa types, MotifsJpa motifs) {
        this.types = types;
        this.motifs = motifs;
    }

    @Override
    public List<TypeDocument> documents(boolean disponiblesSeulement) {
        return (disponiblesSeulement ? types.findByActifTrueOrderByIdAsc() : types.findAllByOrderByIdAsc())
                .stream().map(AdaptateurCatalogue::versDomaine).toList();
    }

    @Override
    public Optional<TypeDocument> document(Long id) {
        return types.findById(id).map(AdaptateurCatalogue::versDomaine);
    }

    @Override
    public void enregistrer(TypeDocument t) {
        TypeDocumentEntite e = types.findById(t.id()).orElseThrow();
        e.tarif = t.tarif();
        e.delaiJours = t.delaiJours();
        e.actif = t.actif();
    }

    @Override
    public List<Motif> motifs() {
        return motifs.findAllByOrderByOrdreAsc().stream().map(m -> new Motif(m.id, m.libelle, m.ordre)).toList();
    }

    @Override
    public Optional<Motif> motif(Long id) {
        return motifs.findById(id).map(m -> new Motif(m.id, m.libelle, m.ordre));
    }

    private static TypeDocument versDomaine(TypeDocumentEntite e) {
        return new TypeDocument(e.id, e.code, e.libelle, e.description, e.typeActe, e.copieIntegrale, e.tarif,
                e.delaiJours, e.actif);
    }
}
