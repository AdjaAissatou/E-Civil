package sn.etatcivil.habilitation.domaine;

/** Personne connectée qui effectue une opération d'administration. */
public record Administrateur(String id, String nomComplet, String email) {

    public String signature() {
        return nomComplet == null || nomComplet.isBlank() ? email : nomComplet + " <" + email + ">";
    }
}
