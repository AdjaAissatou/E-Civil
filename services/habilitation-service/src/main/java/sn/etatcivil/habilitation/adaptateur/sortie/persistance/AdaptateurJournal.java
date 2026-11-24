package sn.etatcivil.habilitation.adaptateur.sortie.persistance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.habilitation.application.port.entree.PageDemandee;
import sn.etatcivil.habilitation.application.port.entree.PageResultat;
import sn.etatcivil.habilitation.application.port.sortie.JournalOperations;
import sn.etatcivil.habilitation.application.port.sortie.MarqueurInitialisation;
import sn.etatcivil.habilitation.domaine.Operation;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
class AdaptateurJournal implements JournalOperations, MarqueurInitialisation {

    private final OperationsJpa operations;
    private final MarqueursJpa marqueurs;

    AdaptateurJournal(OperationsJpa operations, MarqueursJpa marqueurs) {
        this.operations = operations;
        this.marqueurs = marqueurs;
    }

    @Override
    @Transactional
    public void enregistrer(Operation o) {
        OperationEntite e = new OperationEntite();
        e.date = o.date();
        e.auteur = tronquer(o.auteur(), 250);
        e.type = o.type();
        e.cible = tronquer(o.cible(), 250);
        e.detail = tronquer(o.detail(), 1000);
        operations.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultat<Operation> lire(String recherche, PageDemandee page) {
        PageRequest p = PageRequest.of(page.numero(), page.taille());
        Page<OperationEntite> r = recherche == null
                ? operations.findAllByOrderByDateDescIdDesc(p)
                : operations.rechercher("%" + recherche.toLowerCase(Locale.ROOT) + "%", p);
        return new PageResultat<>(r.getContent().stream().map(AdaptateurJournal::versDomaine).toList(),
                page.numero(), page.taille(), r.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operation> concernant(String cible, int nombre) {
        return operations.findTop20ByCibleOrderByDateDescIdDesc(cible).stream().limit(nombre)
                .map(AdaptateurJournal::versDomaine).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estFaite(String etape) {
        return marqueurs.existsById(etape);
    }

    @Override
    @Transactional
    public void marquer(String etape) {
        MarqueurEntite m = new MarqueurEntite();
        m.etape = etape;
        m.date = Instant.now();
        marqueurs.save(m);
    }

    private static Operation versDomaine(OperationEntite e) {
        return new Operation(e.date, e.auteur, e.type, e.cible, e.detail);
    }

    private static String tronquer(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
