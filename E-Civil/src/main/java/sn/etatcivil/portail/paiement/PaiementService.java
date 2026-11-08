package sn.etatcivil.portail.paiement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.demande.Demande;
import sn.etatcivil.portail.demande.DemandeService;
import sn.etatcivil.portail.demande.StatutDemande;
import sn.etatcivil.portail.securite.UtilisateurConnecte;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/**
 * Une demande n'est marquée payée que lorsque l'API PayDunya confirme la facture
 * (statut « completed » et montant exact). Ni le retour navigateur ni la notification IPN
 * ne suffisent seuls : tous deux déclenchent une confirmation auprès de PayDunya.
 */
@Service
public class PaiementService {

    private static final Logger log = LoggerFactory.getLogger(PaiementService.class);

    private final PaiementRepository paiements;
    private final DemandeService demandes;
    private final PayDunyaClient paydunya;
    private final ProprietesPortail proprietes;

    public PaiementService(PaiementRepository paiements, DemandeService demandes,
                           PayDunyaClient paydunya, ProprietesPortail proprietes) {
        this.paiements = paiements;
        this.demandes = demandes;
        this.paydunya = paydunya;
        this.proprietes = proprietes;
    }

    /** Renvoie l'adresse de la page de paiement PayDunya vers laquelle rediriger le citoyen. */
    @Transactional
    public String initier(Long demandeId, UtilisateurConnecte citoyen) {
        Demande d = demandes.demandeDe(demandeId, citoyen);
        if (!d.appartientA(citoyen.getId())) throw new ActionRefuseeException("Demande introuvable");
        if (d.getStatut() != StatutDemande.A_PAYER) {
            throw new ActionRefuseeException("Cette demande n'est pas en attente de paiement");
        }
        Optional<Paiement> encours = paiements.findFirstByDemandeIdAndStatut(d.getId(), StatutPaiement.INITIE)
                .filter(p -> p.getDateCreation().isAfter(LocalDateTime.now().minusHours(1)));
        if (encours.isPresent()) return encours.get().getUrlPaiement();

        String retour = proprietes.lien("/paiements/retour");
        var facture = paydunya.creerFacture(d.getMontant(),
                d.getTypeDocument().getLibelle() + " - demande " + d.getReference(), d.getReference(),
                retour, proprietes.lien("/paiements/annulation?demande=" + d.getId()),
                proprietes.lien("/paiements/paydunya/ipn"));
        paiements.save(new Paiement(d.getId(), facture.token(), d.getMontant(), facture.urlPaiement()));
        return facture.urlPaiement();
    }

    /** Interroge PayDunya et met à jour le paiement et la demande. Sans effet si déjà confirmé. */
    @Transactional
    public Paiement confirmer(String token) {
        Paiement p = paiements.findByToken(token)
                .orElseThrow(() -> new ActionRefuseeException("Paiement inconnu"));
        if (p.getStatut() != StatutPaiement.INITIE) return p;

        var etat = paydunya.confirmer(token);
        if (etat.estPayee()) {
            if (etat.montant().compareTo(BigDecimal.valueOf(p.getMontant())) != 0) {
                log.error("Montant PayDunya {} différent du montant attendu {} (token {})",
                        etat.montant(), p.getMontant(), token);
                throw new ActionRefuseeException("Le montant payé ne correspond pas au montant dû. "
                        + "Contactez l'administration avec la référence de votre demande.");
            }
            p.confirmer(etat.urlRecu());
            demandes.enregistrerPaiement(p.getDemandeId(), "Paiement PayDunya confirmé (facture " + token + ")");
        } else if (etat.estAnnulee()) {
            p.annuler();
        }
        return p;
    }

    /**
     * Notification instantanée (IPN) de PayDunya. On vérifie que l'empreinte envoyée est bien
     * le SHA-512 de notre clé principale, puis on confirme auprès de l'API.
     */
    public void traiterNotification(String hash, String token) {
        if (hash == null || token == null) throw new ActionRefuseeException("Notification incomplète");
        if (!paydunya.estConfigure()) throw new ActionRefuseeException("Paiement non configuré");
        byte[] attendu = sha512(paydunya.masterKey()).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(attendu, hash.toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            log.warn("Notification PayDunya rejetée : empreinte invalide (token {})", token);
            throw new ActionRefuseeException("Notification non authentifiée");
        }
        confirmer(token);
    }

    @Transactional(readOnly = true)
    public List<Paiement> deLaDemande(Long demandeId) {
        return paiements.findByDemandeIdOrderByDateCreationDesc(demandeId);
    }

    @Transactional(readOnly = true)
    public Optional<Paiement> parToken(String token) {
        return paiements.findByToken(token);
    }

    public boolean estDisponible() {
        return paydunya.estConfigure();
    }

    static String sha512(String texte) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512")
                    .digest(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
