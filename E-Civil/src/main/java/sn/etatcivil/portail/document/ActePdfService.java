package sn.etatcivil.portail.document;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;
import sn.etatcivil.portail.catalogue.TypeActe;
import sn.etatcivil.portail.config.ProprietesPortail;
import sn.etatcivil.portail.demande.Demande;
import sn.etatcivil.portail.registre.ActeRegistre;
import sn.etatcivil.portail.web.ActionRefuseeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Produit le PDF à partir du contenu figé lors de la signature : chaque information imprimée
 * provient du registre central, aucune valeur n'est inventée.
 */
@Service
public class ActePdfService {

    private static final Color BRUN = new DeviceRgb(92, 61, 46);
    private static final Color OR = new DeviceRgb(205, 153, 0);
    private static final Color VERT = new DeviceRgb(0, 133, 63);
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter HORODATAGE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm", Locale.FRENCH);

    private final ObjectMapper json;
    private final ProprietesPortail proprietes;

    public ActePdfService(ObjectMapper json, ProprietesPortail proprietes) {
        this.json = json;
        this.proprietes = proprietes;
    }

    public DocumentSigne contenu(Demande d) {
        if (d.getContenuSigne() == null) throw new ActionRefuseeException("Ce document n'a pas encore été signé");
        try {
            return json.readValue(d.getContenuSigne(), DocumentSigne.class);
        } catch (IOException e) {
            throw new IllegalStateException("Contenu signé illisible pour " + d.getReference(), e);
        }
    }

    public byte[] generer(Demande d) {
        DocumentSigne doc = contenu(d);
        ActeRegistre a = doc.acte();
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(sortie));
             Document page = new Document(pdf, PageSize.A4)) {
            pdf.getDocumentInfo().setTitle(doc.libelleDocument() + " " + a.numeroActe());
            page.setMargins(40, 50, 40, 50);

            page.add(new Paragraph("RÉPUBLIQUE DU SÉNÉGAL").setBold().setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER).setFontColor(BRUN).setMarginBottom(0));
            page.add(new Paragraph("Un Peuple – Un But – Une Foi").setItalic().setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(0));
            page.add(new Paragraph("ÉTAT CIVIL — " + a.centre().toUpperCase(Locale.FRENCH)
                    + (a.commune() != null ? " (" + a.commune() + ")" : ""))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setFontColor(VERT));
            SolidLine trait = new SolidLine(1.5f);
            trait.setColor(OR);
            page.add(new LineSeparator(trait).setMarginBottom(12));

            page.add(new Paragraph(doc.libelleDocument().toUpperCase(Locale.FRENCH)).setBold().setFontSize(15)
                    .setTextAlignment(TextAlignment.CENTER).setFontColor(BRUN));
            page.add(new Paragraph("Acte n° " + a.numeroActe() + "  •  Année " + a.annee()
                    + registrePapier(a)).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(14));

            Table t = new Table(UnitValue.createPercentArray(new float[]{38, 62})).useAllAvailableWidth();
            remplir(t, a, doc.copieIntegrale());
            page.add(t);

            if (doc.copieIntegrale()) {
                ajouterBloc(page, "Mentions marginales", a.mentionsMarginales(), "Néant");
                if (a.observations() != null) ajouterBloc(page, "Observations", a.observations(), null);
            }

            page.add(signature(pdf, doc, d));
            page.add(new Paragraph("Document délivré au titre de la demande " + doc.reference()
                    + ". Son authenticité se vérifie en ligne en saisissant le code ci-dessus ou en scannant le QR code.")
                    .setFontSize(8).setFontColor(ColorConstants.DARK_GRAY).setMarginTop(14));
        }
        return sortie.toByteArray();
    }

    private void remplir(Table t, ActeRegistre a, boolean integrale) {
        TypeActe type = a.typeActe();
        switch (type) {
            case NAISSANCE -> {
                ligne(t, "Nom", a.nom().toUpperCase(Locale.FRENCH));
                ligne(t, "Prénom(s)", a.prenom());
                ligne(t, "Sexe", "M".equals(a.sexe()) ? "Masculin" : "F".equals(a.sexe()) ? "Féminin" : null);
                ligne(t, "Né(e) le", date(a.dateEvenement()));
                ligne(t, "À", a.lieuEvenement());
                ligne(t, "Fils / fille de", personne(a.prenomPere(), a.nomPere()));
                ligne(t, "Et de", personne(a.prenomMere(), a.nomMere()));
            }
            case MARIAGE, DIVORCE -> {
                ligne(t, type == TypeActe.MARIAGE ? "Mariage célébré le" : "Divorce prononcé le", date(a.dateEvenement()));
                ligne(t, "À", a.lieuEvenement());
                ligne(t, "Entre", personne(a.prenom(), a.nom().toUpperCase(Locale.FRENCH)));
                ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
                if (a.nomPere() != null || a.nomMere() != null) {
                    ligne(t, "Fils / fille de", filiation(a));
                }
                ligne(t, "Et", personne(a.prenomConjoint(), a.nomConjoint() == null ? null : a.nomConjoint().toUpperCase(Locale.FRENCH)));
                ligne(t, "Né(e) le", naissance(a.dateNaissanceConjoint(), a.lieuNaissanceConjoint()));
            }
            case DECES -> {
                ligne(t, "Nom", a.nom().toUpperCase(Locale.FRENCH));
                ligne(t, "Prénom(s)", a.prenom());
                ligne(t, "Né(e) le", naissance(a.dateNaissance(), a.lieuNaissance()));
                ligne(t, "Fils / fille de", filiation(a));
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

    private Div signature(PdfDocument pdf, DocumentSigne doc, Demande d) {
        String lien = proprietes.lien("/verification/" + doc.codeVerification());
        BarcodeQRCode qr = new BarcodeQRCode(lien);
        PdfFormXObject image = qr.createFormXObject(ColorConstants.BLACK, pdf);
        Image code = new Image(image).setWidth(95).setHeight(95);

        Table t = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth().setMarginTop(24);
        Cell texte = new Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .add(new Paragraph("Délivré le " + doc.dateSignature().format(HORODATAGE)).setFontSize(10))
                .add(new Paragraph("L'Officier de l'état civil : " + doc.signataire()).setBold().setFontSize(10))
                .add(new Paragraph("Signé électroniquement").setFontSize(9).setFontColor(VERT))
                .add(new Paragraph("Code de vérification : " + doc.codeVerification()).setBold().setFontSize(11)
                        .setFontColor(BRUN))
                .add(new Paragraph("Empreinte SHA-256 : " + d.getEmpreinte()).setFontSize(6.5f)
                        .setFontColor(ColorConstants.GRAY));
        t.addCell(texte);
        t.addCell(new Cell().setBorder(null).add(code).setTextAlignment(TextAlignment.RIGHT));
        Div bloc = new Div().setBorder(new SolidBorder(BRUN, 1)).setPadding(10).setMarginTop(20);
        bloc.add(t);
        return bloc;
    }

    private static void ligne(Table t, String libelle, String valeur) {
        if (valeur == null || valeur.isBlank()) return;
        t.addCell(new Cell().add(new Paragraph(libelle).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(null).setBorderBottom(new SolidBorder(new DeviceRgb(230, 220, 210), 0.5f)));
        t.addCell(new Cell().add(new Paragraph(valeur).setFontSize(11).setBold())
                .setBorder(null).setBorderBottom(new SolidBorder(new DeviceRgb(230, 220, 210), 0.5f)));
    }

    private static void ajouterBloc(Document page, String titre, String texte, String siVide) {
        String contenu = texte == null || texte.isBlank() ? siVide : texte;
        if (contenu == null) return;
        page.add(new Paragraph(titre).setBold().setFontSize(10).setFontColor(BRUN).setMarginTop(12).setMarginBottom(2));
        page.add(new Paragraph(contenu).setFontSize(10).setBorderLeft(new SolidBorder(OR, 2)).setPaddingLeft(8));
    }

    private static String registrePapier(ActeRegistre a) {
        StringBuilder s = new StringBuilder();
        if (a.volume() != null) s.append("  •  Volume ").append(a.volume());
        if (a.folio() != null) s.append("  •  Folio ").append(a.folio());
        return s.toString();
    }

    private static String filiation(ActeRegistre a) {
        String pere = personne(a.prenomPere(), a.nomPere());
        String mere = personne(a.prenomMere(), a.nomMere());
        if (pere == null) return mere;
        return mere == null ? pere : pere + " et de " + mere;
    }

    private static String naissance(LocalDate date, String lieu) {
        if (date == null && lieu == null) return null;
        return (date != null ? date(date) : "") + (lieu != null ? (date != null ? " à " : "à ") + lieu : "");
    }

    private static String personne(String prenom, String nom) {
        if (prenom == null && nom == null) return null;
        return ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
    }

    private static String date(LocalDate d) {
        return d == null ? null : d.format(JOUR);
    }
}
