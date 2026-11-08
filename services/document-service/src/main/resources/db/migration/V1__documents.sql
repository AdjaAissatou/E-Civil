CREATE TABLE document_delivre (
    id                UUID         PRIMARY KEY,
    demande_id        UUID         NOT NULL UNIQUE,
    reference         VARCHAR(30)  NOT NULL,
    demandeur_id      VARCHAR(64)  NOT NULL,
    libelle_document  VARCHAR(150) NOT NULL,
    copie_integrale   BOOLEAN      NOT NULL,
    format            VARCHAR(20)  NOT NULL,
    contenu           TEXT         NOT NULL,
    signataire        VARCHAR(150) NOT NULL,
    date_signature    TIMESTAMP    NOT NULL,
    code_verification VARCHAR(20)  NOT NULL UNIQUE,
    empreinte         VARCHAR(64)  NOT NULL
);
