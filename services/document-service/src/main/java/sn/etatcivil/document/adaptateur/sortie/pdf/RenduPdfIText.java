package sn.etatcivil.document.adaptateur.sortie.pdf;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sn.etatcivil.document.application.port.sortie.RenduPdf;
import sn.etatcivil.document.domaine.Complements;
import sn.etatcivil.document.domaine.ContenuActe;
import sn.etatcivil.document.domaine.DocumentDelivre;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Produit le PDF à partir du contenu figé lors de la signature : chaque information imprimée
 * provient du registre central, aucune valeur n'est inventée.
 */
@Component
class RenduPdfIText implements RenduPdf {

    private static final Color VERT_NUIT = new DeviceRgb(5, 58, 44);
    private static final Color OR = new DeviceRgb(184, 134, 43);
    private static final Color VERT = new DeviceRgb(14, 138, 102);
    private static final Color SABLE = new DeviceRgb(251, 239, 214);
    private static final Color TRAIT = new DeviceRgb(230, 220, 210);
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH)
            .withZone(ZoneId.of("Africa/Dakar"));

    private final String urlPublique;

    RenduPdfIText(@Value("${ecivil.url-publique:http://localhost:8080}") String urlPublique) {
        this.urlPublique = urlPublique.endsWith("/") ? urlPublique.substring(0, urlPublique.length() - 1) : urlPublique;
    }

    @Override
    public byte[] rendre(DocumentDelivre doc) {
        ContenuActe a = doc.contenu();
        Complements c = doc.complements();
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(sortie));
             Document page = new Document(pdf, PageSize.A4)) {
            pdf.getDocumentInfo().setTitle(doc.libelleDocument() + " " + a.numeroActe());
            page.setMargins(40, 50, 40, 50);
            entete(page, a);

            page.add(new Paragraph(doc.libelleDocument().toUpperCase(Locale.FRENCH)).setBold().setFontSize(15)
                    .setTextAlignment(TextAlignment.CENTER).setFontColor(VERT_NUIT));
            if (c.estCertificat()) {
                certificat(page, a, c);
            } else if (c.estLivret()) {
                livret(page, a, c);
            } else {
                acte(page, a, c);
            }

            page.add(signature(pdf, doc));
            page.add(new Paragraph("Document délivré au titre de la demande " + doc.reference()
                    + ". Son authenticité se vérifie sur " + urlPublique + "/verification"
                    + " en saisissant le code ci-dessus ou en scannant le QR code.")
                    .setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setMarginTop(14));
        }
        return sortie.toByteArray();
    }

    private static void entete(Document page, ContenuActe a) {
        page.add(new Paragraph("RÉPUBLIQUE DU SÉNÉGAL").setBold().setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER).setFontColor(VERT_NUIT).setMarginBottom(0));
        page.add(new Paragraph("Un Peuple – Un But – Une Foi").setItalic().setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(0));
        page.add(new Paragraph("ÉTAT CIVIL — " + a.centre().toUpperCase(Locale.FRENCH)
                + (a.commune() != null ? " (" + a.commune() + ")" : ""))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setFontColor(VERT));
        SolidLine trait = new SolidLine(1.5f);
        trait.setColor(OR);
        page.add(new LineSeparator(trait).setMarginBottom(12));
    }

    /** Extrait (avec ou sans filiation) ou copie intégrale d'un acte. */
    private static void acte(Document page, ContenuActe a, Complements c) {
        boolean integrale = Complements.COPIE_INTEGRALE.equals(c.modele());
        boolean filiation = !"EXTRAIT_SANS_FILIATION".equals(c.modele());
        page.add(new Paragraph("Acte n° " + a.numeroActe() + "  •  Année " + a.annee()
                + registrePapier(a)).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(14));
        if (a.origineParticuliere()) {
            page.add(encadre(("JUGEMENT_SUPPLETIF".equals(a.origine())
                    ? "Acte dressé en exécution du jugement supplétif : " : "Transcription de l'acte étranger : ")
                    + Objects.toString(a.referenceOrigine(), "")));
        }
        Table t = tableau();
        remplir(t, a, integrale, filiation);
        page.add(t);
        if (integrale) {
            ajouterBloc(page, "Mentions marginales", a.mentionsMarginales(), "Néant");
            if (a.observations() != null) ajouterBloc(page, "Observations", a.observations(), null);
        }
    }

    /** Certificat établi d'après l'acte de naissance et la situation de la personne dans le registre national. */
    private static void certificat(Document page, ContenuActe a, Complements c) {
        page.add(new Paragraph("L'officier de l'état civil soussigné certifie, au vu du registre national des actes, que :")
                .setFontSize(11).setMarginTop(10).setMarginBottom(8));
        Table t = tableau();
        ligne(t, "Nom", a.nom().toUpperCase(Locale.FRENCH));
        ligne(t, "Prénom(s)", a.prenom());
        ligne(t, "Sexe", sexe(a.sexe()));
        ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
        ligne(t, "Fils / fille de", filiation(a));
        ligne(t, "Acte de naissance", "n° " + a.numeroActe() + " — " + a.centre());
        page.add(t);
        page.add(encadre(c.constat()));
        if (c.motif() != null) {
            page.add(new Paragraph("Certificat délivré pour : " + c.motif()).setFontSize(11).setMarginTop(4));
        }
        page.add(new Paragraph("En foi de quoi le présent certificat est délivré pour servir et valoir ce que de droit.")
                .setItalic().setFontSize(10).setMarginTop(6));
    }

    /** Duplicata de livret de famille : l'acte de mariage puis les naissances des enfants du couple. */
    private static void livret(Document page, ContenuActe a, Complements c) {
        boolean titulaireEpouse = "F".equals(a.sexe());
        page.add(new Paragraph("Duplicata établi d'après les actes inscrits au registre national")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
        page.add(titre("Mariage"));
        Table t = tableau();
        String titulaire = Objects.toString(personne(a.prenom(), majuscules(a.nom())), "")
                + naissanceEntre(a.dateNaissance(), a.lieuNaissance());
        String conjoint = Objects.toString(personne(a.prenomConjoint(), majuscules(a.nomConjoint())), "")
                + naissanceEntre(a.dateNaissanceConjoint(), a.lieuNaissanceConjoint());
        ligne(t, "Époux", titulaireEpouse ? conjoint : titulaire);
        ligne(t, "Épouse", titulaireEpouse ? titulaire : conjoint);
        ligne(t, "Mariage célébré le", date(a.dateEvenement()));
        ligne(t, "À", a.lieuEvenement());
        ligne(t, "Acte de mariage", "n° " + a.numeroActe() + registrePapier(a));
        page.add(t);

        page.add(titre("Enfants"));
        if (c.enfants().isEmpty()) {
            page.add(new Paragraph("Aucune naissance d'enfant de ce couple n'est inscrite au registre national.")
                    .setFontSize(10).setItalic());
            return;
        }
        Table e = new Table(UnitValue.createPercentArray(new float[]{6, 34, 32, 28})).useAllAvailableWidth();
        for (String entete : new String[]{"", "Prénom(s) et nom", "Né(e) le, à", "Acte de naissance"}) {
            e.addHeaderCell(new Cell().add(new Paragraph(entete).setFontSize(9).setBold().setFontColor(VERT_NUIT))
                    .setBorder(null).setBorderBottom(new SolidBorder(OR, 1)));
        }
        int rang = 1;
        for (Complements.Enfant x : c.enfants()) {
            e.addCell(cellule(String.valueOf(rang++)));
            e.addCell(cellule(Objects.toString(personne(x.prenom(), majuscules(x.nom())), "")
                    + ("F".equals(x.sexe()) ? " (F)" : "M".equals(x.sexe()) ? " (M)" : "")));
            e.addCell(cellule(naissance(x.dateNaissance(), x.lieuNaissance())));
            e.addCell(cellule("n° " + x.numeroActe()));
        }
        page.add(e);
    }

    private static void remplir(Table t, ContenuActe a, boolean integrale, boolean avecFiliation) {
        String type = a.typeActe();
        switch (type) {
            case "NAISSANCE" -> {
                ligne(t, "Nom", majuscules(a.nom()));
                ligne(t, "Prénom(s)", a.prenom());
                ligne(t, "Sexe", sexe(a.sexe()));
                ligne(t, "Né(e) le", date(a.dateEvenement()));
                ligne(t, "À", a.lieuEvenement());
                if (avecFiliation) {
                    ligne(t, "Fils / fille de", personne(a.prenomPere(), a.nomPere()));
                    ligne(t, "Et de", personne(a.prenomMere(), a.nomMere()));
                }
            }
            case "RECONNAISSANCE" -> {
                ligne(t, "Enfant", personne(a.prenom(), majuscules(a.nom())));
                ligne(t, "Sexe", sexe(a.sexe()));
                ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
                ligne(t, "Père", personne(a.prenomPere(), a.nomPere()));
                ligne(t, "Mère", personne(a.prenomMere(), a.nomMere()));
                ligne(t, "Reconnaissance faite le", date(a.dateEvenement()));
                ligne(t, "À", a.lieuEvenement());
            }
            case "MARIAGE", "DIVORCE" -> {
                ligne(t, "MARIAGE".equals(type) ? "Mariage célébré le" : "Divorce prononcé le", date(a.dateEvenement()));
                ligne(t, "À", a.lieuEvenement());
                ligne(t, "Entre", personne(a.prenom(), majuscules(a.nom())));
                ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
                if (avecFiliation && (a.nomPere() != null || a.nomMere() != null)) {
                    ligne(t, "Fils / fille de", filiation(a));
                }
                ligne(t, "Et", personne(a.prenomConjoint(), majuscules(a.nomConjoint())));
                ligne(t, "Né(e) le", naissance(a.dateNaissanceConjoint(), a.lieuNaissanceConjoint()));
            }
            default -> {
                ligne(t, "Nom", majuscules(a.nom()));
                ligne(t, "Prénom(s)", a.prenom());
                ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
                if (avecFiliation) ligne(t, "Fils / fille de", filiation(a));
                ligne(t, "Décédé(e) le", date(a.dateEvenement()) + (a.heureDeces() != null ? " à " + a.heureDeces() : ""));
                ligne(t, "À", a.lieuEvenement());
                if (integrale) ligne(t, "Cause déclarée", a.causeDeces());
            }
        }
        if (integrale) {
            ligne(t, "Premier témoin", a.temoin1());
            ligne(t, "Second témoin", a.temoin2());
        }
    }

    private Div signature(PdfDocument pdf, DocumentDelivre doc) {
        String lien = urlPublique + "/verification/" + doc.codeVerification();
        BarcodeQRCode qr = new BarcodeQRCode(lien);
        PdfFormXObject image = qr.createFormXObject(ColorConstants.BLACK, pdf);
        Image code = new Image(image).setWidth(95).setHeight(95);

        Table t = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        Cell texte = new Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph("Délivré le " + HORODATAGE.format(doc.dateSignature())).setFontSize(10))
                .add(new Paragraph("L'Officier de l'état civil : " + doc.signataire()).setBold().setFontSize(10))
                .add(new Paragraph("Signé électroniquement").setFontSize(9).setFontColor(VERT))
                .add(new Paragraph("Code de vérification : " + doc.codeVerification()).setBold().setFontSize(11)
                        .setFontColor(VERT_NUIT))
                .add(new Paragraph("Empreinte SHA-256 : " + doc.empreinte()).setFontSize(6.5f)
                        .setFontColor(ColorConstants.GRAY));
        t.addCell(texte);
        t.addCell(new Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE).add(code).setTextAlignment(TextAlignment.RIGHT));
        Div bloc = new Div().setBorder(new SolidBorder(VERT_NUIT, 1)).setPadding(12).setMarginTop(24);
        bloc.add(t);
        return bloc;
    }

    private static Table tableau() {
        return new Table(UnitValue.createPercentArray(new float[]{38, 62})).useAllAvailableWidth();
    }

    private static Paragraph titre(String texte) {
        return new Paragraph(texte.toUpperCase(Locale.FRENCH)).setBold().setFontSize(10).setFontColor(VERT_NUIT)
                .setMarginTop(14).setMarginBottom(4);
    }

    private static Div encadre(String texte) {
        Div d = new Div().setBorderLeft(new SolidBorder(OR, 3)).setBackgroundColor(SABLE)
                .setPadding(8).setMarginTop(10).setMarginBottom(6);
        d.add(new Paragraph(Objects.toString(texte, "")).setFontSize(10.5f).setBold().setFontColor(VERT_NUIT));
        return d;
    }

    private static Cell cellule(String texte) {
        return new Cell().add(new Paragraph(Objects.toString(texte, "")).setFontSize(10)).setBorder(null)
                .setBorderBottom(new SolidBorder(TRAIT, 0.5f));
    }

    private static void ligne(Table t, String libelle, String valeur) {
        if (valeur == null || valeur.isBlank()) return;
        t.addCell(new Cell().add(new Paragraph(libelle).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(null).setBorderBottom(new SolidBorder(TRAIT, 0.5f)));
        t.addCell(new Cell().add(new Paragraph(valeur).setFontSize(11).setBold())
                .setBorder(null).setBorderBottom(new SolidBorder(TRAIT, 0.5f)));
    }

    private static void ajouterBloc(Document page, String titre, String texte, String siVide) {
        String contenu = texte == null || texte.isBlank() ? siVide : texte;
        if (contenu == null) return;
        page.add(new Paragraph(titre).setBold().setFontSize(10).setFontColor(VERT_NUIT).setMarginTop(12).setMarginBottom(2));
        page.add(new Paragraph(contenu).setFontSize(10).setBorderLeft(new SolidBorder(OR, 2)).setPaddingLeft(8));
    }

    private static String registrePapier(ContenuActe a) {
        StringBuilder s = new StringBuilder();
        if (a.volume() != null) s.append("  •  Volume ").append(a.volume());
        if (a.folio() != null) s.append("  •  Folio ").append(a.folio());
        return s.toString();
    }

    private static String filiation(ContenuActe a) {
        String pere = personne(a.prenomPere(), a.nomPere());
        String mere = personne(a.prenomMere(), a.nomMere());
        if (pere == null) return mere;
        return mere == null ? pere : pere + " et de " + mere;
    }

    private static String sexe(String s) {
        return "M".equals(s) ? "Masculin" : "F".equals(s) ? "Féminin" : null;
    }

    private static String majuscules(String s) {
        return s == null ? null : s.toUpperCase(Locale.FRENCH);
    }

    private static String naissance(LocalDate date, String lieu) {
        if (date == null && lieu == null) return null;
        return (date != null ? date(date) : "") + (lieu != null ? (date != null ? " à " : "à ") + lieu : "");
    }

    private static String naissanceEntre(LocalDate date, String lieu) {
        String n = naissance(date, lieu);
        return n == null ? "" : ", né(e) le " + n;
    }

    private static String personne(String prenom, String nom) {
        if (prenom == null && nom == null) return null;
        return ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
    }

    private static String date(LocalDate d) {
        return d == null ? null : d.format(JOUR);
    }
}
