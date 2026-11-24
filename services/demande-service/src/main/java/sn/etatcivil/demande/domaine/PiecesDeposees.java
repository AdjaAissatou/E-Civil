package sn.etatcivil.demande.domaine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Photos jointes au dépôt d'une demande, avant enregistrement. Règles : formats JPEG ou PNG, 5 Mo au plus par photo,
 * côtés exigés par la pièce choisie, selfie toujours exigé.
 */
public final class PiecesDeposees {

    public static final int TAILLE_MAXIMALE = 5 * 1024 * 1024;

    /** Photo reçue : son contenu brut et le type annoncé par le navigateur. */
    public record Photo(byte[] contenu, String contentType) {}

    public static final PiecesDeposees AUCUNE = new PiecesDeposees(null, Map.of());

    private final PieceIdentite.TypePiece typePiece;
    private final Map<PieceIdentite.Nature, Photo> photos;

    public PiecesDeposees(PieceIdentite.TypePiece typePiece, Map<PieceIdentite.Nature, Photo> photos) {
        this.typePiece = typePiece;
        EnumMap<PieceIdentite.Nature, Photo> m = new EnumMap<>(PieceIdentite.Nature.class);
        photos.forEach((k, v) -> { if (v != null && v.contenu() != null && v.contenu().length > 0) m.put(k, v); });
        this.photos = Collections.unmodifiableMap(m);
    }

    public boolean vides() { return photos.isEmpty(); }

    /** Vérifie que les photos exigées sont là et recevables ; le contenu lui-même est contrôlé à l'enregistrement. */
    public void verifierPour(TypeDocument type) {
        if (!type.verificationIdentite()) {
            if (!photos.isEmpty()) throw new RegleMetierException("Ce document ne demande aucune pièce d'identité");
            return;
        }
        if (typePiece == null) throw new RegleMetierException("Choisissez la pièce d'identité présentée (carte d'identité ou passeport)");
        for (PieceIdentite.Nature cote : typePiece.cotes()) {
            if (!photos.containsKey(cote)) {
                throw new RegleMetierException("Joignez la photo de votre " + typePiece.libelle().toLowerCase()
                        + (typePiece == PieceIdentite.TypePiece.CNI ? " (" + cote.name().toLowerCase() + ")" : " (page avec la photo)"));
            }
        }
        if (!photos.containsKey(PieceIdentite.Nature.SELFIE)) {
            throw new RegleMetierException("Prenez un selfie : il permet au délégué de vérifier que la pièce est bien la vôtre");
        }
        photos.forEach((nature, p) -> {
            if (p.contenu().length > TAILLE_MAXIMALE) {
                throw new RegleMetierException(nature.libelle() + " : la photo dépasse 5 Mo");
            }
            String t = p.contentType() == null ? "" : p.contentType().toLowerCase(java.util.Locale.ROOT);
            if (!t.equals("image/jpeg") && !t.equals("image/png")) {
                throw new RegleMetierException(nature.libelle() + " : seules les photos JPEG ou PNG sont acceptées");
            }
        });
    }

    public PieceIdentite.TypePiece typePiece() { return typePiece; }

    public Map<PieceIdentite.Nature, Photo> photos() { return photos; }
}
