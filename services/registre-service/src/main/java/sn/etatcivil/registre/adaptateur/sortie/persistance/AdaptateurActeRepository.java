package sn.etatcivil.registre.adaptateur.sortie.persistance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import sn.etatcivil.registre.application.port.entree.FiltreActes;
import sn.etatcivil.registre.application.port.entree.PageDemandee;
import sn.etatcivil.registre.application.port.entree.PageResultat;
import sn.etatcivil.registre.application.port.sortie.ActeRepository;
import sn.etatcivil.registre.domaine.Acte;
import sn.etatcivil.registre.domaine.Normalisation;
import sn.etatcivil.registre.domaine.StatutActe;
import sn.etatcivil.registre.domaine.TypeActe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class AdaptateurActeRepository implements ActeRepository {

    private final ActeJpaRepository jpa;

    AdaptateurActeRepository(ActeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Acte enregistrer(Acte acte) {
        ActeEntite e = jpa.findById(acte.id()).orElseGet(ActeEntite::new);
        if (e.version != null && acte.version() != null && !e.version.equals(acte.version())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(ActeEntite.class, acte.id());
        }
        CorrespondanceActe.copier(acte, e);
        return CorrespondanceActe.versDomaine(jpa.saveAndFlush(e));
    }

    @Override
    public Optional<Acte> parId(UUID id) {
        return jpa.findById(id).map(CorrespondanceActe::versDomaine);
    }

    @Override
    public Optional<Acte> parNumero(String numero) {
        return jpa.findByNumeroActe(numero).map(CorrespondanceActe::versDomaine);
    }

    @Override
    public List<Acte> candidats(TypeActe type, String nom, String prenom) {
        return jpa.candidats(type, nom, prenom).stream().map(CorrespondanceActe::versDomaine).toList();
    }

    @Override
    public PageResultat<Acte> lister(FiltreActes f, PageDemandee page) {
        // Seuls les critères renseignés sont ajoutés (pas de paramètre null dans la requête SQL)
        Specification<ActeEntite> spec = (r, q, cb) -> cb.conjunction();
        if (f.type() != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("typeActe"), f.type()));
        if (f.statut() != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("statut"), f.statut()));
        if (f.nom() != null && !f.nom().isBlank()) {
            String motif = "%" + Normalisation.normaliser(f.nom()) + "%";
            spec = spec.and((r, q, cb) -> cb.like(r.get("nomNormalise"), motif));
        }
        Page<ActeEntite> p = jpa.findAll(spec, PageRequest.of(page.numero(), page.taille(),
                Sort.by(Sort.Direction.DESC, "dateSaisie")));
        return new PageResultat<>(p.map(CorrespondanceActe::versDomaine).getContent(), page.numero(), page.taille(),
                p.getTotalElements());
    }

    @Override public long compter() { return jpa.count(); }
    @Override public long compterParStatut(StatutActe statut) { return jpa.countByStatut(statut); }
    @Override public long compterParType(TypeActe type) { return jpa.countByTypeActe(type); }
}
