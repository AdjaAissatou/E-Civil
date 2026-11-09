CREATE TABLE acte (
    id                        UUID         PRIMARY KEY,
    type_acte                 VARCHAR(20)  NOT NULL,
    numero_acte               VARCHAR(40)  NOT NULL UNIQUE,
    annee                     INTEGER      NOT NULL,
    statut                    VARCHAR(20)  NOT NULL,
    centre                    VARCHAR(150) NOT NULL,
    commune                   VARCHAR(150),
    volume                    VARCHAR(30),
    folio                     VARCHAR(30),
    date_evenement            DATE         NOT NULL,
    lieu_evenement            VARCHAR(150) NOT NULL,
    nom                       VARCHAR(100) NOT NULL,
    prenom                    VARCHAR(150) NOT NULL,
    nom_normalise             VARCHAR(100) NOT NULL,
    prenom_normalise          VARCHAR(150) NOT NULL,
    sexe                      VARCHAR(1),
    date_naissance            DATE,
    lieu_naissance            VARCHAR(150),
    nom_pere                  VARCHAR(100),
    prenom_pere               VARCHAR(150),
    nom_mere                  VARCHAR(100),
    prenom_mere               VARCHAR(150),
    nom_conjoint              VARCHAR(100),
    prenom_conjoint           VARCHAR(150),
    nom_conjoint_normalise    VARCHAR(100),
    prenom_conjoint_normalise VARCHAR(150),
    date_naissance_conjoint   DATE,
    lieu_naissance_conjoint   VARCHAR(150),
    heure_deces               VARCHAR(5),
    cause_deces               VARCHAR(255),
    temoin1                   VARCHAR(200),
    temoin2                   VARCHAR(200),
    mentions_marginales       VARCHAR(2000),
    observations              VARCHAR(2000),
    agent_saisie              VARCHAR(150) NOT NULL,
    date_saisie               TIMESTAMP    NOT NULL,
    date_modification         TIMESTAMP    NOT NULL,
    version                   INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX idx_acte_titulaire ON acte (type_acte, nom_normalise, prenom_normalise);
CREATE INDEX idx_acte_conjoint ON acte (type_acte, nom_conjoint_normalise, prenom_conjoint_normalise);

CREATE TABLE numerotation (
    type_acte      VARCHAR(20) NOT NULL,
    annee          INTEGER     NOT NULL,
    dernier_numero BIGINT      NOT NULL,
    PRIMARY KEY (type_acte, annee)
);
