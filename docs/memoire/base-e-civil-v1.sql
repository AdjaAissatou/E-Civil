-- =====================================================================================================
--  Base « E-Civil » de la version 1 (code du mémoire : Servlets/JSP, commit 28d59f4)
--
--  Reconstruite à partir des requêtes SQL des DAO et des modèles Java, la base d'origine ayant été perdue.
--  Chaque table et chaque colonne correspond à ce que le code lit ou écrit ; les types sont déduits des
--  modèles. Les contraintes (clés étrangères, NOT NULL) sont volontairement souples pour accepter le
--  comportement du code d'origine.
--
--  Utilisation (PostgreSQL) :
--      createdb -U postgres E-Civil
--      psql -U postgres -d E-Civil -f base-e-civil-v1.sql
--
--  La version actuelle (microservices) n'a PAS besoin de cette base : ses bases sont créées
--  automatiquement par Flyway au démarrage.
--
--  La base du ServeurCentral (« serveur-central-etat-civil ») n'a pas besoin de script : sa table
--  actes_registre est créée par Hibernate au démarrage (spring.jpa.hibernate.ddl-auto=update).
-- =====================================================================================================

-- ------------------------------------------------------------------ référentiels
CREATE TABLE communes (
    idcommune  SERIAL PRIMARY KEY,
    nom        VARCHAR(150) NOT NULL,
    ville      VARCHAR(150)
);

CREATE TABLE varietes (
    idvariete        SERIAL PRIMARY KEY,
    nom              VARCHAR(100) NOT NULL,        -- Naissance, Mariage, Divorce, Décès
    description      TEXT,
    mots_cles        VARCHAR(255),                 -- utilisés par la vue recherche_documents
    conjoint         BOOLEAN DEFAULT FALSE,        -- champs à demander dans le formulaire
    temoins          BOOLEAN DEFAULT FALSE,
    parents          BOOLEAN DEFAULT FALSE,
    medecin          BOOLEAN DEFAULT FALSE,
    mentionmarginale BOOLEAN DEFAULT FALSE,
    causedeces       BOOLEAN DEFAULT FALSE
);

CREATE TABLE nature (                               -- types de documents délivrables
    idnature         SERIAL PRIMARY KEY,
    nom              VARCHAR(150) NOT NULL,
    description      TEXT,
    tarif_base       NUMERIC(10,2) DEFAULT 0,       -- seules les natures à tarif > 0 sont proposées
    duree_validite   INTEGER,                       -- en mois
    delai_traitement INTEGER,                       -- en jours
    idvariete        INTEGER REFERENCES varietes (idvariete)
);

CREATE TABLE motif_demande (
    idmotif          SERIAL PRIMARY KEY,
    idnature         INTEGER REFERENCES nature (idnature),
    libelle          VARCHAR(150) NOT NULL,
    description      TEXT,
    documents_requis TEXT
);

-- ------------------------------------------------------------------ personnes et comptes
CREATE TABLE personne (
    id              SERIAL PRIMARY KEY,
    prenom          VARCHAR(150),
    nom             VARCHAR(100),
    sexe            VARCHAR(10),
    datedenaissance DATE,
    lieudenaissance VARCHAR(150),
    profession      VARCHAR(150),
    residance       VARCHAR(255)                    -- orthographe du code d'origine
);

CREATE TABLE compte (
    idcompte   SERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    motdepasse VARCHAR(255) NOT NULL,              -- haché en BCrypt par PasswordUtil
    statut     BOOLEAN DEFAULT TRUE,
    role       VARCHAR(30) DEFAULT 'utilisateur',  -- utilisateur, delegue, officier, admin
    id         INTEGER REFERENCES personne (id)
);

CREATE TABLE utilisateur (
    idutilisateur              SERIAL PRIMARY KEY,
    idcompte                   INTEGER REFERENCES compte (idcompte),
    id                         INTEGER REFERENCES personne (id),
    datedinscription           DATE DEFAULT CURRENT_DATE,
    numerocartedintentite_visa BIGINT,             -- int dans le modèle : BIGINT évite les débordements
    numerotelephone            BIGINT,
    nombredenfants             INTEGER DEFAULT 0
);

CREATE TABLE typeutilisateur (
    idtype           SERIAL PRIMARY KEY,
    idutilisateur    INTEGER REFERENCES utilisateur (idutilisateur),
    nom              VARCHAR(100),                  -- résident, diaspora…
    paysderesidence  VARCHAR(100)
);

-- ------------------------------------------------------------------ rôles et permissions (administration)
CREATE TABLE role (
    idrole      SERIAL PRIMARY KEY,
    nom         VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE permission (
    idpermission SERIAL PRIMARY KEY,
    nom          VARCHAR(100) NOT NULL,
    description  TEXT,
    avantage     TEXT,
    interdiction TEXT
);

CREATE TABLE role_permissions (
    role_id       INTEGER REFERENCES role (idrole) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permission (idpermission) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE profil (
    "idProfil" SERIAL PRIMARY KEY,                  -- casse du code d'origine (p.idProfil)
    nom        VARCHAR(100) NOT NULL,
    idrole     INTEGER REFERENCES role (idrole)
);

-- ------------------------------------------------------------------ registre local des documents
CREATE TABLE documentetatcivil (
    iddocument         SERIAL PRIMARY KEY,
    idcommune          INTEGER REFERENCES communes (idcommune),
    numeroregistre     INTEGER,
    numerodocument     INTEGER UNIQUE,
    dateevenement      DATE,
    lieuevenement      VARCHAR(150),
    dateenregistrement DATE,
    idnature           INTEGER REFERENCES nature (idnature),
    statut             VARCHAR(20) DEFAULT 'actif',
    date_creation      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(150)
);

-- ------------------------------------------------------------------ paiements et demandes
CREATE TABLE paiement (
    idpaiement   SERIAL PRIMARY KEY,
    datepaiement TIMESTAMP DEFAULT NOW(),
    montant      NUMERIC(10,2),
    status       VARCHAR(30) DEFAULT 'en attente',
    recu         VARCHAR(500),
    modepaiement VARCHAR(50),
    reference    VARCHAR(100)                       -- référence de la transaction
);

CREATE TABLE demandedocument (
    iddemande         SERIAL PRIMARY KEY,
    date              DATE DEFAULT CURRENT_DATE,
    format            VARCHAR(30),                  -- numérique ou papier
    status            VARCHAR(30) DEFAULT 'en cours',
                      -- valeurs du code : en cours, validée, en attente, payé, recherché, trouvé, signé
    qrcode            TEXT,
    id                INTEGER REFERENCES personne (id),
    idutilisateur     INTEGER REFERENCES utilisateur (idutilisateur),
    idnature          INTEGER REFERENCES nature (idnature),
    idmotif           INTEGER REFERENCES motif_demande (idmotif),
    iddocument        INTEGER REFERENCES documentetatcivil (iddocument),
    idpaiement        INTEGER REFERENCES paiement (idpaiement),
    prenom            VARCHAR(150),                 -- personne concernée par l'acte
    nom               VARCHAR(100),
    datedenaissance   DATE,
    lieudenaissance   VARCHAR(150),
    sexe              VARCHAR(10),
    profession        VARCHAR(150),
    residance         VARCHAR(255),
    adresse_livraison VARCHAR(255),
    ville_livraison   VARCHAR(100),
    destinataire      VARCHAR(20)                   -- « utilisateur » ou « autre »
);

CREATE TABLE demande_variete_details (            -- champs propres à chaque type d'acte (parents, conjoint…)
    id           SERIAL PRIMARY KEY,
    id_demande   INTEGER REFERENCES demandedocument (iddemande) ON DELETE CASCADE,
    type_variete VARCHAR(50),
    donnees      TEXT                               -- JSON des informations saisies
);

CREATE TABLE historiquestatut (
    id                SERIAL PRIMARY KEY,
    iddemande         INTEGER REFERENCES demandedocument (iddemande) ON DELETE CASCADE,
    statut            VARCHAR(30),
    commentaire       TEXT,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table utilisée uniquement par la signature (SignerDemandeServlet). Le code y écrit au lieu de
-- demandedocument : c'était probablement une erreur de la version 1, conservée ici pour que la page fonctionne.
CREATE TABLE demandes (
    id_demande     INTEGER PRIMARY KEY,
    status         VARCHAR(30),
    date_demande   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_signature TIMESTAMP
);

-- ------------------------------------------------------------------ vue de recherche (DetailDemandeServlet)
CREATE VIEW recherche_documents AS
SELECT d.iddocument,
       n.nom            AS nature_document,
       v.nom            AS type_document,
       c.nom            AS commune,
       d.dateevenement,
       d.lieuevenement,
       n.description,
       n.tarif_base,
       n.duree_validite,
       COALESCE(v.mots_cles, '') AS mots_cles
FROM documentetatcivil d
LEFT JOIN nature   n ON n.idnature  = d.idnature
LEFT JOIN varietes v ON v.idvariete = n.idvariete
LEFT JOIN communes c ON c.idcommune = d.idcommune;

-- =====================================================================================================
--  Données de départ (à adapter)
-- =====================================================================================================
INSERT INTO varietes (nom, description, mots_cles, conjoint, temoins, parents, medecin, mentionmarginale, causedeces) VALUES
 ('Naissance', 'Actes de naissance', 'naissance',  FALSE, TRUE, TRUE,  TRUE,  TRUE, FALSE),
 ('Mariage',   'Actes de mariage',   'mariage',    TRUE,  TRUE, TRUE,  FALSE, TRUE, FALSE),
 ('Divorce',   'Actes de divorce',   'divorce',    TRUE,  FALSE, FALSE, FALSE, TRUE, FALSE),
 ('Décès',     'Actes de décès',     'deces,décès', FALSE, TRUE, TRUE,  TRUE,  TRUE, TRUE);

INSERT INTO nature (nom, description, tarif_base, duree_validite, delai_traitement, idvariete) VALUES
 ('Extrait d''acte de naissance',         'État civil de la personne et filiation', 1000, 3, 2, 1),
 ('Copie littérale d''acte de naissance', 'Reproduction intégrale de l''acte',      1500, 3, 3, 1),
 ('Extrait d''acte de mariage',           'Identité des époux et date du mariage',   1500, 3, 3, 2),
 ('Copie littérale d''acte de mariage',   'Reproduction intégrale de l''acte',      2000, 3, 4, 2),
 ('Extrait d''acte de divorce',           'Mention du divorce prononcé',            1500, 3, 3, 3),
 ('Extrait d''acte de décès',             'Date et lieu du décès',                  1000, 3, 2, 4),
 ('Copie littérale d''acte de décès',     'Reproduction intégrale de l''acte',      1500, 3, 3, 4);

INSERT INTO motif_demande (idnature, libelle, description, documents_requis)
SELECT n.idnature, m.libelle, NULL, m.pieces
FROM nature n
CROSS JOIN (VALUES
  ('Constitution de dossier administratif', 'Pièce d''identité'),
  ('Inscription scolaire ou universitaire', 'Pièce d''identité'),
  ('Demande de passeport ou de carte d''identité', 'Pièce d''identité'),
  ('Autre', NULL)) AS m (libelle, pieces);

INSERT INTO communes (nom, ville) VALUES
 ('Dakar Plateau', 'Dakar'), ('Médina', 'Dakar'), ('Thiès Nord', 'Thiès'),
 ('Saint-Louis', 'Saint-Louis'), ('Kaolack', 'Kaolack'), ('Ziguinchor', 'Ziguinchor');

INSERT INTO role (nom, description) VALUES
 ('utilisateur', 'Citoyen : dépose et suit ses demandes'),
 ('delegue',     'Examine les demandes'),
 ('officier',    'Signe les documents'),
 ('admin',       'Administre la plateforme');

-- Aucun compte n'est créé ici : les mots de passe sont hachés en BCrypt par l'application.
-- Créez votre compte avec la page d'inscription, puis donnez-lui le rôle voulu :
--     UPDATE compte SET role = 'admin' WHERE email = 'votre@email';
