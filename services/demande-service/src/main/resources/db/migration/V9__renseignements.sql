-- Dossiers de renseignement : recherche d'un acte dans les registres papier par le chargé de renseignement
CREATE TABLE renseignement (
    demande_id          UUID          PRIMARY KEY REFERENCES demande (id),
    date_ouverture      TIMESTAMP     NOT NULL,
    urgent              BOOLEAN       NOT NULL DEFAULT FALSE,
    agent_id            VARCHAR(64),
    agent_nom           VARCHAR(200),
    date_prise_en_charge TIMESTAMP,
    motif_non_trouve    VARCHAR(1000),
    agent_conclusion    VARCHAR(250),
    date_conclusion     TIMESTAMP
);

-- Registres papier consultés (identifiant et libellé copiés depuis le registre)
CREATE TABLE consultation_registre (
    id                BIGSERIAL     PRIMARY KEY,
    demande_id        UUID          NOT NULL REFERENCES renseignement (demande_id),
    registre_id       BIGINT        NOT NULL,
    registre          VARCHAR(300)  NOT NULL,
    resultat          VARCHAR(30)   NOT NULL,
    folio             VARCHAR(30),
    note              VARCHAR(1000),
    agent             VARCHAR(250)  NOT NULL,
    date_consultation TIMESTAMP     NOT NULL
);
CREATE INDEX idx_consultation_registre_demande ON consultation_registre (demande_id);
