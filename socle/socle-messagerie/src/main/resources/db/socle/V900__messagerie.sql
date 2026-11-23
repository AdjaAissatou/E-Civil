-- Outbox transactionnel : l'événement est écrit dans la même transaction que la donnée métier,
-- puis relayé vers RabbitMQ. Aucun événement n'est perdu si le broker est momentanément indisponible.
CREATE TABLE evenement_sortant (
    id              VARCHAR(36)  PRIMARY KEY,
    routage         VARCHAR(100) NOT NULL,
    contenu         TEXT         NOT NULL,
    date_creation   TIMESTAMP    NOT NULL,
    date_envoi      TIMESTAMP,
    tentatives      INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_evenement_sortant_attente ON evenement_sortant (date_envoi, date_creation);

-- Événements déjà traités : un même message reçu deux fois n'a d'effet qu'une fois.
CREATE TABLE evenement_traite (
    id              VARCHAR(36)  PRIMARY KEY,
    date_traitement TIMESTAMP    NOT NULL
);
