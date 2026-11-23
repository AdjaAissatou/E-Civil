-- Demandes en attente de paiement, alimentées par les événements de demande-service.
CREATE TABLE demande_payable (
    demande_id       UUID         PRIMARY KEY,
    reference        VARCHAR(30)  NOT NULL,
    demandeur_id     VARCHAR(64)  NOT NULL,
    libelle_document VARCHAR(150) NOT NULL,
    montant          INTEGER      NOT NULL,
    payable          BOOLEAN      NOT NULL
);

CREATE TABLE paiement (
    id                UUID         PRIMARY KEY,
    demande_id        UUID         NOT NULL,
    token             VARCHAR(100) NOT NULL UNIQUE,
    montant           INTEGER      NOT NULL,
    statut            VARCHAR(20)  NOT NULL,
    url_paiement      VARCHAR(500) NOT NULL,
    url_recu          VARCHAR(500),
    date_creation     TIMESTAMP    NOT NULL,
    date_confirmation TIMESTAMP,
    version           INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_paiement_demande ON paiement (demande_id);
