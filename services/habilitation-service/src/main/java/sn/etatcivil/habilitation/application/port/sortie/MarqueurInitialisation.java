package sn.etatcivil.habilitation.application.port.sortie;

/** Garde trace des étapes d'initialisation déjà appliquées, pour ne jamais les rejouer. */
public interface MarqueurInitialisation {
    boolean estFaite(String etape);

    void marquer(String etape);
}
