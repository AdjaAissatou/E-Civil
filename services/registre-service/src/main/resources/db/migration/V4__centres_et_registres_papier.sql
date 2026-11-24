-- Centres d'état civil : centre principal de chaque commune, centres secondaires rattachés, centres consulaires
CREATE TABLE centre_etat_civil (
    id              BIGSERIAL    PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL UNIQUE,
    type            VARCHAR(20)  NOT NULL,
    commune         VARCHAR(150),
    pays            VARCHAR(150),
    rattachement_id BIGINT       REFERENCES centre_etat_civil (id),
    actif           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Registres papier : un par centre, type d'actes et année, en deux exemplaires (centre et greffe du tribunal)
CREATE TABLE registre_papier (
    id                BIGSERIAL    PRIMARY KEY,
    centre_id         BIGINT       NOT NULL REFERENCES centre_etat_civil (id),
    type_acte         VARCHAR(20)  NOT NULL,
    annee             INTEGER      NOT NULL,
    exemplaire        VARCHAR(10)  NOT NULL,
    volume            VARCHAR(30)  NOT NULL DEFAULT '',  -- '' : registre en un seul volume
    lieu_conservation VARCHAR(200),
    etat              VARCHAR(20)  NOT NULL DEFAULT 'BON',
    numerise          BOOLEAN      NOT NULL DEFAULT FALSE,
    observations      VARCHAR(1000)
);
CREATE UNIQUE INDEX uq_registre_papier ON registre_papier (centre_id, type_acte, annee, exemplaire, volume);
CREATE INDEX idx_registre_papier_recherche ON registre_papier (type_acte, annee);

-- Un centre principal par commune du référentiel
INSERT INTO centre_etat_civil (nom, type, commune)
SELECT 'Centre principal de ' || nom, 'PRINCIPAL', nom FROM commune ORDER BY region, nom;

-- Centres déjà nommés dans les actes saisis (les noms qui ne diffèrent que par un tiret sont confondus)
INSERT INTO centre_etat_civil (nom, type, commune, rattachement_id)
SELECT DISTINCT ON (a.centre) a.centre,
       CASE WHEN a.centre ILIKE 'Centre principal%' THEN 'PRINCIPAL' ELSE 'SECONDAIRE' END,
       COALESCE(a.commune, a.lieu_evenement),
       CASE WHEN a.centre ILIKE 'Centre principal%' THEN NULL ELSE
           (SELECT p.id FROM centre_etat_civil p WHERE p.type = 'PRINCIPAL'
              AND LOWER(REPLACE(p.commune, '-', ' ')) LIKE LOWER(REPLACE(COALESCE(a.commune, ''), '-', ' ')) || '%'
            ORDER BY p.id LIMIT 1) END
FROM acte a
WHERE NOT EXISTS (SELECT 1 FROM centre_etat_civil c WHERE LOWER(REPLACE(c.nom, '-', ' ')) = LOWER(REPLACE(a.centre, '-', ' ')));
-- Un centre secondaire sans centre principal connu devient principal
UPDATE centre_etat_civil SET type = 'PRINCIPAL' WHERE type = 'SECONDAIRE' AND rattachement_id IS NULL;

-- Centres consulaires des missions diplomatiques
INSERT INTO centre_etat_civil (nom, type, pays) VALUES
    ('Centre consulaire de Paris', 'CONSULAIRE', 'France'),
    ('Centre consulaire de New York', 'CONSULAIRE', 'États-Unis'),
    ('Centre consulaire de Riyad', 'CONSULAIRE', 'Arabie Saoudite');

-- Registres d'où proviennent les actes déjà saisis (exemplaire du centre)
INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, volume, lieu_conservation)
SELECT DISTINCT c.id, a.type_acte, a.annee, 'CENTRE', COALESCE(TRIM(a.volume), ''), 'Archives du centre'
FROM acte a
JOIN centre_etat_civil c ON LOWER(REPLACE(c.nom, '-', ' ')) = LOWER(REPLACE(a.centre, '-', ' '));
