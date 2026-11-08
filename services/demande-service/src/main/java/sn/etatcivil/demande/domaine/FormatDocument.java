package sn.etatcivil.demande.domaine;

public enum FormatDocument {
    NUMERIQUE("Numérique (PDF téléchargeable)"), PHYSIQUE("Papier (livraison ou retrait)");

    private final String libelle;

    FormatDocument(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }
}
