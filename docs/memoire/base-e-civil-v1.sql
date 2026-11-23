-- =====================================================================================================
--  Base « E-Civil » de la version 1 (code du mémoire : Servlets/JSP, commit 28d59f4)
--
--  Reconstruite à partir des requêtes SQL des DAO et des modèles Java, la base d'origine ayant été perdue,
--  puis complétée avec le MCD PowerAMC du 10/08/2025 et les données d'origine (variétés, natures, tarifs,
--  communes, permissions, missions diplomatiques, registre central). Quand le code et le MCD diffèrent, le code l'emporte,
--  puisque c'est lui qui interroge la base :
--    MCD                         code (retenu)
--    commune                     communes
--    permision                   permission
--    Association_18              role_permissions (role_id, permission_id)
--    paiement.status INT2        texte : 'en attente', 'payé'…
--    documentEtatCivil.iddocument CHAR(10)   entier (RETURNING iddocument)
--    demandeDocument (8 colonnes) 21 colonnes (personne concernée, livraison, nature, motif…)
--    nature (4 colonnes)         + tarif_base, duree_validite, delai_traitement
--  Les tables ajoutées par le code après le MCD sont : motif_demande, historiquestatut,
--  demande_variete_details, demandes et la vue recherche_documents.
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

-- ------------------------------------------------------------------ diaspora (MCD PowerAMC du 10/08/2025)
-- Tables du modèle conceptuel d'origine. Les classes Java existent (MissionDiplomatique, Pays, Ville,
-- Zonedecompetence, Typemission) mais aucun DAO ne les utilise encore : la partie diaspora était conçue,
-- pas branchée.
CREATE TABLE typemission (
    idtype  SERIAL PRIMARY KEY,
    libelle VARCHAR(254)                            -- ambassade, consulat…
);

CREATE TABLE missiondiplomatique (
    idmission      SERIAL PRIMARY KEY,
    idtype         INTEGER REFERENCES typemission (idtype),   -- ambassade, consulat…
    pays_hote      VARCHAR(254),
    nom            VARCHAR(254),
    zonecompetence VARCHAR(254),
    adresse        VARCHAR(254),
    telephone      VARCHAR(254),
    email          VARCHAR(254)
);

CREATE TABLE zonedecompetence (
    idzone      SERIAL PRIMARY KEY,
    nom_zone    VARCHAR(254),
    description VARCHAR(254)
);

CREATE TABLE pays (
    idpays SERIAL PRIMARY KEY,
    idzone    INTEGER REFERENCES zonedecompetence (idzone),
    nom       VARCHAR(254),
    code_iso  CHAR(3),
    continent VARCHAR(50),
    capitale  VARCHAR(100)
);

CREATE TABLE ville (
    idville    SERIAL PRIMARY KEY,
    idmission  INTEGER REFERENCES missiondiplomatique (idmission),
    idpays     INTEGER REFERENCES pays (idpays),
    nom        VARCHAR(254),
    codepostal VARCHAR(254)
);

CREATE TABLE mission_zone (                         -- « Association_27 » du MCD
    idmission INTEGER REFERENCES missiondiplomatique (idmission),
    idzone    INTEGER REFERENCES zonedecompetence (idzone),
    PRIMARY KEY (idmission, idzone)
);

CREATE TABLE demande_commune (                      -- « Association_23 » du MCD (non utilisée par le code)
    iddemande INTEGER REFERENCES demandedocument (iddemande),
    idcommune INTEGER REFERENCES communes (idcommune),
    PRIMARY KEY (iddemande, idcommune)
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

-- ------------------------------------------------------------------ serveur de données (registre central)
CREATE TABLE registre_etat_civil (
    idregistre         SERIAL PRIMARY KEY,
    idcommune          INTEGER REFERENCES communes (idcommune),
    idnature           INTEGER REFERENCES nature (idnature),
    annee_registre     INTEGER NOT NULL,
    numero_registre    VARCHAR(50) NOT NULL,
    numero_acte        INTEGER NOT NULL,
    date_etablissement DATE NOT NULL,
    statut             VARCHAR(20) DEFAULT 'saisi' CHECK (statut IN ('saisi', 'verifie', 'valide', 'archive')),
    -- personne concernée (naissance, mariage, décès)
    nom_personne       VARCHAR(254),
    prenom_personne    VARCHAR(254),
    date_evenement     DATE,
    lieu_evenement     VARCHAR(254),
    details_acte       JSONB,                       -- informations propres au type d'acte
    -- gestion
    saisie_par         INTEGER REFERENCES utilisateur (idutilisateur),
    date_saisie        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verification_par   INTEGER REFERENCES utilisateur (idutilisateur),
    date_verification  TIMESTAMP,
    validation_par     INTEGER REFERENCES utilisateur (idutilisateur),
    date_validation    TIMESTAMP,
    -- synchronisation avec le serveur central
    sync_serveur_central BOOLEAN DEFAULT FALSE,
    date_sync_serveur    TIMESTAMP,
    id_serveur_central   VARCHAR(100),
    CONSTRAINT uk_registre_unique UNIQUE (idcommune, annee_registre, numero_registre, numero_acte)
);
CREATE INDEX idx_registre_commune    ON registre_etat_civil (idcommune);
CREATE INDEX idx_registre_nature     ON registre_etat_civil (idnature);
CREATE INDEX idx_registre_date       ON registre_etat_civil (date_evenement);
CREATE INDEX idx_registre_nom_prenom ON registre_etat_civil (nom_personne, prenom_personne);
CREATE INDEX idx_registre_sync       ON registre_etat_civil (sync_serveur_central);

CREATE TABLE audit_registre (                       -- trace de toutes les modifications du registre
    idaudit         SERIAL PRIMARY KEY,
    idregistre      INTEGER REFERENCES registre_etat_civil (idregistre),
    action          VARCHAR(20) NOT NULL CHECK (action IN ('creation', 'modification', 'validation', 'correction')),
    ancienne_valeur JSONB,
    nouvelle_valeur JSONB,
    date_action     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur  INTEGER REFERENCES utilisateur (idutilisateur),
    ip_adresse      INET,
    user_agent      TEXT
);

CREATE TABLE generation_documents (                 -- documents officiels produits depuis le registre
    iddocument           SERIAL PRIMARY KEY,
    idregistre           INTEGER REFERENCES registre_etat_civil (idregistre),
    idnature             INTEGER REFERENCES nature (idnature),
    type_document        VARCHAR(50) NOT NULL,      -- extrait, copie_integrale, certificat
    format               VARCHAR(20) DEFAULT 'pdf' CHECK (format IN ('pdf', 'html', 'xml')),
    contenu_document     TEXT,
    hash_document        VARCHAR(64),               -- empreinte pour l'intégrité
    qr_code              TEXT,
    genere_par           INTEGER REFERENCES utilisateur (idutilisateur),
    date_generation      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_expiration      DATE,
    nombre_consultations INTEGER DEFAULT 0,
    statut               VARCHAR(20) DEFAULT 'actif' CHECK (statut IN ('actif', 'expire', 'revoke'))
);

-- Génère un document officiel à partir d'un acte du registre
CREATE OR REPLACE FUNCTION generer_document(p_idregistre INT4, p_type_document VARCHAR, p_idutilisateur INT4)
RETURNS INT4 AS $$
DECLARE
    v_iddocument INT4;
    v_registre   RECORD;
    v_contenu    TEXT;
BEGIN
    SELECT * INTO v_registre FROM registre_etat_civil WHERE idregistre = p_idregistre;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Registre non trouvé';
    END IF;
    v_contenu := format('
        DOCUMENT OFFICIEL - RÉPUBLIQUE DU SÉNÉGAL
        Type: %s
        Registre: %s/%s - Acte n°%s
        Nom: %s %s
        Date événement: %s
        Lieu: %s
        Date établissement: %s
        Numéro sécurisé: %s
    ', p_type_document, v_registre.annee_registre, v_registre.numero_registre,
       v_registre.numero_acte, v_registre.nom_personne, v_registre.prenom_personne,
       v_registre.date_evenement, v_registre.lieu_evenement, v_registre.date_etablissement,
       md5(v_registre.idregistre::text || p_type_document));
    INSERT INTO generation_documents (idregistre, idnature, type_document, contenu_document, hash_document, genere_par, date_expiration)
    VALUES (p_idregistre, v_registre.idnature, p_type_document, v_contenu, md5(v_contenu), p_idutilisateur,
            CURRENT_DATE + INTERVAL '3 months')
    RETURNING iddocument INTO v_iddocument;
    RETURN v_iddocument;
END;
$$ LANGUAGE plpgsql;

-- Marque les actes validés comme synchronisés avec le serveur central et renvoie un bilan
CREATE OR REPLACE FUNCTION synchroniser_serveur_central()
RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    UPDATE registre_etat_civil
    SET sync_serveur_central = TRUE, date_sync_serveur = CURRENT_TIMESTAMP
    WHERE statut = 'valide' AND sync_serveur_central = FALSE;
    SELECT json_build_object(
        'registres_synchronises', (SELECT COUNT(*) FROM registre_etat_civil WHERE sync_serveur_central = TRUE),
        'registres_en_attente',   (SELECT COUNT(*) FROM registre_etat_civil WHERE sync_serveur_central = FALSE),
        'date_synchronisation',   CURRENT_TIMESTAMP
    ) INTO result;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================================================
--  Données d'origine de la base E-Civil
-- =====================================================================================================
-- Variétés de documents (19)
INSERT INTO varietes (nom, description, conjoint, temoins, parents, medecin, mentionmarginale, causedeces, mots_cles) VALUES
 ('Acte de Naissance', 'Document attestant de la naissance d''une personne', FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, 'naissance, enfant, parent, filiation, état civil'),
 ('Acte de Mariage', 'Document attestant de l''union matrimoniale', TRUE, TRUE, FALSE, FALSE, TRUE, FALSE, 'mariage, union, époux, épouse, famille'),
 ('Acte de Décès', 'Document attestant du décès d''une personne', FALSE, FALSE, FALSE, TRUE, FALSE, TRUE, 'décès, mort, défunt, héritage, succession'),
 ('Extrait de Naissance', 'Version simplifiée de l''acte de naissance', FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, 'extrait, simplifié, administratif, copie'),
 ('Extrait de Mariage', 'Version simplifiée de l''acte de mariage', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL),
 ('Extrait de Décès', 'Version simplifiée de l''acte de décès', FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, NULL),
 ('Copie Intégrale de Naissance', 'Copie complète de l''acte de naissance', FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, 'intégral, complet, copie, mentions'),
 ('Copie Intégrale de Mariage', 'Copie complète de l''acte de mariage', TRUE, TRUE, FALSE, FALSE, TRUE, FALSE, NULL),
 ('Livret de Famille', 'Document regroupant les actes familiaux', TRUE, FALSE, TRUE, FALSE, TRUE, TRUE, 'livret, famille, regroupement, actes'),
 ('Certificat de Célibat', 'Document attestant de l''état célibataire d''une personne', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'célibat, mariage, état civil, marital'),
 ('Certificat de Vie', 'Document attestant qu''une personne est en vie à une date donnée', FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, 'vie, existence, pension, retraite'),
 ('Certificat de Nationalité', 'Document attestant de la nationalité d''une personne', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'nationalité, citoyenneté, naturalisation'),
 ('Certificat de Domicile', 'Document attestant du lieu de résidence', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'domicile, résidence, adresse, habitation'),
 ('Acte de Reconnaissance', 'Document attestant la reconnaissance d''un enfant', FALSE, FALSE, TRUE, FALSE, TRUE, FALSE, 'reconnaissance, enfant, paternité, maternité'),
 ('Jugement Supplétif', 'Document judiciaire remplaçant un acte manquant', FALSE, TRUE, TRUE, FALSE, TRUE, FALSE, 'jugement, supplétif, tribunal, manquant'),
 ('Transcription d''Acte', 'Document de transcription d''un acte étranger', FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, 'transcription, étranger, international, consulat'),
 ('Certificat de Non-Appartenance', 'Certificat de non-appartenance à une nationalité', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL),
 ('Certificat de Coutume', 'Document attestant des règles coutumières applicables', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL),
 ('Certificat de Capacité Matrimoniale', 'Document attestant de la capacité à contracter mariage', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL);

-- Natures de documents (36), avec durée de validité (jours), tarif (FCFA) et délai de traitement (jours)
INSERT INTO nature (idvariete, description, nom, duree_validite, tarif_base, delai_traitement) VALUES
 (1, 'Acte original de naissance avec mentions marginales', 'Acte Naissance Original', 180, 2000, 2),
 (4, 'Extrait d''acte de naissance incluant la filiation', 'Extrait Naissance avec Filiation', 90, 1000, 1),
 (4, 'Extrait d''acte de naissance sans informations de filiation', 'Extrait Naissance sans Filiation', 90, 1000, 1),
 (7, 'Copie intégrale avec mentions', 'Copie Intégrale Naissance', 365, 5000, 5),
 (2, 'Acte original de mariage', 'Acte Mariage Original', 180, 2000, 2),
 (5, 'Extrait d''acte de mariage', 'Extrait Mariage', 90, 1000, 1),
 (8, 'Copie intégrale de mariage', 'Copie Intégrale Mariage', 365, 5000, 5),
 (3, 'Acte original de décès', 'Acte Décès Original', 180, 2000, 2),
 (6, 'Extrait d''acte de décès', 'Extrait Décès', 90, 1000, 1),
 (9, 'Livret de famille', 'Livret de Famille', 365, 5000, 5),
 (9, 'Duplicata de livret', 'Duplicata Livret', NULL, NULL, NULL),
 (10, 'Certificat de célibat pour mariage civil', 'Certificat Célibat Mariage Civil', 30, 500, 1),
 (10, 'Certificat de célibat pour mariage religieux', 'Certificat Célibat Mariage Religieux', 30, 500, 1),
 (10, 'Certificat de célibat pour adoption', 'Certificat Célibat Adoption', 30, 500, 1),
 (11, 'Certificat de vie pour retraite', 'Certificat Vie Retraite', 30, 500, 1),
 (11, 'Certificat de vie pour pension', 'Certificat Vie Pension', 30, 500, 1),
 (11, 'Certificat de vie pour succession', 'Certificat Vie Succession', 30, 500, 1),
 (12, 'Certificat de nationalité sénégalaise', 'Certificat Nationalité Sénégalaise', 30, 500, 1),
 (12, 'Certificat de nationalité pour mariage', 'Certificat Nationalité Mariage', 30, 500, 1),
 (12, 'Certificat de nationalité pour naturalisation', 'Certificat Nationalité Naturalisation', 30, 500, 1),
 (13, 'Certificat de domicile pour élections', 'Certificat Domicile Élections', 30, 500, 1),
 (13, 'Certificat de domicile pour scolarité', 'Certificat Domicile Scolarité', 30, 500, 1),
 (13, 'Certificat de domicile pour démarches administratives', 'Certificat Domicile Administratif', 30, 500, 1),
 (14, 'Reconnaissance d''enfant naturel', 'Reconnaissance Enfant Naturel', 30, 500, 1),
 (14, 'Reconnaissance anticipée', 'Reconnaissance Anticipée', 30, 500, 1),
 (14, 'Reconnaissance par le père', 'Reconnaissance Paternelle', 30, 500, 1),
 (14, 'Reconnaissance par la mère', 'Reconnaissance Maternelle', 30, 500, 1),
 (15, 'Jugement supplétif de naissance', 'Jugement Supplétif Naissance', 30, 500, 1),
 (15, 'Jugement supplétif de décès', 'Jugement Supplétif Décès', 30, 500, 1),
 (15, 'Jugement supplétif de mariage', 'Jugement Supplétif Mariage', 30, 500, 1),
 (16, 'Transcription d''acte de naissance étranger', 'Transcription Naissance Étranger', 30, 500, 1),
 (16, 'Transcription d''acte de mariage étranger', 'Transcription Mariage Étranger', 30, 500, 1),
 (16, 'Transcription d''acte de décès étranger', 'Transcription Décès Étranger', 30, 500, 1),
 (17, 'Certificat de non-appartenance à la nationalité', 'Certificat Non-Appartenance Nationalité', 30, 500, 1),
 (18, 'Certificat de coutume pour mariage mixte', 'Certificat Coutume Mariage Mixte', 30, 500, 1),
 (19, 'Certificat de capacité matrimoniale', 'Certificat Capacité Matrimoniale', 30, 500, 1);

-- Motifs de demande proposés pour chaque nature
INSERT INTO motif_demande (idnature, libelle, documents_requis)
SELECT n.idnature, m.libelle, m.pieces
FROM nature n
CROSS JOIN (VALUES
  ('Constitution de dossier administratif', 'Pièce d''identité'),
  ('Inscription scolaire ou universitaire', 'Pièce d''identité'),
  ('Demande de passeport ou de carte d''identité', 'Pièce d''identité'),
  ('Autre', NULL)) AS m (libelle, pieces);

-- Communes (14 régions)
-- Région de Dakar
INSERT INTO communes (nom, ville) VALUES
 ('Dakar-Plateau', 'Dakar'),
 ('Gorée', 'Dakar'),
 ('Guédiawaye', 'Guédiawaye'),
 ('Pikine', 'Pikine'),
 ('Rufisque', 'Rufisque'),
 ('Bargny', 'Rufisque'),
 ('Diamniadio', 'Rufisque'),
 ('Sébikhotane', 'Rufisque');
-- Région de Thiès
INSERT INTO communes (nom, ville) VALUES
 ('Thiès-Est', 'Thiès'),
 ('Thiès-Ouest', 'Thiès'),
 ('Mbour', 'Mbour'),
 ('Joal-Fadiouth', 'Mbour'),
 ('Saly-Portudal', 'Mbour'),
 ('Ngaparou', 'Mbour'),
 ('Kayar', 'Thiès');
-- Région de Kaolack
INSERT INTO communes (nom, ville) VALUES
 ('Kaolack', 'Kaolack'),
 ('Guinguinéo', 'Guinguinéo'),
 ('Kahone', 'Kaolack'),
 ('Ndoffane', 'Kaolack'),
 ('Gandiaye', 'Kaolack');
-- Région de Diourbel
INSERT INTO communes (nom, ville) VALUES
 ('Diourbel', 'Diourbel'),
 ('Bambey', 'Bambey'),
 ('Mbacké', 'Mbacké'),
 ('Touba-Mosquée', 'Mbacké');
-- Région de Fatick
INSERT INTO communes (nom, ville) VALUES
 ('Fatick', 'Fatick'),
 ('Diofior', 'Fatick'),
 ('Foundiougne', 'Fatick'),
 ('Sokone', 'Fatick'),
 ('Soum', 'Fatick');
-- Région de Kaffrine
INSERT INTO communes (nom, ville) VALUES
 ('Kaffrine', 'Kaffrine'),
 ('Nganda', 'Kaffrine'),
 ('Birkelane', 'Kaffrine'),
 ('Koungheul', 'Koungheul'),
 ('Malem Hodar', 'Malem Hodar');
-- Région de Ziguinchor
INSERT INTO communes (nom, ville) VALUES
 ('Ziguinchor', 'Ziguinchor'),
 ('Bignona', 'Bignona'),
 ('Oussouye', 'Oussouye'),
 ('Diouloulou', 'Bignona');
-- Région de Saint-Louis
INSERT INTO communes (nom, ville) VALUES
 ('Saint-Louis', 'Saint-Louis'),
 ('Richard-Toll', 'Dagana'),
 ('Dagana', 'Dagana'),
 ('Podor', 'Podor'),
 ('Rosso-Sénégal', 'Dagana');
-- Région de Matam
INSERT INTO communes (nom, ville) VALUES
 ('Matam', 'Matam'),
 ('Ourossogui', 'Matam'),
 ('Kanel', 'Kanel'),
 ('Ranérou', 'Ranérou');
-- Région de Tambacounda
INSERT INTO communes (nom, ville) VALUES
 ('Tambacounda', 'Tambacounda'),
 ('Goudiry', 'Goudiry'),
 ('Koumpentoum', 'Koumpentoum'),
 ('Bakel', 'Bakel');
-- Région de Kolda
INSERT INTO communes (nom, ville) VALUES
 ('Kolda', 'Kolda'),
 ('Médina Yoro Foulah', 'Kolda'),
 ('Vélingara', 'Kolda'),
 ('Diaobé', 'Vélingara');
-- Région de Sédhiou
INSERT INTO communes (nom, ville) VALUES
 ('Sédhiou', 'Sédhiou'),
 ('Bounkiling', 'Bounkiling'),
 ('Goudomp', 'Goudomp'),
 ('Marsassoum', 'Sédhiou');
-- Région de Kédougou
INSERT INTO communes (nom, ville) VALUES
 ('Kédougou', 'Kédougou'),
 ('Salémata', 'Salémata'),
 ('Saraya', 'Saraya');

-- Rôles (dans l'ordre des identifiants de l'association rôles / permissions)
INSERT INTO role (nom, description) VALUES
 ('Administrateur système', 'Accès complet'),
 ('Officier d''état civil', 'Validation officielle'),
 ('Délégué', 'Supervision locale'),
 ('Chargé de renseignement', 'Gestion du serveur central'),
 ('Utilisateur', 'Consultation seulement');

-- Permissions (15)
INSERT INTO permission (nom, description, avantage, interdiction) VALUES
 ('LECTURE_DOCUMENTS', 'Permission de lire les documents', 'Consultation des actes d''état civil', 'Modification interdite'),
 ('ECRITURE_DOCUMENTS', 'Permission de créer/modifier les documents', 'Saisie et mise à jour des actes', 'Suppression définitive interdite'),
 ('VALIDATION_ACTES', 'Permission de valider les actes officiels', 'Validation définitive des actes', 'Modification après validation interdite'),
 ('GESTION_UTILISATEURS', 'Permission de gérer les utilisateurs', 'Création/modification des comptes', 'Suppression admin principal interdite'),
 ('STATISTIQUES', 'Permission de consulter les statistiques', 'Accès aux rapports et tableaux de bord', 'Export massif interdit'),
 ('GESTION_PARAMETRES', 'Permission de gérer les paramètres système', 'Configuration générale de l''application', 'Modification structure base interdite'),
 ('SAISIE_REGISTRES', 'Permission de saisir les registres', 'Ajout d''actes dans le système central', 'Validation interdite'),
 ('MISE_A_JOUR_REGISTRES', 'Permission de mettre à jour les registres', 'Correction des actes en cours', 'Modification après validation interdite'),
 ('SIGNATURE_DOCUMENTS', 'Permission de signer les documents officiels', 'Signature légale des actes', 'Suppression interdite'),
 ('VALIDATION_OFFICIELLE', 'Permission de valider officiellement les actes', 'Validation avec force légale', 'Modification après validation interdite'),
 ('SUPERVISION_ACTES', 'Permission de superviser les actes', 'Contrôle des opérations locales', 'Validation interdite'),
 ('VALIDATION_DELEGUE', 'Permission de valider les actes au niveau local', 'Validation limitée (niveau délégué)', 'Signature officielle interdite'),
 ('IMPORT_DONNEES', 'Permission d''importer des données massives', 'Import depuis serveur central', 'Modification directe interdite'),
 ('EXPORT_DONNEES', 'Permission d''exporter des données', 'Export vers serveur central', 'Export complet interdit'),
 ('GESTION_SERVEUR_CENTRAL', 'Permission de gérer le serveur central', 'Synchronisation des données', 'Modification historique interdite');

-- Permissions de chaque rôle (« Association_18 » du MCD)
INSERT INTO role_permissions (role_id, permission_id) VALUES
 (1, 1),
 (1, 2),
 (1, 3),
 (1, 4),
 (1, 5),
 (1, 6),
 (1, 13),
 (1, 14),
 (1, 15),
 (2, 1),
 (2, 2),
 (2, 3),
 (2, 9),
 (2, 10),
 (2, 5),
 (3, 1),
 (3, 2),
 (3, 11),
 (3, 12),
 (3, 5),
 (4, 1),
 (4, 7),
 (4, 8),
 (4, 13),
 (4, 14),
 (4, 15),
 (4, 5),
 (5, 1);

-- Types de missions diplomatiques
INSERT INTO typemission (libelle) VALUES
 ('Ambassade'),
 ('Consulat'),
 ('Représentation diplomatique'),
 ('Mission permanente');

-- Zones de compétence
INSERT INTO zonedecompetence (nom_zone, description) VALUES
 ('Afrique de l''Ouest', 'Pays de la CEDEAO'),
 ('Afrique Centrale', 'Pays d''Afrique centrale'),
 ('Afrique du Nord', 'Pays du Maghreb et Sahara'),
 ('Europe', 'Pays européens'),
 ('Amérique du Nord', 'États-Unis, Canada'),
 ('Amérique du Sud', 'Pays d''Amérique latine'),
 ('Asie', 'Pays asiatiques'),
 ('Moyen-Orient', 'Pays du Golfe et Moyen-Orient');

-- Pays
INSERT INTO pays (idzone, nom, code_iso, continent, capitale) VALUES
 (1, 'Sénégal', 'SEN', 'Afrique', 'Dakar'),
 (1, 'Côte d''Ivoire', 'CIV', 'Afrique', 'Abidjan'),
 (1, 'Mali', 'MLI', 'Afrique', 'Bamako'),
 (1, 'Ghana', 'GHA', 'Afrique', 'Accra'),
 (4, 'France', 'FRA', 'Europe', 'Paris'),
 (4, 'Belgique', 'BEL', 'Europe', 'Bruxelles'),
 (4, 'Espagne', 'ESP', 'Europe', 'Madrid'),
 (4, 'Italie', 'ITA', 'Europe', 'Rome'),
 (5, 'États-Unis', 'USA', 'Amérique', 'Washington'),
 (5, 'Canada', 'CAN', 'Amérique', 'Ottawa'),
 (7, 'Chine', 'CHN', 'Asie', 'Pékin'),
 (7, 'Japon', 'JPN', 'Asie', 'Tokyo'),
 (8, 'Arabie Saoudite', 'SAU', 'Asie', 'Riyad'),
 (8, 'Émirats Arabes Unis', 'ARE', 'Asie', 'Abou Dabi');

-- Missions diplomatiques sénégalaises
INSERT INTO missiondiplomatique (idtype, nom, zonecompetence, adresse, telephone, email, pays_hote) VALUES
 (1, 'Ambassade du Sénégal en France', 'France et institutions européennes', '14 Avenue Robert Schuman, 75007 Paris', '+33 1 47 05 39 45', 'ambassade@senegal.fr', 'France'),
 (2, 'Consulat du Sénégal à Paris', 'Circonscription consulaire de Paris', '12 Rue de Tilsitt, 75008 Paris', '+33 1 42 27 20 10', 'consulat.paris@senegal.fr', 'France'),
 (1, 'Ambassade du Sénégal aux États-Unis', 'États-Unis et Canada', '2112 Wyoming Avenue NW, Washington, DC 20008', '+1 202-234-0540', 'embassy@senegal.us', 'États-Unis'),
 (1, 'Ambassade du Sénégal en Arabie Saoudite', 'Pays du Golfe', 'Riyadh 11693, Arabie Saoudite', '+966 11 480 5555', 'ambassade.ryad@senegal.sa', 'Arabie Saoudite'),
 (2, 'Consulat du Sénégal à New York', 'Nord-Est des États-Unis', '123 East 39th Street, New York, NY 10016', '+1 212-517-9030', 'consulat.ny@senegal.us', 'États-Unis');

-- Villes des missions
INSERT INTO ville (idmission, idpays, nom, codepostal) VALUES
 (1, 5, 'Paris', '75007'),
 (2, 5, 'Paris', '75008'),
 (3, 9, 'Washington', '20008'),
 (4, 13, 'Riyad', '11693'),
 (5, 9, 'New York', '10016');

-- Exemple de registre central rempli (saisie_par reste vide tant qu'aucun utilisateur n'existe)
INSERT INTO registre_etat_civil (idcommune, idnature, annee_registre, numero_registre, numero_acte, date_etablissement,
                                 nom_personne, prenom_personne, date_evenement, lieu_evenement, details_acte, statut) VALUES
 (1, 1, 2024, 'NAISS-2024', 1, '2024-01-15', 'DIOP', 'Moussa', '2024-01-10', 'Hôpital Principal de Dakar',
  '{"parents": {"pere": "Mamadou DIOP", "mere": "Aminata SOW"}, "heure_naissance": "08:30", "poids": "3.2kg", "sexe": "M"}', 'valide'),
 (1, 1, 2024, 'NAISS-2024', 2, '2024-01-20', 'SENE', 'Fatou', '2024-01-18', 'Clinique du Cap',
  '{"parents": {"pere": "Ibrahima SENE", "mere": "Mariama DIALLO"}, "heure_naissance": "14:15", "poids": "2.9kg", "sexe": "F"}', 'valide'),
 (2, 5, 2024, 'MARI-2024', 1, '2024-02-14', 'DIOP', 'Mamadou', '2024-02-14', 'Mairie de Guédiawaye',
  '{"conjoint": {"nom": "SOW", "prenom": "Aminata"}, "temoins": ["Jean FAYE", "Marie NDIAYE"], "regime_matrimonial": "Communauté réduite"}', 'valide'),
 (3, 8, 2024, 'DECES-2024', 1, '2024-03-10', 'FALL', 'Abdoulaye', '2024-03-08', 'Hôpital Fann',
  '{"cause_deces": "Naturelle", "age": 78, "lieu_deces": "Domicile", "medecin": "Dr. Sylvie KANE"}', 'valide');

-- Aucun compte n'est créé ici : les mots de passe sont hachés en BCrypt par l'application.
-- Créez votre compte avec la page d'inscription, puis donnez-lui le rôle voulu
-- (valeurs reconnues par le code : utilisateur, delegue, officier, admin) :
--     UPDATE compte SET role = 'admin' WHERE email = 'votre@email';
