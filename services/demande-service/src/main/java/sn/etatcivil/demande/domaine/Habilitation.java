package sn.etatcivil.demande.domaine;

/**
 * Ce qu'un acteur a le droit de faire sur une demande. Ces habilitations viennent des permissions que
 * l'administration attribue par les profils et les rôles ; le domaine ne connaît pas les rôles eux-mêmes.
 */
public enum Habilitation {
    DEMANDES_DEPOSER,
    DEMANDES_CONSULTER,
    DEMANDES_VALIDER,
    DEMANDES_RECHERCHER,
    DEMANDES_SUPERVISER,
    DOCUMENTS_LIVRER,
    DOCUMENTS_SIGNER
}
