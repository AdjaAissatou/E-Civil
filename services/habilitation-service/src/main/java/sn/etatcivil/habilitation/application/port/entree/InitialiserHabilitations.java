package sn.etatcivil.habilitation.application.port.entree;

public interface InitialiserHabilitations {
    /** Idempotent : crée ce qui manque et, une seule fois, migre les comptes existants vers les profils. */
    void initialiser();
}
