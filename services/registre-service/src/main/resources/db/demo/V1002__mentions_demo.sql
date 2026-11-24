-- Données de démonstration : les mentions saisies en texte (V1000) deviennent des mentions rattachées
INSERT INTO mention (acte_id, type, date_mention, details, agent, date_saisie)
SELECT id,
       CASE WHEN mentions_marginales ILIKE 'mari%' THEN 'MARIAGE'
            WHEN mentions_marginales ILIKE 'divor%' THEN 'DIVORCE'
            WHEN mentions_marginales ILIKE 'décéd%' THEN 'DECES'
            ELSE 'AUTRE' END,
       GREATEST(date_evenement, CAST(date_modification AS DATE)), mentions_marginales, agent_saisie, date_modification
FROM acte WHERE mentions_marginales IS NOT NULL AND TRIM(mentions_marginales) <> '';
UPDATE acte SET mentions_marginales = NULL WHERE mentions_marginales IS NOT NULL;
INSERT INTO journal_acte (acte_id, date_operation, auteur, action, detail)
SELECT a.id, a.date_saisie, a.agent_saisie, 'TRANSCRIPTION', 'Acte ' || a.numero_acte || ' saisi'
FROM acte a WHERE NOT EXISTS (SELECT 1 FROM journal_acte j WHERE j.acte_id = a.id);
