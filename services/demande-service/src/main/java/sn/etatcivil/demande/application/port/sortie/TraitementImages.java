package sn.etatcivil.demande.application.port.sortie;

/**
 * Relit une photo et la réenregistre en JPEG : ce qui n'est pas une image est refusé, et les métadonnées (position
 * GPS, appareil…) disparaissent.
 */
public interface TraitementImages {

    record Image(byte[] contenu, String contentType) {}

    /** @throws sn.etatcivil.demande.domaine.RegleMetierException si le fichier n'est pas une photo exploitable */
    Image normaliser(byte[] contenu, String libelle);
}
