package sn.etatcivil.demande.adaptateur.sortie.persistance;

import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.CatalogueRepository;
import sn.etatcivil.demande.domaine.Champ;
import sn.etatcivil.demande.domaine.Motif;
import sn.etatcivil.demande.domaine.Nature;
import sn.etatcivil.demande.domaine.TypeDocument;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class AdaptateurCatalogue implements CatalogueRepository {

    /** Aucune rubrique (distinct de « non renseigné », qui reprend les rubriques du type d'acte). */
    private static final String AUCUN = "-";

    private final TypesDocumentJpa types;
    private final MotifsJpa motifs;
    private final NaturesJpa natures;

    AdaptateurCatalogue(TypesDocumentJpa types, MotifsJpa motifs, NaturesJpa natures) {
        this.types = types;
        this.motifs = motifs;
        this.natures = natures;
    }

    @Override
    public List<TypeDocument> documents(boolean disponiblesSeulement) {
        Map<Long, String> libelles = libellesNatures();
        return (disponiblesSeulement ? types.findByActifTrueOrderByIdAsc() : types.findAllByOrderByIdAsc())
                .stream().map(e -> versDomaine(e, libelles)).toList();
    }

    @Override
    public Optional<TypeDocument> document(Long id) {
        return types.findById(id).map(e -> versDomaine(e, libellesNatures()));
    }

    @Override
    public Optional<TypeDocument> documentParCode(String code) {
        return types.findByCode(code).map(e -> versDomaine(e, libellesNatures()));
    }

    @Override
    public Long enregistrer(TypeDocument t) {
        TypeDocumentEntite e = t.id() == null ? new TypeDocumentEntite() : types.findById(t.id()).orElseThrow();
        e.code = t.code();
        e.libelle = t.libelle();
        e.description = t.description();
        e.natureId = t.natureId();
        e.typeActe = t.typeActe();
        e.modele = t.modele();
        e.origine = t.origine();
        e.typeMention = t.typeMention();
        e.champs = t.champs().isEmpty() ? AUCUN : t.champs().stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        e.tarif = t.tarif();
        e.delaiJours = t.delaiJours();
        e.dureeValiditeJours = t.dureeValiditeJours();
        e.motsCles = t.motsCles();
        e.piecesAFournir = t.piecesAFournir();
        e.estOriginal = t.estOriginal();
        e.copieDeId = t.copieDeId();
        e.actif = t.actif();
        return types.saveAndFlush(e).id;
    }

    @Override
    public List<Nature> natures() {
        return natures.findAllByOrderByOrdreAscLibelleAsc().stream().map(AdaptateurCatalogue::versDomaine).toList();
    }

    @Override
    public Optional<Nature> nature(Long id) {
        return id == null ? Optional.empty() : natures.findById(id).map(AdaptateurCatalogue::versDomaine);
    }

    @Override
    public Optional<Nature> natureParCode(String code) {
        return natures.findByCode(code).map(AdaptateurCatalogue::versDomaine);
    }

    @Override
    public Long enregistrer(Nature n) {
        NatureEntite e = n.id() == null ? new NatureEntite() : natures.findById(n.id()).orElseThrow();
        e.code = n.code();
        e.libelle = n.libelle();
        e.description = n.description();
        e.ordre = n.ordre();
        e.active = n.active();
        return natures.saveAndFlush(e).id;
    }

    @Override
    public List<Motif> motifs() {
        return motifs.findAllByOrderByOrdreAscIdAsc().stream().map(AdaptateurCatalogue::versDomaine).toList();
    }

    @Override
    public Optional<Motif> motif(Long id) {
        return motifs.findById(id).map(AdaptateurCatalogue::versDomaine);
    }

    @Override
    public Long enregistrer(Motif m) {
        MotifEntite e = m.id() == null ? new MotifEntite() : motifs.findById(m.id()).orElseThrow();
        e.libelle = m.libelle();
        e.ordre = m.ordre();
        e.piecesRequises = m.piecesRequises();
        e.typeDocumentId = m.typeDocumentId();
        return motifs.saveAndFlush(e).id;
    }

    private Map<Long, String> libellesNatures() {
        return natures.findAll().stream().collect(Collectors.toMap(n -> n.id, n -> n.libelle));
    }

    private static Motif versDomaine(MotifEntite m) {
        return new Motif(m.id, m.libelle, m.ordre, m.piecesRequises, m.typeDocumentId);
    }

    private static Nature versDomaine(NatureEntite n) {
        return new Nature(n.id, n.code, n.libelle, n.description, n.ordre, n.active);
    }

    private static TypeDocument versDomaine(TypeDocumentEntite e, Map<Long, String> natures) {
        Set<Champ> champs = e.champs == null || e.champs.isBlank() ? TypeDocument.champsParDefaut(e.typeActe)
                : AUCUN.equals(e.champs) ? EnumSet.noneOf(Champ.class) : Arrays.stream(e.champs.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Champ::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(Champ.class)));
        return new TypeDocument(e.id, e.code, e.libelle, e.description, e.natureId, natures.get(e.natureId), e.typeActe,
                e.modele, e.origine, e.typeMention, champs, e.tarif, e.delaiJours, e.dureeValiditeJours, e.motsCles,
                e.piecesAFournir, e.estOriginal, e.copieDeId, e.actif);
    }
}
