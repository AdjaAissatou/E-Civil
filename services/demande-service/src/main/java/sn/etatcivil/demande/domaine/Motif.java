package sn.etatcivil.demande.domaine;

public record Motif(Long id, String libelle, int ordre) {
    public boolean estAutre() { return "Autre".equals(libelle); }
}
