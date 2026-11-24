# E-Civil Sénégal

Plateforme de demande en ligne de documents d'état civil : extraits et copies littérales d'actes de
naissance, de mariage, de divorce et de décès, avec paiement en ligne (PayDunya) et documents signés
vérifiables par QR code.

Architecture : **microservices** communiquant par **événements** (RabbitMQ), chacun construit selon
l'**architecture hexagonale**, avec **Keycloak** pour l'identité et une **passerelle** comme point d'entrée unique.

## Vue d'ensemble

```mermaid
flowchart LR
    N[Navigateur<br/>citoyens · agents] --> G[gateway<br/>:8080]
    PD[PayDunya] -->|notification IPN| G
    N <-->|connexion OIDC| K[Keycloak<br/>:8180]
    G --> P[portail-web]
    G --> D[demande-service]
    G --> PA[paiement-service]
    G --> R[registre-service]
    G --> DOC[document-service]
    G --> H[habilitation-service]
    H -->|API d'administration| KC[(Keycloak)]
    P -->|API + jeton utilisateur| G
    D -->|jeton de service| R
    D -->|jeton de service| DOC
    DOC -->|jeton de service| R
    PA -->|facture, confirmation| PD
    D & PA & R -. événements .-> MQ[(RabbitMQ)]
    MQ -. événements .-> D & PA & NO[notification-service]
```

| Service | Rôle | Base | Port |
|---|---|---|---|
| `gateway` | Point d'entrée unique (Spring Cloud Gateway) | — | 8080 |
| `portail-web` | Pages des citoyens et des agents (Thymeleaf), client OIDC de Keycloak | — | 8090 |
| `demande-service` | Catalogue, dépôt et instruction des demandes, cycle de vie | `demande` | 8081 |
| `paiement-service` | Paiement PayDunya (Wave, Orange Money, Free Money, carte) | `paiement` | 8082 |
| `registre-service` | Registre national des actes : transcription, validation, recherche | `registre` | 8083 |
| `document-service` | Signature, PDF, vérification publique d'authenticité | `document` | 8084 |
| `notification-service` | Emails aux citoyens à chaque étape | `notification` | 8085 |
| `habilitation-service` | Utilisateurs, profils, rôles, permissions et journal (administre Keycloak) | `habilitation` | 8086 |

Chaque service possède **sa propre base** : aucun ne lit les données d'un autre, ils communiquent
uniquement par API et par événements.

## Architecture hexagonale

Chaque service est découpé de la même façon :

```
sn.etatcivil.<service>
├── domaine/           Java pur : entités, règles de gestion, machine à états (aucun framework)
├── application/
│   ├── port/entree/   cas d'usage offerts (interfaces)
│   ├── port/sortie/   besoins du service : stockage, autres services, événements (interfaces)
│   └── service/       implémentation des cas d'usage, toujours sans framework
├── adaptateur/
│   ├── entree/        REST, écoute RabbitMQ, tâches planifiées
│   └── sortie/        JPA, clients HTTP, PayDunya, outbox, email, PDF
└── configuration/     assemblage (Spring), transactions, sécurité
```

Ces règles sont **vérifiées à chaque build** par des tests ArchUnit (`socle/socle-tests`) : le domaine ne
dépend que de Java, l'application que du domaine, et les adaptateurs ne se connaissent pas entre eux.
Grâce aux ports, les cas d'usage se testent avec de simples implémentations en mémoire, sans base ni réseau.

Le **socle** commun contient :
- `socle-evenements` : les contrats des événements échangés ;
- `socle-messagerie` : l'outbox transactionnel et la consommation idempotente ;
- `socle-securite` : la lecture des rôles Keycloak et les jetons de service ;
- `socle-tests` : les règles ArchUnit.

## Événements et fiabilité

| Événement | Publié par | Écouté par | Effet |
|---|---|---|---|
| `demande.statut` | demande-service | notification-service, paiement-service | email au citoyen ; la demande devient payable |
| `paiement.confirme` | paiement-service | demande-service | la demande passe « payée », l'acte est recherché aussitôt |
| `registre.acte-valide` | registre-service | demande-service | les demandes dont l'acte manquait sont relancées automatiquement |

- **Outbox transactionnel** : un événement est enregistré dans la même transaction que la donnée métier,
  puis relayé vers RabbitMQ. Si le broker est arrêté, rien n'est perdu.
- **Consommation idempotente** : chaque événement a un identifiant, et un message reçu deux fois n'a
  d'effet qu'une fois.
- **File d'erreurs** : un message qui échoue après plusieurs tentatives est rangé dans `<service>.erreurs`
  au lieu d'être perdu.

## Cycle de vie d'une demande

```mermaid
stateDiagram-v2
    [*] --> SOUMISE : le citoyen dépose
    SOUMISE --> A_PAYER : le délégué valide
    SOUMISE --> REJETEE
    A_PAYER --> PAYEE : PayDunya confirme le paiement
    PAYEE --> ACTE_TROUVE : recherche dans le registre
    PAYEE --> ACTE_INTROUVABLE
    ACTE_INTROUVABLE --> ACTE_TROUVE : l'acte est transcrit et validé<br/>(relance automatique) ou rattaché
    ACTE_INTROUVABLE --> REJETEE : le chargé de renseignement conclut<br/>que l'acte n'existe pas
    ACTE_TROUVE --> SIGNEE : l'officier signe
    ACTE_TROUVE --> ACTE_INTROUVABLE : l'officier conteste l'acte
    SIGNEE --> LIVREE : format papier expédié
    SOUMISE --> ANNULEE
    A_PAYER --> ANNULEE
```

### Renseignement : quand l'acte manque au registre national

Une grande partie des actes n'existe encore que sur papier. Quand la recherche automatique échoue, la demande
passe « Acte non retrouvé » et le **chargé de renseignement** ouvre son dossier depuis la fiche de la demande :

- les **registres papier** à consulter lui sont proposés : ceux du type d'acte, pour l'année de l'événement et
  les deux années voisines, dans tous les centres, avec leur exemplaire (centre ou greffe), leur état et s'ils
  sont numérisés ;
- il **consigne** chaque registre consulté et ce qu'il y a constaté (aucun acte, acte présent, page illisible,
  registre manquant), avec le folio et une note ; il peut marquer le dossier **urgent** ;
- s'il trouve l'acte, il le transcrit puis le rattache : le dossier est clos « acte retrouvé » ;
- sinon il **conclut** : la demande est rejetée avec son explication (registres consultés, jugement
  d'autorisation d'inscription à demander au tribunal) et le citoyen est prévenu.

Les **centres d'état civil** (principal par commune, secondaires rattachés, consulaires) et leurs **registres
papier** (un par type d'acte et par année, en deux exemplaires) se gèrent dans *Espace agent › Centres et
registres papier*. La file des actes non retrouvés affiche les recherches en cours, urgentes, abouties et
infructueuses.

### Habilitations : utilisateurs, profils, rôles, permissions

Chaque service vérifie une **permission** sur chaque action (15 permissions : `DEMANDES_VALIDER`,
`DOCUMENTS_SIGNER`, `REGISTRE_VALIDER`…). Les permissions sont regroupées en **rôles**, les rôles en
**profils**, et chaque utilisateur reçoit un ou plusieurs profils. Tout se gère dans le portail,
*Administration* : comptes des agents, profils, rôles, matrice rôles × permissions, journal des opérations.

| Profil de départ | Rôles | Ce qu'il permet |
|---|---|---|
| Citoyen (donné à chaque inscription) | Citoyen | déposer, payer, suivre, télécharger ses demandes |
| Délégué de commune | Délégué | examiner les demandes, expédier les documents papier |
| Agent du registre | Agent du registre | transcrire les actes, traiter les actes non retrouvés |
| Chargé de renseignement | Agent du registre | chercher dans les registres papier les actes non retrouvés |
| Officier de l'état civil | Officier | valider les transcriptions, contrôler et signer les documents |
| Chef de centre | Officier + Délégué | les deux |
| Administrateur | Administrateur | tout, sauf déposer des demandes |

La plateforme refuse toute modification qui la laisserait sans administrateur. Détails, règles et
correspondance avec Keycloak : [`docs/memoire/roles-permissions.md`](docs/memoire/roles-permissions.md).
Les comptes techniques des services portent le rôle `SERVICE`.

## Catalogue des documents

Le catalogue suit le modèle **Nature / Variété** de la version d'origine : **15 natures** (familles) regroupent
**46 variétés** (documents délivrables). Chaque variété a un **modèle** (ce que contient le document), l'acte
sur lequel elle s'appuie, les rubriques du formulaire de demande, un tarif, un délai, une **durée de validité**,
des mots-clés, les **pièces à fournir**, et, pour une copie, le document **original** qu'elle reproduit.

| Nature | Documents |
|---|---|
| Naissance | Acte original, extrait (avec ou sans filiation), certificat de naissance |
| Reconnaissance | Copie d'acte, enfant naturel, anticipée, paternelle, maternelle, certificat de reconnaissance |
| Mariage | Acte original, extrait, certificat de mariage |
| Divorce | Extrait d'acte de divorce |
| Décès | Acte original, extrait, certificat de décès |
| Copie | Copies littérales de naissance, de mariage et de décès (copies de l'original) |
| Jugement supplétif, Transcription | Naissance, mariage, décès |
| Mention marginale | Extraits de mention de divorce et de légitimation |
| Livret de famille | Livret, duplicata |
| Célibat, Vie individuelle | Célibat (mariage civil, religieux, adoption) ; vie (retraite, pension, succession, vie individuelle) |
| Nationalité, Domicile, Coutume | Nationalité (3), non-appartenance, domicile (3), coutume, capacité matrimoniale |

**Validité.** Un document administratif est valable **3 mois** (90 jours) à compter de sa signature : la date
limite est imprimée sur le document et contrôlée par la page de vérification. Le livret de famille et son
duplicata restent sans limite. L'administrateur peut changer la durée de chaque variété.

**Vérification d'identité.** Pour les documents sensibles (actes originaux, copies intégrales, livret de famille,
extraits de mention, certificats de situation, de nationalité, de domicile et sur comparution), le demandeur joint
la photo de sa pièce d'identité (recto et verso de la carte, ou page du passeport) et un selfie, pris avec la
caméra ou importé. Les images sont réencodées (métadonnées retirées), visibles seulement des agents qui valident,
signent ou supervisent, et effacées 30 jours après la clôture de la demande. Le délégué atteste avoir comparé
les photos avant de valider. Les extraits et les certificats d'événement ne demandent rien de plus.

**Langues.** Tout le portail est traduit en français, wolof et anglais (choix dans *Mon compte* ou en pied de
page, paramètre `?lang=`). Les noms des documents, les libellés modifiables par l'administrateur et les messages
d'erreur des services restent en français. La traduction wolof est à faire relire par un locuteur natif.

L'administration gère natures, documents, motifs (communs ou propres à un document, avec leurs pièces) et le
référentiel de la diaspora (pays, missions diplomatiques). Les certificats de non-appartenance et de coutume
sont établis **sur comparution** devant l'officier, sans acte du registre. Un Sénégalais de l'étranger peut
retirer un document papier à la **mission diplomatique** qui couvre son pays de résidence.

Ce que la version d'origine faisait et comment chaque élément a été rétabli : [`docs/comparaison-v1.md`](docs/comparaison-v1.md).

- **Origine de l'acte.** Un acte est dressé sur déclaration ordinaire, en exécution d'un **jugement supplétif**
  ou par **transcription** d'un acte étranger ; l'agent en saisit les références. Les documents « jugement
  supplétif » et « transcription » ne retrouvent que les actes de cette origine.
- **Registre.** Les mentions marginales (mariage, divorce, légitimation…) sont rattachées à l'acte qu'elles
  modifient ; chaque acte a son journal (saisie, corrections champ par champ, validation, mentions, avec
  l'agent) ; la commune se choisit dans le référentiel (66 communes, 14 régions).
- **Certificats.** registre-service établit la situation civile d'une personne : ses mariages (dissous ou non
  par un divorce ou le décès du conjoint) et son décès. Le certificat de célibat exige qu'aucun mariage ne
  soit inscrit, la capacité matrimoniale qu'aucun mariage ne soit en cours, le certificat de vie qu'aucun décès
  ne soit inscrit. Dans le cas contraire, la demande n'est jamais refusée automatiquement : un agent vérifie
  (homonymie, option de polygamie…), puis rattache l'acte ou rejette la demande. Le constat fait dans le
  registre est imprimé sur le certificat et couvert par l'empreinte.

**Authenticité.** À la signature, document-service relit l'acte dans le registre, en fige le contenu, en
calcule l'empreinte SHA-256 et lui attribue un code de vérification unique, imprimé avec un QR code sur
le PDF. Sur `/verification/{code}`, n'importe qui peut vérifier le document. Toute modification du contenu
enregistré est détectée. Il s'agit d'une signature applicative, pas d'une signature électronique qualifiée.

## Démarrer

> Première installation ? Suivez le **[guide pas à pas](docs/guide-demarrage.md)** : outils à installer,
> fichier `.env`, comptes agents, PayDunya en mode test, premier parcours et problèmes fréquents.

**Prérequis :** Java 17 ou plus récent et Docker Desktop (Maven est fourni par le wrapper `mvnw`).

1. Copier `.env.example` en `.env`, puis remplir chaque valeur (mots de passe, secrets, PayDunya).
   Le fichier `.env` est ignoré par git : aucun secret n'est écrit dans le code.
2. Construire les services :

   ```bash
   ./mvnw -DskipTests package
   ```

   (Windows : `.\mvnw.cmd -DskipTests package`.)

3. Tout démarrer :

   ```bash
   docker compose up --build
   ```

4. Ouvrir <http://localhost:8080>. Le compte administrateur (`ECIVIL_ADMIN_EMAIL`) doit changer son mot de
   passe à la première connexion. Les comptes agents se créent dans *Administration › Utilisateurs ›
   Nouvel agent*, ou d'un coup pour la démonstration : `./scripts/agents-demo.sh <mot de passe>`.

| Adresse | Contenu |
|---|---|
| <http://localhost:8080> | Portail E-Civil (via la passerelle) |
| <http://localhost:8180> | Keycloak (console d'administration) |
| <http://localhost:15672> | Console RabbitMQ (pour voir passer les événements) |

### Thème Keycloak

Les pages de connexion, d'inscription et l'espace « Mon compte » de Keycloak portent le style E-Civil
(`infra/keycloak-theme/ecivil`). Le thème est monté dans le conteneur Keycloak et appliqué par défaut
(`KC_SPI_THEME_DEFAULT`), y compris sur un realm déjà importé ; la console d'administration garde l'apparence
d'origine de Keycloak.

### Mode démonstration

Avec `ECIVIL_MODE=demo` dans `.env`, registre-service ajoute au registre national 16 actes **fictifs**
(naissances, mariages, divorce, décès, reconnaissance, un acte par jugement supplétif, une transcription, dont
2 encore à valider), pour présenter la plateforme sans saisie préalable. Par exemple, une demande d'extrait de naissance pour **Ousmane Sarr**, né le 22/07/1995 à Thiès,
de Abdoulaye Sarr et Khady Faye, retrouve aussitôt l'acte `N-1995-000001` après le paiement.

Pour les autres documents : un certificat de célibat pour **Mame Diarra Gueye** (née le 09/01/2003 à
Saint-Louis), refusé pour **Babacar Mbaye** (marié) ; un livret de famille pour Babacar Mbaye et Seynabou Diouf
(deux enfants) ; un jugement supplétif de naissance pour **Mamadou Saliou Baldé** ; une transcription de mariage
pour **Aliou Faye** et Bineta Ndiaye ; une copie de reconnaissance pour **Fatou Gueye** (née le 05/12/2018).

Le jeu est chargé une seule fois, à la création de la base (`db/demo/V1000__actes_demo.sql`) : pour revenir à un
registre vide, repasser en `ECIVIL_MODE=production` et recréer les volumes (`docker compose down -v`).

### Paiement PayDunya

Créer une application en **mode test** sur <https://app.paydunya.com>, puis recopier la clé principale, la
clé privée et le token dans `.env`. Une demande n'est marquée payée que lorsque l'API PayDunya confirme le
statut et le montant exact. La notification IPN (`/api/paiements/paydunya/ipn`) n'est joignable que si la
plateforme est accessible depuis Internet (`ECIVIL_URL_PUBLIQUE`) ; en local, la confirmation se fait au
retour du navigateur.

### Emails

Renseigner `MAIL_HOST`, `MAIL_USERNAME` et `MAIL_PASSWORD` (pour Gmail : un mot de passe d'application).
Sans `MAIL_HOST`, notification-service journalise les messages sans les envoyer.

## Tests

```bash
./mvnw test
```

Les tests n'ont besoin ni de Docker ni du réseau (base H2 en mémoire, jetons Keycloak simulés). Pour
chaque service, ils couvrent le domaine en Java pur, les cas d'usage avec des ports en mémoire, l'API REST
avec le contrôle des rôles, l'idempotence des événements, l'outbox et les règles d'architecture.

## Historique

| Version | Architecture | Commit |
|---|---|---|
| 1 | Servlets/JSP + ServeurCentral (code du mémoire) | `28d59f4` |
| 2 | Monolithe Spring Boot + registre, simulations supprimées | `b58ab4b` |
| 3 | Microservices hexagonaux, événements, Keycloak (version actuelle) | — |

La génération PDF utilise iText 8 (licence AGPL). Une diffusion commerciale nécessiterait une licence iText
ou une bibliothèque alternative (par exemple OpenPDF).
