INSERT INTO type_document (code, libelle, description, type_acte, copie_integrale, tarif, delai_jours) VALUES
 ('EXTRAIT_NAISSANCE', 'Extrait d''acte de naissance',
  'Document le plus demandé : état civil de la personne et filiation.', 'NAISSANCE', FALSE, 1000, 2),
 ('COPIE_NAISSANCE', 'Copie littérale d''acte de naissance',
  'Reproduction intégrale de l''acte, avec les mentions marginales.', 'NAISSANCE', TRUE, 2000, 3),
 ('EXTRAIT_MARIAGE', 'Extrait d''acte de mariage',
  'Atteste le mariage et l''identité des époux.', 'MARIAGE', FALSE, 1500, 3),
 ('COPIE_MARIAGE', 'Copie littérale d''acte de mariage',
  'Reproduction intégrale de l''acte de mariage, témoins et mentions compris.', 'MARIAGE', TRUE, 2500, 4),
 ('EXTRAIT_DIVORCE', 'Extrait d''acte de divorce',
  'Atteste la dissolution du mariage.', 'DIVORCE', FALSE, 1500, 3),
 ('EXTRAIT_DECES', 'Extrait d''acte de décès',
  'Atteste le décès, nécessaire pour la succession ou les pensions.', 'DECES', FALSE, 1000, 2);

INSERT INTO motif (libelle, ordre) VALUES
 ('Constitution d''un dossier administratif', 1),
 ('Établissement d''une carte d''identité ou d''un passeport', 2),
 ('Inscription scolaire ou universitaire', 3),
 ('Concours ou recrutement', 4),
 ('Mariage', 5),
 ('Succession', 6),
 ('Pension ou allocation', 7),
 ('Démarche consulaire / diaspora', 8),
 ('Autre', 99);
