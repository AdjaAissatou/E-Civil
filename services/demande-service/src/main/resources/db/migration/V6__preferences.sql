-- Préférences de notification du demandeur, choisies dans « Mon compte »
CREATE TABLE preference_demandeur (
    demandeur_id  VARCHAR(64) PRIMARY KEY,
    notifications BOOLEAN     NOT NULL DEFAULT TRUE
);
