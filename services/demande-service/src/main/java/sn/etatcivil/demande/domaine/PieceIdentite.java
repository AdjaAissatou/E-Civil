package sn.etatcivil.demande.domaine;

import java.time.Instant;
import java.util.UUID;

/**
 * Photo déposée avec une demande pour vérifier l'identité du demandeur : pièce d'identité (recto, verso) et selfie.
 * Le contenu est effacé quelques semaines après la clôture de la demande ; il en reste l'empreinte et les dates.
 */
public record PieceIdentite(UUID id, UUID demandeId, Nature nature, TypePiece typePiece, String contentType, int taille,
                            String empreinte, Instant dateDepot, Instant datePurge) {

    /** Côté de la pièce ou selfie. */
    public enum Nature {
        RECTO("Pièce d'identité (recto)"), VERSO("Pièce d'identité (verso)"), SELFIE("Selfie");

        private final String libelle;

        Nature(String libelle) { this.libelle = libelle; }

        public String libelle() { return libelle; }
    }

    /** Pièce présentée : carte nationale d'identité (recto et verso) ou passeport (page photo). */
    public enum TypePiece {
        CNI("Carte nationale d'identité"), PASSEPORT("Passeport");

        private final String libelle;

        TypePiece(String libelle) { this.libelle = libelle; }

        public String libelle() { return libelle; }

        /** Côtés à fournir pour cette pièce. */
        public java.util.Set<Nature> cotes() {
            return this == CNI ? java.util.EnumSet.of(Nature.RECTO, Nature.VERSO) : java.util.EnumSet.of(Nature.RECTO);
        }
    }

    public boolean purgee() { return datePurge != null; }
}
