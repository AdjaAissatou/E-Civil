package sn.etatcivil.demande.domaine;

/** Rôles métier de l'état civil (attribués dans Keycloak). */
public enum Role {
    CITOYEN("Citoyen"), DELEGUE("Délégué"), AGENT_REGISTRE("Agent du registre"),
    OFFICIER("Officier de l'état civil"), ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) { this.libelle = libelle; }

    public String libelle() { return libelle; }

    public boolean estAgent() { return this != CITOYEN; }
}
