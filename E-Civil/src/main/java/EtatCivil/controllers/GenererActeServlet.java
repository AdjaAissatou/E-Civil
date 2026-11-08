package EtatCivil.controllers;

import EtatCivil.dao.DemandeDocumentDAO;
import EtatCivil.modeles.DemandeDocument;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.HorizontalAlignment;

@WebServlet("/GenererActeServlet")
public class GenererActeServlet extends HttpServlet {

    private static final Color BLEU_PROFOND = new DeviceRgb(0, 51, 102);
    private static final Color OR_SENEGALAIS = new DeviceRgb(205, 153, 0);
    private static final Color VERT_TERRE = new DeviceRgb(76, 140, 74);
    private static final Color BORDEAUX = new DeviceRgb(115, 40, 55);
    private static final Color GRIS_LEGER = new DeviceRgb(245, 245, 245);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String demandeIdStr = request.getParameter("idDemande");
        if (demandeIdStr == null) {
            response.getWriter().write("Aucune demande spécifiée !");
            return;
        }

        try {
            int demandeId = Integer.parseInt(demandeIdStr);
            DemandeDocumentDAO demandeDAO = new DemandeDocumentDAO();
            DemandeDocument d = demandeDAO.getDemandeById(demandeId);

            if (d == null) {
                response.getWriter().write("Demande introuvable !");
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=acte_" + d.getIdDemande() + ".pdf");
            OutputStream out = response.getOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 50, 40);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfLong = new SimpleDateFormat("dd MMMM yyyy", java.util.Locale.FRENCH);

            // En-tête officiel
            Paragraph entete = new Paragraph("RÉPUBLIQUE DU SÉNÉGAL\nUN PEUPLE - UN BUT - UNE FOI")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(BLEU_PROFOND)
                    .setMarginBottom(10);

            Paragraph direction = new Paragraph("MINISTÈRE DE L’INTÉRIEUR\nDIRECTION GÉNÉRALE DE L’ÉTAT CIVIL")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(VERT_TERRE)
                    .setMarginBottom(15);

            document.add(entete);
            document.add(direction);

            // Ligne décorative
            document.add(new Paragraph("__________________________")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(OR_SENEGALAIS));

            // Titre du document
            String titreActe = getTitreOfficiel(d.getNatureDocument());
            Paragraph titre = new Paragraph(titreActe)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(BLEU_PROFOND)
                    .setMarginTop(15)
                    .setMarginBottom(15);
            document.add(titre);

            // Bloc d'informations principales
            Div bloc = new Div();
            bloc.setBackgroundColor(GRIS_LEGER);
            bloc.setPadding(15);
            bloc.setBorder(new SolidBorder(VERT_TERRE, 1));

            addInfo(bloc, "Nom :", d.getNom().toUpperCase());
            addInfo(bloc, "Prénom(s) :", d.getPrenom());
            addInfo(bloc, "Date de naissance :", sdf.format(d.getDatedenaissance()));
            addInfo(bloc, "Lieu de naissance :", d.getLieudenaissance());
            addInfo(bloc, "Sexe :", d.getSexe().equals("M") ? "Masculin" : "Féminin");
            addInfo(bloc, "Profession :", d.getProfession());
            addInfo(bloc, "Domicile :", d.getResidance());

            document.add(bloc);

            // Informations familiales
            Div famille = new Div();
            famille.setMarginTop(10);
            famille.setPadding(10);
            famille.setBorderLeft(new SolidBorder(OR_SENEGALAIS, 3));

            addInfo(famille, "Père :", "M. Moustapha Ndiaye, né à Dakar");
            addInfo(famille, "Mère :", "Mme Aïssatou Diouf, née à Thiès");
            document.add(famille);

            // Zone légale et signature
            Div officiel = new Div();
            officiel.setMarginTop(20);
            officiel.setPadding(10);
            officiel.setBorder(new SolidBorder(BLEU_PROFOND, 1));

            Paragraph mention = new Paragraph(
                    "Le présent acte est établi conformément aux dispositions du Code de l’État Civil du Sénégal et fait foi jusqu’à inscription de faux.")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setFontColor(ColorConstants.DARK_GRAY);
            officiel.add(mention);

            Paragraph signature = new Paragraph("\nFait à Dakar, le " + sdfLong.format(new Date()) +
                    "\nL’Officier de l’État Civil")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(BLEU_PROFOND)
                    .setMarginTop(15);
            officiel.add(signature);

            document.add(officiel);

            // Pied de page
            Paragraph pied = new Paragraph("Service Central de l’État Civil – Dakar | www.etatcivil.sn")
                    .setFontSize(7)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20);
            document.add(pied);

            document.close();
            System.out.println("✅ Acte officiel généré avec succès pour la demande #" + d.getIdDemande());

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Erreur lors de la génération du document : " + e.getMessage());
        }
    }

    private void addInfo(Div parent, String label, String valeur) {
        if (valeur == null || valeur.isEmpty()) return;
        Paragraph p = new Paragraph(label + " " + valeur)
                .setFontSize(10)
                .setMarginBottom(5)
                .setFontColor(ColorConstants.BLACK);
        parent.add(p);
    }

    private String getTitreOfficiel(String nature) {
        if (nature == null) return "ACTE OFFICIEL";
        switch (nature.toLowerCase()) {
            case "naissance": return "EXTRAIT D’ACTE DE NAISSANCE";
            case "mariage": return "EXTRAIT D’ACTE DE MARIAGE";
            case "deces": return "EXTRAIT D’ACTE DE DÉCÈS";
            default: return "CERTIFICAT OFFICIEL";
        }
    }
}
