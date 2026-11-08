package sn.etatcivil.portail.demande;

public enum FormatDocument {
    NUMERIQUE("Numérique (PDF téléchargeable)"), PHYSIQUE("Papier (livraison ou retrait)");

    private final String libelle;

    FormatDocument(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }
}
