package sn.etatcivil.portail.demande;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.portail.catalogue.*;
import sn.etatcivil.portail.document.DocumentSigne;
import sn.etatcivil.portail.registre.*;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.utilisateur.UtilisateurRepository;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static sn.etatcivil.portail.demande.StatutDemande.*;

/** Toutes les règles du traitement d'une demande, de la soumission à la délivrance. */
@Service
@Transactional
public class DemandeService {

    public static final String SYSTEME = "Système E-Civil";
    private static final Logger log = LoggerFactory.getLogger(DemandeService.class);
    private static final char[] ALPHABET_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final DemandeRepository demandes;
    private final HistoriqueStatutRepository historique;
    private final TypeDocumentRepository typesDocument;
    private final MotifRepository motifs;
    private final UtilisateurRepository utilisateurs;
    private final GenerateurReference references;
    private final RegistreClient registre;
    private final ApplicationEventPublisher evenements;
    private final ObjectMapper json;
    private final SecureRandom aleatoire = new SecureRandom();

    public DemandeService(DemandeRepository demandes, HistoriqueStatutRepository historique,
                          TypeDocumentRepository typesDocument, MotifRepository motifs,
                          UtilisateurRepository utilisateurs, GenerateurReference references,
                          RegistreClient registre, ApplicationEventPublisher evenements, ObjectMapper json) {
        this.demandes = demandes;
        this.historique = historique;
        this.typesDocument = typesDocument;
        this.motifs = motifs;
        this.utilisateurs = utilisateurs;
        this.references = references;
        this.registre = registre;
        this.evenements = evenements;
        this.json = json;
    }

    // ------------------------------------------------------------------ citoyen

    public Demande creer(FormulaireDemande f, UtilisateurConnecte citoyen) {
        TypeDocument type = typesDocument.findById(f.getTypeDocumentId())
                .filter(TypeDocument::isActif)
                .orElseThrow(() -> new ActionRefuseeException("Ce document n'est pas disponible"));
        Motif motif = motifs.findById(f.getMotifId())
                .orElseThrow(() -> new ActionRefuseeException("Motif inconnu"));
        verifierFormulaire(f, type, motif);

        Demande d = new Demande();
        d.setReference(references.suivante());
        d.setDemandeur(utilisateurs.getReferenceById(citoyen.getId()));
        d.setTypeDocument(type);
        d.setMontant(type.getTarif());
        d.setMotif(motif);
        d.setMotifPrecision(texte(f.getMotifPrecision()));
        d.setLienDemandeur(f.getLienDemandeur());
        d.setFormat(f.getFormat());
        if (f.getFormat() == FormatDocument.PHYSIQUE) {
            d.setAdresseLivraison(texte(f.getAdresseLivraison()));
            d.setVilleLivraison(texte(f.getVilleLivraison()));
        }
        d.setNom(f.getNom().trim());
        d.setPrenom(f.getPrenom().trim());
        d.setSexe(texte(f.getSexe()));
        d.setDateNaissance(f.getDateNaissance());
        d.setLieuNaissance(texte(f.getLieuNaissance()));
        d.setNomPere(texte(f.getNomPere()));
        d.setPrenomPere(texte(f.getPrenomPere()));
        d.setNomMere(texte(f.getNomMere()));
        d.setPrenomMere(texte(f.getPrenomMere()));
        if (type.getTypeActe().concerneUnCouple()) {
            d.setNomConjoint(texte(f.getNomConjoint()));
            d.setPrenomConjoint(texte(f.getPrenomConjoint()));
        }
        d.setDateEvenement(type.getTypeActe() == TypeActe.NAISSANCE ? null : f.getDateEvenement());
        demandes.save(d);
        tracer(d, null, SOUMISE, citoyen.signature(), null);
        return d;
    }

    public void annuler(Long id, UtilisateurConnecte citoyen) {
        Demande d = demandeDe(id, citoyen);
        changer(d, ANNULEE, citoyen.signature(), null);
    }

    @Transactional(readOnly = true)
    public List<Demande> demandesDe(UtilisateurConnecte citoyen) {
        return demandes.findByDemandeurIdOrderByDateCreationDesc(citoyen.getId());
    }

    /** Un citoyen ne voit que ses propres demandes ; un agent les voit toutes. */
    @Transactional(readOnly = true)
    public Demande demandeDe(Long id, UtilisateurConnecte qui) {
        Demande d = trouver(id);
        if (!qui.getRole().estAgent() && !d.appartientA(qui.getId())) {
            throw new ActionRefuseeException("Demande introuvable");
        }
        return d;
    }

    // ------------------------------------------------------------------ délégué

    public void validerPourPaiement(Long id, UtilisateurConnecte delegue) {
        changer(trouver(id), A_PAYER, delegue.signature(), null);
    }

    public void rejeter(Long id, String motif, UtilisateurConnecte agent) {
        if (motif == null || motif.isBlank()) throw new ActionRefuseeException("Indiquez le motif du rejet");
        Demande d = trouver(id);
        d.setMotifRejet(motif.trim());
        changer(d, REJETEE, agent.signature(), motif.trim());
    }

    public void marquerLivree(Long id, UtilisateurConnecte delegue) {
        Demande d = trouver(id);
        if (d.getFormat() != FormatDocument.PHYSIQUE) {
            throw new ActionRefuseeException("Un document numérique est délivré par téléchargement");
        }
        changer(d, LIVREE, delegue.signature(), null);
    }

    // ------------------------------------------------------------------ paiement

    /** Appelé uniquement quand PayDunya a confirmé le paiement (jamais sur simple déclaration). */
    public void enregistrerPaiement(Long id, String detail) {
        Demande d = trouver(id);
        if (d.getStatut() == PAYEE || !d.getStatut().peutPasserA(PAYEE)) {
            log.warn("Paiement confirmé pour {} alors que la demande est {}", d.getReference(), d.getStatut());
            return;
        }
        changer(d, PAYEE, SYSTEME, detail);
    }

    // ------------------------------------------------------------------ registre

    /**
     * Interroge le registre central. Si celui-ci est injoignable, la demande reste « Payée »
     * et la recherche est retentée automatiquement plus tard.
     */
    public void rechercherDansRegistre(Long id, String auteur) {
        Demande d = trouver(id);
        if (d.getStatut() != PAYEE && d.getStatut() != ACTE_INTROUVABLE) {
            throw new ActionRefuseeException("Cette demande n'est pas en attente de recherche");
        }
        ResultatRecherche r = registre.rechercher(criteres(d));
        switch (r.resultat()) {
            case TROUVE -> rattacher(d, r.actes().get(0), auteur, "Acte retrouvé automatiquement dans le registre");
            case AMBIGU -> {
                String numeros = r.actes().stream().map(ActeRegistre::numeroActe).collect(Collectors.joining(", "));
                d.setNoteRecherche("Plusieurs actes correspondent : " + numeros + ". Un agent doit choisir le bon.");
                passerIntrouvable(d, auteur);
            }
            case NON_TROUVE -> {
                d.setNoteRecherche("Aucun acte validé ne correspond dans le registre central.");
                passerIntrouvable(d, auteur);
            }
        }
    }

    /** L'agent indique lui-même le numéro de l'acte (après transcription ou en cas d'homonymes). */
    public void rattacherParNumero(Long id, String numeroActe, UtilisateurConnecte agent) {
        if (numeroActe == null || numeroActe.isBlank()) throw new ActionRefuseeException("Indiquez le numéro de l'acte");
        Demande d = trouver(id);
        if (d.getStatut() != ACTE_INTROUVABLE) {
            throw new ActionRefuseeException("Seule une demande dont l'acte n'a pas été retrouvé peut être rattachée");
        }
        ActeRegistre acte = registre.parNumero(numeroActe);
        rattacher(d, acte, agent.signature(), "Acte " + acte.numeroActe() + " rattaché manuellement");
    }

    // ------------------------------------------------------------------ officier

    /** L'officier constate que l'acte rattaché n'est pas le bon : la demande retourne au registre. */
    public void contesterRattachement(Long id, String raison, UtilisateurConnecte officier) {
        if (raison == null || raison.isBlank()) throw new ActionRefuseeException("Expliquez pourquoi l'acte ne convient pas");
        Demande d = trouver(id);
        if (d.getStatut() != ACTE_TROUVE) throw new ActionRefuseeException("Aucun acte n'est rattaché à cette demande");
        d.setNoteRecherche("Rattachement contesté par l'officier : " + raison.trim());
        d.setActeCentralId(null);
        d.setNumeroActe(null);
        changer(d, ACTE_INTROUVABLE, officier.signature(), raison.trim());
    }

    /**
     * Signature : l'acte est relu dans le registre au moment même de signer, figé en JSON,
     * et protégé par une empreinte SHA-256 et un code de vérification unique.
     */
    public void signer(Long id, UtilisateurConnecte officier) {
        Demande d = trouver(id);
        if (d.getStatut() != ACTE_TROUVE) throw new ActionRefuseeException("Cette demande n'est pas prête à être signée");
        ActeRegistre acte = registre.obtenir(d.getActeCentralId());
        verifierActe(d, acte);

        LocalDateTime maintenant = LocalDateTime.now();
        String code = nouveauCode();
        DocumentSigne contenu = new DocumentSigne(d.getReference(), d.getTypeDocument().getLibelle(),
                d.getTypeDocument().isCopieIntegrale(), acte, officier.getNomComplet(), maintenant, code);
        String contenuJson = enJson(contenu);

        d.setSignataire(utilisateurs.getReferenceById(officier.getId()));
        d.setDateSignature(maintenant);
        d.setCodeVerification(code);
        d.setContenuSigne(contenuJson);
        d.setEmpreinte(sha256(contenuJson));
        changer(d, SIGNEE, officier.signature(), "Code de vérification " + code);
    }

    // ------------------------------------------------------------------ consultation

    @Transactional(readOnly = true)
    public Page<Demande> parStatuts(List<StatutDemande> statuts, Pageable page) {
        return demandes.findByStatutInOrderByDateCreationAsc(statuts, page);
    }

    @Transactional(readOnly = true)
    public Page<Demande> rechercher(StatutDemande statut, String reference, Pageable page) {
        Specification<Demande> filtre = (racine, requete, cb) -> cb.conjunction();
        if (statut != null) filtre = filtre.and((d, q, cb) -> cb.equal(d.get("statut"), statut));
        if (reference != null && !reference.isBlank()) {
            String motif = "%" + reference.trim().toUpperCase() + "%";
            filtre = filtre.and((d, q, cb) -> cb.like(d.get("reference"), motif));
        }
        Pageable tri = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by(Sort.Direction.DESC, "dateCreation"));
        return demandes.findAll(filtre, tri);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueStatut> historique(Long demandeId) {
        return historique.findByDemandeIdOrderByDateChangementAsc(demandeId);
    }

    @Transactional(readOnly = true)
    public List<Long> enAttenteDeRecherche() {
        return demandes.findTop50ByStatutOrderByDateModificationAsc(PAYEE).stream().map(Demande::getId).toList();
    }

    @Transactional(readOnly = true)
    public long nombre(StatutDemande statut) {
        return demandes.countByStatut(statut);
    }

    @Transactional(readOnly = true)
    public Demande trouver(Long id) {
        return demandes.findById(id).orElseThrow(() -> new ActionRefuseeException("Demande introuvable"));
    }

    // ------------------------------------------------------------------ interne

    private void rattacher(Demande d, ActeRegistre acte, String auteur, String commentaire) {
        verifierActe(d, acte);
        d.setActeCentralId(acte.id());
        d.setNumeroActe(acte.numeroActe());
        d.setNoteRecherche(null);
        changer(d, ACTE_TROUVE, auteur, commentaire);
    }

    private void passerIntrouvable(Demande d, String auteur) {
        if (d.getStatut() == ACTE_INTROUVABLE) {
            tracer(d, ACTE_INTROUVABLE, ACTE_INTROUVABLE, auteur, d.getNoteRecherche()); // nouvelle tentative
        } else {
            changer(d, ACTE_INTROUVABLE, auteur, d.getNoteRecherche());
        }
    }

    private void verifierActe(Demande d, ActeRegistre acte) {
        if (acte.typeActe() != d.getTypeDocument().getTypeActe()) {
            throw new ActionRefuseeException("L'acte " + acte.numeroActe() + " est un acte de "
                    + acte.typeActe().getLibelle().toLowerCase() + ", pas de "
                    + d.getTypeDocument().getTypeActe().getLibelle().toLowerCase());
        }
        if (!acte.estValide()) {
            throw new ActionRefuseeException("L'acte " + acte.numeroActe() + " n'est pas encore validé par un officier");
        }
    }

    private void changer(Demande d, StatutDemande nouveau, String auteur, String commentaire) {
        StatutDemande ancien = d.getStatut();
        if (!ancien.peutPasserA(nouveau)) {
            throw new ActionRefuseeException("Impossible de passer une demande « " + ancien.getLibelle()
                    + " » à « " + nouveau.getLibelle() + " »");
        }
        d.setStatut(nouveau);
        tracer(d, ancien, nouveau, auteur, commentaire);
    }

    private void tracer(Demande d, StatutDemande ancien, StatutDemande nouveau, String auteur, String commentaire) {
        historique.save(new HistoriqueStatut(d.getId(), ancien, nouveau, auteur, commentaire));
        if (ancien != nouveau) evenements.publishEvent(new ChangementStatut(d.getId(), ancien, nouveau));
    }

    private void verifierFormulaire(FormulaireDemande f, TypeDocument type, Motif motif) {
        if (motif.estAutre() && texte(f.getMotifPrecision()) == null) {
            throw new ActionRefuseeException("Précisez le motif de votre demande");
        }
        if (f.getFormat() == FormatDocument.PHYSIQUE
                && (texte(f.getAdresseLivraison()) == null || texte(f.getVilleLivraison()) == null)) {
            throw new ActionRefuseeException("L'adresse et la ville de livraison sont obligatoires pour un document papier");
        }
        if (f.getDateNaissance() != null && f.getDateNaissance().isBefore(LocalDate.now().minusYears(130))) {
            throw new ActionRefuseeException("La date de naissance semble erronée");
        }
        switch (type.getTypeActe()) {
            case NAISSANCE -> {
                if (f.getDateNaissance() == null || texte(f.getLieuNaissance()) == null) {
                    throw new ActionRefuseeException("La date et le lieu de naissance sont obligatoires");
                }
            }
            case MARIAGE, DIVORCE -> {
                if (texte(f.getNomConjoint()) == null || texte(f.getPrenomConjoint()) == null) {
                    throw new ActionRefuseeException("Le nom et le prénom du conjoint sont obligatoires");
                }
            }
            case DECES -> {
                if (f.getDateEvenement() == null) {
                    throw new ActionRefuseeException("La date du décès est obligatoire");
                }
            }
        }
    }

    private static CriteresRecherche criteres(Demande d) {
        return new CriteresRecherche(d.getTypeDocument().getTypeActe(), d.getNom(), d.getPrenom(),
                d.getDateNaissance(), d.getLieuNaissance(), d.getNomPere(), d.getPrenomPere(),
                d.getNomMere(), d.getPrenomMere(), d.getNomConjoint(), d.getPrenomConjoint(),
                d.getDateEvenement());
    }

    private String nouveauCode() {
        String code;
        do {
            StringBuilder s = new StringBuilder(10);
            for (int i = 0; i < 10; i++) s.append(ALPHABET_CODE[aleatoire.nextInt(ALPHABET_CODE.length)]);
            code = s.substring(0, 5) + "-" + s.substring(5);
        } while (demandes.findByCodeVerification(code).isPresent());
        return code;
    }

    private String enJson(DocumentSigne contenu) {
        try {
            return json.writeValueAsString(contenu);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossible de figer le contenu de l'acte", e);
        }
    }

    static String sha256(String texte) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(texte.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String texte(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
