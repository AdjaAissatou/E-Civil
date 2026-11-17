-- Origine de l'acte (déclaration ordinaire, jugement supplétif, transcription d'un acte étranger)
ALTER TABLE acte ADD COLUMN origine VARCHAR(20) NOT NULL DEFAULT 'ORDINAIRE';
ALTER TABLE acte ADD COLUMN reference_origine VARCHAR(500);

-- Recherche des enfants d'un couple (livret de famille)
CREATE INDEX idx_acte_nom ON acte (type_acte, nom_normalise);
