package sn.etatcivil.demande.domaine;

/** Citoyen qui dépose la demande (identité issue de Keycloak). */
public record Demandeur(String id, String email, String prenom, String nom) {
    public String nomComplet() { return (prenom + " " + nom).trim(); }
}
