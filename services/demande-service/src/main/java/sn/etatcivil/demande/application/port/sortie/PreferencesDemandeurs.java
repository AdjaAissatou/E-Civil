package sn.etatcivil.demande.application.port.sortie;

public interface PreferencesDemandeurs {
    /** Le demandeur accepte-t-il les emails ? Oui par défaut. */
    boolean notifications(String demandeurId);

    void definir(String demandeurId, boolean notifications);
}
