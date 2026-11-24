package sn.etatcivil.demande.domaine;

/**
 * Motif d'une demande, avec les pièces à fournir. Un motif sans variété est proposé pour tous les documents ;
 * sinon, seulement pour sa variété.
 */
public record Motif(Long id, String libelle, int ordre, String piecesRequises, Long typeDocumentId) {

    public Motif(Long id, String libelle, int ordre) {
        this(id, libelle, ordre, null, null);
    }

    public Motif {
        if (libelle == null || libelle.isBlank()) throw new RegleMetierException("Le libellé du motif est obligatoire");
        if (libelle.length() > 150) throw new RegleMetierException("Le libellé du motif ne doit pas dépasser 150 caractères");
        if (piecesRequises != null && piecesRequises.length() > 500) {
            throw new RegleMetierException("Les pièces à fournir ne doivent pas dépasser 500 caractères");
        }
    }

    public boolean estAutre() { return "Autre".equals(libelle); }

    /** Proposé pour cette variété ? */
    public boolean concerne(Long typeDocument) { return typeDocumentId == null || typeDocumentId.equals(typeDocument); }
}
