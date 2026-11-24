package sn.etatcivil.document.application.service;

import java.time.LocalDate;
import java.time.ZoneId;
import sn.etatcivil.document.domaine.Constats;
import sn.etatcivil.document.domaine.RegleMetierException;

import sn.etatcivil.document.application.CasDUsage;
import sn.etatcivil.document.application.port.entree.DelivrerDocuments;
import sn.etatcivil.document.application.port.sortie.DocumentRepository;
import sn.etatcivil.document.application.port.sortie.Horloge;
import sn.etatcivil.document.application.port.sortie.RegistreActes;
import sn.etatcivil.document.application.port.sortie.RenduPdf;
import sn.etatcivil.document.domaine.Complements;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;
import sn.etatcivil.document.domaine.DocumentIntrouvableException;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@CasDUsage
public class ServiceDocuments implements DelivrerDocuments {

    private final DocumentRepository documents;
    private final RegistreActes registre;
    private final RenduPdf rendu;
    private final Horloge horloge;
    private final SecureRandom aleatoire = new SecureRandom();

    public ServiceDocuments(DocumentRepository documents, RegistreActes registre, RenduPdf rendu, Horloge horloge) {
        this.documents = documents;
        this.registre = registre;
        this.rendu = rendu;
        this.horloge = horloge;
    }

    @Override
    public DocumentDelivre delivrer(DocumentADelivrer c) {
        Optional<DocumentDelivre> existant = documents.parDemande(c.demandeId());
        if (existant.isPresent()) return existant.get();
        String modele = c.modele() == null ? "EXTRAIT" : c.modele();
        // relu au moment même de la signature ; un document établi sur comparution ne s'appuie sur aucun acte
        ContenuActe contenu = c.acteId() == null ? null : registre.contenu(c.acteId());
        List<Complements.Enfant> enfants = contenu != null && "LIVRET_FAMILLE".equals(modele) && "MARIAGE".equals(contenu.typeActe())
                ? registre.enfants(contenu) : List.of();
        String mention = null;
        if ("EXTRAIT_MENTION".equals(modele)) {
            List<String> mentions = contenu == null || c.typeMention() == null ? List.of() : registre.mentions(c.acteId(), c.typeMention());
            if (mentions.isEmpty()) throw new RegleMetierException("La mention demandée ne figure pas en marge de l'acte");
            mention = String.join(" ; ", mentions);
        }
        String constat = c.constat() != null ? c.constat() : Constats.pour(modele, contenu, c.declaration(), mention);
        LocalDate valableJusquAu = c.dureeValiditeJours() == null ? null
                : LocalDate.ofInstant(horloge.maintenant(), ZoneId.of("Africa/Dakar")).plusDays(c.dureeValiditeJours());
        Complements complements = new Complements(modele, c.motif(), constat, enfants, valableJusquAu, c.declaration(), mention);
        String code;
        do {
            code = DocumentDelivre.nouveauCode(aleatoire);
        } while (documents.parCode(code).isPresent());
        DocumentDelivre d = DocumentDelivre.delivrer(c.demandeId(), c.reference(), c.demandeurId(), c.libelleDocument(),
                c.format(), contenu, complements, c.signataire(), code, horloge.maintenant());
        documents.enregistrer(d);
        return d;
    }

    @Override
    public byte[] pdf(UUID demandeId, String utilisateurId, boolean estAgent) {
        DocumentDelivre d = documents.parDemande(demandeId)
                .filter(x -> x.telechargeablePar(utilisateurId, estAgent))
                .orElseThrow(DocumentIntrouvableException::new);
        if (!d.integre()) throw new IllegalStateException("Empreinte invalide pour le document " + d.reference());
        return rendu.rendre(d);
    }

    @Override
    public Optional<DocumentDelivre> verifier(String code) {
        return documents.parCode(code.trim().toUpperCase(Locale.ROOT)).filter(DocumentDelivre::integre);
    }
}
