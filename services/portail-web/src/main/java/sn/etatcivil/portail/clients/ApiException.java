package sn.etatcivil.portail.clients;

/** Réponse d'erreur d'un microservice ; {@code detail} est le message destiné à l'utilisateur. */
public class ApiException extends RuntimeException {
    private final int statut;

    public ApiException(int statut, String detail) {
        super(detail);
        this.statut = statut;
    }

    public int statut() { return statut; }

    public boolean indisponible() { return statut >= 500 || statut == 0; }
}
