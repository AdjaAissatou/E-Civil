-- Modèle de chaque document (ce qu'il contient) et origine exigée de l'acte (jugement supplétif, transcription)
ALTER TABLE type_document ADD COLUMN modele VARCHAR(40) NOT NULL DEFAULT 'EXTRAIT';
ALTER TABLE type_document ADD COLUMN origine_acte VARCHAR(30);
UPDATE type_document SET modele = 'COPIE_INTEGRALE' WHERE copie_integrale;
ALTER TABLE type_document DROP COLUMN copie_integrale;

ALTER TABLE demande ADD COLUMN modele VARCHAR(40) NOT NULL DEFAULT 'EXTRAIT';
ALTER TABLE demande ADD COLUMN origine_acte VARCHAR(30);
UPDATE demande SET modele = 'COPIE_INTEGRALE' WHERE copie_integrale;

-- Documents ajoutés au catalogue (repris du catalogue de la version d'origine, limité à l'état civil)
INSERT INTO type_document (code, libelle, description, type_acte, modele, origine_acte, tarif, delai_jours) VALUES
 ('EXTRAIT_NAISSANCE_SANS_FILIATION', 'Extrait d''acte de naissance sans filiation',
  'Extrait sans les noms des parents, pour les démarches qui ne demandent que l''identité.',
  'NAISSANCE', 'EXTRAIT_SANS_FILIATION', NULL, 1000, 1),
 ('JUGEMENT_SUPPLETIF_NAISSANCE', 'Jugement supplétif de naissance',
  'Copie de l''acte de naissance dressé en exécution d''un jugement supplétif, avec les références du jugement.',
  'NAISSANCE', 'COPIE_INTEGRALE', 'JUGEMENT_SUPPLETIF', 500, 1),
 ('TRANSCRIPTION_NAISSANCE', 'Transcription d''acte de naissance étranger',
  'Copie de l''acte de naissance dressé à l''étranger et transcrit sur les registres sénégalais.',
  'NAISSANCE', 'COPIE_INTEGRALE', 'TRANSCRIPTION', 500, 1),
 ('COPIE_RECONNAISSANCE', 'Copie d''acte de reconnaissance',
  'Reconnaissance d''un enfant par son père ou sa mère, y compris la reconnaissance anticipée faite avant la naissance.',
  'RECONNAISSANCE', 'COPIE_INTEGRALE', NULL, 500, 1),
 ('JUGEMENT_SUPPLETIF_MARIAGE', 'Jugement supplétif de mariage',
  'Copie de l''acte de mariage dressé en exécution d''un jugement supplétif, avec les références du jugement.',
  'MARIAGE', 'COPIE_INTEGRALE', 'JUGEMENT_SUPPLETIF', 500, 1),
 ('TRANSCRIPTION_MARIAGE', 'Transcription d''acte de mariage étranger',
  'Copie de l''acte de mariage célébré à l''étranger et transcrit sur les registres sénégalais.',
  'MARIAGE', 'COPIE_INTEGRALE', 'TRANSCRIPTION', 500, 1),
 ('DUPLICATA_LIVRET_FAMILLE', 'Duplicata de livret de famille',
  'Reconstitue le livret à partir de l''acte de mariage et des actes de naissance des enfants du couple.',
  'MARIAGE', 'LIVRET_FAMILLE', NULL, 5000, 5),
 ('COPIE_DECES', 'Copie littérale d''acte de décès',
  'Reproduction intégrale de l''acte de décès, témoins et mentions compris.',
  'DECES', 'COPIE_INTEGRALE', NULL, 2000, 3),
 ('JUGEMENT_SUPPLETIF_DECES', 'Jugement supplétif de décès',
  'Copie de l''acte de décès dressé en exécution d''un jugement supplétif, avec les références du jugement.',
  'DECES', 'COPIE_INTEGRALE', 'JUGEMENT_SUPPLETIF', 500, 1),
 ('TRANSCRIPTION_DECES', 'Transcription d''acte de décès étranger',
  'Copie de l''acte de décès survenu à l''étranger et transcrit sur les registres sénégalais.',
  'DECES', 'COPIE_INTEGRALE', 'TRANSCRIPTION', 500, 1),
 ('CERTIFICAT_CELIBAT', 'Certificat de célibat',
  'Atteste qu''aucun mariage n''est inscrit au registre national à votre nom (mariage civil ou religieux, adoption).',
  'NAISSANCE', 'CERTIFICAT_CELIBAT', NULL, 500, 1),
 ('CERTIFICAT_CAPACITE_MATRIMONIALE', 'Certificat de capacité matrimoniale',
  'Atteste que vous pouvez contracter mariage, notamment à l''étranger : aucun mariage en cours n''est inscrit à votre nom.',
  'NAISSANCE', 'CERTIFICAT_CAPACITE_MATRIMONIALE', NULL, 500, 1),
 ('CERTIFICAT_VIE', 'Certificat de vie',
  'Pour la retraite, une pension ou une succession. Présentez-vous au centre avec votre pièce d''identité : le délégué valide la demande après vous avoir vu.',
  'NAISSANCE', 'CERTIFICAT_VIE', NULL, 500, 1);

INSERT INTO motif (libelle, ordre) VALUES
 ('Adoption', 9),
 ('Retraite', 10);
