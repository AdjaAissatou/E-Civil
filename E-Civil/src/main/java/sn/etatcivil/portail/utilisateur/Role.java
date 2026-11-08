package sn.etatcivil.portail.utilisateur;

public enum Role {
    CITOYEN("Citoyen"),
    DELEGUE("Délégué (instruction des demandes)"),
    AGENT_REGISTRE("Agent du registre (transcription)"),
    OFFICIER("Officier de l'état civil"),
    ADMIN("Administrateur");

    private final String libelle;

    Role(String libelle) { this.libelle = libelle; }

    public String getLibelle() { return libelle; }

    public boolean estAgent() { return this != CITOYEN; }
}
