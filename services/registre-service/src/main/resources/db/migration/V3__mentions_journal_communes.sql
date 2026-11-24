-- Énonciations de l'acte : professions et domiciles des personnes, médecin qui constate le décès
ALTER TABLE acte ADD COLUMN profession          VARCHAR(150);
ALTER TABLE acte ADD COLUMN domicile            VARCHAR(200);
ALTER TABLE acte ADD COLUMN profession_pere     VARCHAR(150);
ALTER TABLE acte ADD COLUMN domicile_pere       VARCHAR(200);
ALTER TABLE acte ADD COLUMN profession_mere     VARCHAR(150);
ALTER TABLE acte ADD COLUMN domicile_mere       VARCHAR(200);
ALTER TABLE acte ADD COLUMN profession_conjoint VARCHAR(150);
ALTER TABLE acte ADD COLUMN domicile_conjoint   VARCHAR(200);
ALTER TABLE acte ADD COLUMN medecin             VARCHAR(200);

-- Mentions marginales rattachées à leur acte (remplacent le texte libre « mentions_marginales »)
CREATE TABLE mention (
    id           BIGSERIAL    PRIMARY KEY,
    acte_id      UUID         NOT NULL REFERENCES acte (id),
    type         VARCHAR(20)  NOT NULL,
    date_mention DATE         NOT NULL,
    reference    VARCHAR(300),
    details      VARCHAR(1000),
    agent        VARCHAR(200),
    date_saisie  TIMESTAMP
);
CREATE INDEX idx_mention_acte ON mention (acte_id);

-- Reprise des mentions déjà saisies en texte libre
INSERT INTO mention (acte_id, type, date_mention, details, agent, date_saisie)
SELECT id,
       CASE WHEN mentions_marginales ILIKE 'mari%' THEN 'MARIAGE'
            WHEN mentions_marginales ILIKE 'divor%' THEN 'DIVORCE'
            WHEN mentions_marginales ILIKE 'décéd%' OR mentions_marginales ILIKE 'deced%' THEN 'DECES'
            ELSE 'AUTRE' END,
       GREATEST(date_evenement, CAST(date_modification AS DATE)), mentions_marginales, agent_saisie, date_modification
FROM acte WHERE mentions_marginales IS NOT NULL AND TRIM(mentions_marginales) <> '';
UPDATE acte SET mentions_marginales = NULL WHERE mentions_marginales IS NOT NULL;

-- Journal des opérations sur les actes (qui a saisi, corrigé, validé, porté une mention)
CREATE TABLE journal_acte (
    id             BIGSERIAL    PRIMARY KEY,
    acte_id        UUID         NOT NULL REFERENCES acte (id),
    date_operation TIMESTAMP    NOT NULL,
    auteur         VARCHAR(200) NOT NULL,
    action         VARCHAR(20)  NOT NULL,
    detail         VARCHAR(4000)
);
CREATE INDEX idx_journal_acte ON journal_acte (acte_id);

-- Les actes existants reçoivent leur entrée de saisie
INSERT INTO journal_acte (acte_id, date_operation, auteur, action, detail)
SELECT id, date_saisie, agent_saisie, 'TRANSCRIPTION', 'Acte ' || numero_acte || ' saisi' FROM acte;
INSERT INTO journal_acte (acte_id, date_operation, auteur, action, detail)
SELECT id, date_modification, 'non tracé (avant le journal)', 'VALIDATION', 'Acte validé' FROM acte WHERE statut = 'VALIDE';

-- Référentiel des communes (données de la version 1, complétées de la région de Louga)
CREATE TABLE commune (
    id          SERIAL       PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    departement VARCHAR(150) NOT NULL,
    region      VARCHAR(150) NOT NULL
);
INSERT INTO commune (nom, departement, region) VALUES
 ('Dakar-Plateau', 'Dakar', 'Dakar'),
 ('Gorée', 'Dakar', 'Dakar'),
 ('Guédiawaye', 'Guédiawaye', 'Dakar'),
 ('Pikine', 'Pikine', 'Dakar'),
 ('Rufisque', 'Rufisque', 'Dakar'),
 ('Bargny', 'Rufisque', 'Dakar'),
 ('Diamniadio', 'Rufisque', 'Dakar'),
 ('Sébikhotane', 'Rufisque', 'Dakar'),
 ('Thiès-Est', 'Thiès', 'Thiès'),
 ('Thiès-Ouest', 'Thiès', 'Thiès'),
 ('Mbour', 'Mbour', 'Thiès'),
 ('Joal-Fadiouth', 'Mbour', 'Thiès'),
 ('Saly-Portudal', 'Mbour', 'Thiès'),
 ('Ngaparou', 'Mbour', 'Thiès'),
 ('Kayar', 'Thiès', 'Thiès'),
 ('Kaolack', 'Kaolack', 'Kaolack'),
 ('Guinguinéo', 'Guinguinéo', 'Kaolack'),
 ('Kahone', 'Kaolack', 'Kaolack'),
 ('Ndoffane', 'Kaolack', 'Kaolack'),
 ('Gandiaye', 'Kaolack', 'Kaolack'),
 ('Diourbel', 'Diourbel', 'Diourbel'),
 ('Bambey', 'Bambey', 'Diourbel'),
 ('Mbacké', 'Mbacké', 'Diourbel'),
 ('Touba-Mosquée', 'Mbacké', 'Diourbel'),
 ('Fatick', 'Fatick', 'Fatick'),
 ('Diofior', 'Fatick', 'Fatick'),
 ('Foundiougne', 'Fatick', 'Fatick'),
 ('Sokone', 'Fatick', 'Fatick'),
 ('Soum', 'Fatick', 'Fatick'),
 ('Kaffrine', 'Kaffrine', 'Kaffrine'),
 ('Nganda', 'Kaffrine', 'Kaffrine'),
 ('Birkelane', 'Kaffrine', 'Kaffrine'),
 ('Koungheul', 'Koungheul', 'Kaffrine'),
 ('Malem Hodar', 'Malem Hodar', 'Kaffrine'),
 ('Ziguinchor', 'Ziguinchor', 'Ziguinchor'),
 ('Bignona', 'Bignona', 'Ziguinchor'),
 ('Oussouye', 'Oussouye', 'Ziguinchor'),
 ('Diouloulou', 'Bignona', 'Ziguinchor'),
 ('Saint-Louis', 'Saint-Louis', 'Saint-Louis'),
 ('Richard-Toll', 'Dagana', 'Saint-Louis'),
 ('Dagana', 'Dagana', 'Saint-Louis'),
 ('Podor', 'Podor', 'Saint-Louis'),
 ('Rosso-Sénégal', 'Dagana', 'Saint-Louis'),
 ('Matam', 'Matam', 'Matam'),
 ('Ourossogui', 'Matam', 'Matam'),
 ('Kanel', 'Kanel', 'Matam'),
 ('Ranérou', 'Ranérou', 'Matam'),
 ('Tambacounda', 'Tambacounda', 'Tambacounda'),
 ('Goudiry', 'Goudiry', 'Tambacounda'),
 ('Koumpentoum', 'Koumpentoum', 'Tambacounda'),
 ('Bakel', 'Bakel', 'Tambacounda'),
 ('Kolda', 'Kolda', 'Kolda'),
 ('Médina Yoro Foulah', 'Kolda', 'Kolda'),
 ('Vélingara', 'Kolda', 'Kolda'),
 ('Diaobé', 'Vélingara', 'Kolda'),
 ('Sédhiou', 'Sédhiou', 'Sédhiou'),
 ('Bounkiling', 'Bounkiling', 'Sédhiou'),
 ('Goudomp', 'Goudomp', 'Sédhiou'),
 ('Marsassoum', 'Sédhiou', 'Sédhiou'),
 ('Kédougou', 'Kédougou', 'Kédougou'),
 ('Salémata', 'Salémata', 'Kédougou'),
 ('Saraya', 'Saraya', 'Kédougou'),
 ('Louga', 'Louga', 'Louga'),
 ('Kébémer', 'Kébémer', 'Louga'),
 ('Linguère', 'Linguère', 'Louga'),
 ('Dahra', 'Linguère', 'Louga');
