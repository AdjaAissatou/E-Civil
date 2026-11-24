package sn.etatcivil.demande.adaptateur.sortie.images;

import org.springframework.stereotype.Component;
import sn.etatcivil.demande.application.port.sortie.TraitementImages;
import sn.etatcivil.demande.domaine.RegleMetierException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Décode la photo (refus de tout ce qui n'est pas une image), la réduit à 1600 pixels au plus et la réenregistre
 * en JPEG : seuls les pixels sont conservés, aucune métadonnée (position GPS, appareil, date) ni contenu caché.
 */
@Component
class TraitementImagesJava implements TraitementImages {

    private static final int COTE_MAXIMAL = 1600;
    private static final int COTE_MINIMAL = 240;
    private static final long PIXELS_MAXIMUM = 40_000_000L; // protège contre les images « bombes »

    @Override
    public Image normaliser(byte[] contenu, String libelle) {
        BufferedImage source;
        try {
            var lecteurs = ImageIO.getImageReaders(ImageIO.createImageInputStream(new ByteArrayInputStream(contenu)));
            if (!lecteurs.hasNext()) throw new RegleMetierException(libelle + " : le fichier n'est pas une photo");
            var lecteur = lecteurs.next();
            try (var entree = ImageIO.createImageInputStream(new ByteArrayInputStream(contenu))) {
                lecteur.setInput(entree);
                long pixels = (long) lecteur.getWidth(0) * lecteur.getHeight(0);
                if (pixels > PIXELS_MAXIMUM) throw new RegleMetierException(libelle + " : la photo est trop grande");
                source = lecteur.read(0);
            } finally {
                lecteur.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof RegleMetierException r) throw r;
            throw new RegleMetierException(libelle + " : la photo est illisible");
        }
        if (source == null) throw new RegleMetierException(libelle + " : la photo est illisible");
        int l = source.getWidth(), h = source.getHeight();
        if (Math.min(l, h) < COTE_MINIMAL) {
            throw new RegleMetierException(libelle + " : la photo est trop petite pour être lue (" + l + " × " + h + " pixels)");
        }
        double echelle = Math.min(1.0, (double) COTE_MAXIMAL / Math.max(l, h));
        int nl = (int) Math.round(l * echelle), nh = (int) Math.round(h * echelle);
        BufferedImage rgb = new BufferedImage(nl, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, nl, nh);
        g.drawImage(source, 0, 0, nl, nh, null);
        g.dispose();
        try (ByteArrayOutputStream sortie = new ByteArrayOutputStream(); ImageOutputStream flux = ImageIO.createImageOutputStream(sortie)) {
            ImageWriter ecrivain = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam p = ecrivain.getDefaultWriteParam();
            p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            p.setCompressionQuality(0.85f);
            ecrivain.setOutput(flux);
            ecrivain.write(null, new IIOImage(rgb, null, null), p);
            ecrivain.dispose();
            flux.flush();
            return new Image(sortie.toByteArray(), "image/jpeg");
        } catch (IOException e) {
            throw new IllegalStateException("Réencodage JPEG impossible", e);
        }
    }
}
