-- Vérification d'identité : pour les documents sensibles, le demandeur joint la photo de sa pièce d'identité
-- (carte nationale d'identité recto et verso, ou passeport) et un selfie, que le délégué compare avant de valider.
ALTER TABLE type_document ADD COLUMN verification_identite BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE demande ADD COLUMN verification_identite BOOLEAN NOT NULL DEFAULT FALSE;

-- Exigée pour les documents qui révèlent la filiation complète, les mentions (divorce…) ou qui attestent la
-- situation de la personne : actes originaux, copies littérales, livret, extraits de mention, certificats de
-- célibat, capacité, vie, nationalité, domicile, non-appartenance, coutume. Pas pour les extraits ni les
-- certificats de naissance, de mariage ou de décès.
UPDATE type_document SET verification_identite = TRUE
WHERE modele IN ('ACTE_ORIGINAL', 'COPIE_INTEGRALE', 'LIVRET_FAMILLE', 'EXTRAIT_MENTION', 'CERTIFICAT_CELIBAT',
                 'CERTIFICAT_CAPACITE_MATRIMONIALE', 'CERTIFICAT_VIE', 'CERTIFICAT_NATIONALITE', 'CERTIFICAT_DOMICILE',
                 'CERTIFICAT_NON_APPARTENANCE', 'CERTIFICAT_COUTUME');

CREATE TABLE piece_identite (
    id           UUID         PRIMARY KEY,
    demande_id   UUID         NOT NULL REFERENCES demande (id),
    nature       VARCHAR(10)  NOT NULL,
    type_piece   VARCHAR(15)  NOT NULL,
    content_type VARCHAR(40)  NOT NULL,
    taille       INTEGER      NOT NULL,
    empreinte    VARCHAR(64)  NOT NULL,
    contenu      BYTEA,
    date_depot   TIMESTAMP    NOT NULL,
    date_purge   TIMESTAMP
);
CREATE INDEX idx_piece_identite_demande ON piece_identite (demande_id);
