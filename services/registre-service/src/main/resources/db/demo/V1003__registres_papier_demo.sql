-- Données de démonstration : centres et registres des actes de démonstration, plus quelques registres
-- (exemplaires du greffe, registre manquant) pour illustrer le travail du chargé de renseignement.
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

-- Registres d'où proviennent les actes déjà saisis (exemplaire du centre)
INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, volume, lieu_conservation)
SELECT DISTINCT c.id, a.type_acte, a.annee, 'CENTRE', COALESCE(TRIM(a.volume), ''), 'Archives du centre'
FROM acte a
JOIN centre_etat_civil c ON LOWER(REPLACE(c.nom, '-', ' ')) = LOWER(REPLACE(a.centre, '-', ' '))
ON CONFLICT DO NOTHING;

INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, lieu_conservation, etat, numerise, observations)
SELECT g.centre_id, g.type_acte, g.annee, 'GREFFE', 'Greffe du tribunal d''instance', 'BON', FALSE, NULL
FROM registre_papier g WHERE g.exemplaire = 'CENTRE'
ON CONFLICT DO NOTHING;

INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, lieu_conservation, etat, numerise, observations)
SELECT id, 'NAISSANCE', 1987, 'CENTRE', 'Archives du centre', 'MANQUANT', FALSE,
       'Registre détruit lors de l''inondation de 2009 : consulter l''exemplaire du greffe'
FROM centre_etat_civil WHERE nom = 'Centre principal de Kaolack'
ON CONFLICT DO NOTHING;
INSERT INTO registre_papier (centre_id, type_acte, annee, exemplaire, lieu_conservation, etat, numerise, observations)
SELECT id, 'NAISSANCE', 1987, 'GREFFE', 'Greffe du tribunal d''instance de Kaolack', 'ABIME', FALSE, 'Pages 40 à 52 tachées'
FROM centre_etat_civil WHERE nom = 'Centre principal de Kaolack'
ON CONFLICT DO NOTHING;
