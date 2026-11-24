# Guide : installer et lancer E-Civil sur son ordinateur

Ce guide part de zéro. Les commandes sont données pour **Windows** (PowerShell) ; les différences pour
macOS et Linux sont signalées. Compter environ une heure la première fois, surtout du téléchargement.

## 1. Ce qu'il faut

| Outil | Version | Où le trouver | Vérifier avec |
|---|---|---|---|
| Git | récente | <https://git-scm.com> (installe aussi **Git Bash**) | `git --version` |
| Java (JDK) | 17 ou plus | <https://adoptium.net> (Temurin) | `java -version` |
| Docker Desktop | récente | <https://www.docker.com/products/docker-desktop> | `docker version` |

- **Mémoire** : la plateforme lance 10 conteneurs (7 services, PostgreSQL, RabbitMQ, Keycloak). Il faut un
  ordinateur avec **8 Go de RAM** au minimum. Dans Docker Desktop → *Settings* → *Resources*, donnez au moins
  **6 Go** à Docker.
- **Windows** : Docker Desktop demande WSL 2 ; acceptez son installation et redémarrez si on vous le propose.
- **Maven** n'a pas besoin d'être installé : le projet contient le *Maven Wrapper* (`mvnw.cmd` sous Windows,
  `./mvnw` sous macOS et Linux), qui télécharge la bonne version de Maven la première fois.

Lancez Docker Desktop et attendez qu'il indique *Engine running* avant de continuer.

## 2. Récupérer le projet

```bash
git clone https://github.com/AdjaAissatou/E-Civil.git
```

```bash
cd E-Civil
```

La version microservices se trouve sur la branche de la pull request #1. Tant qu'elle n'est pas fusionnée
dans `main`, placez-vous dessus :

```bash
git checkout claude/intelligent-galileo-iej75j
```

(Une fois la pull request fusionnée, `main` suffit : `git checkout main` puis `git pull`.)

## 3. Créer le fichier `.env`

Toute la configuration secrète est dans un fichier `.env` qui reste sur votre ordinateur (il n'est jamais
envoyé sur GitHub).

```powershell
Copy-Item .env.example .env
```

(macOS / Linux : `cp .env.example .env`.) Ouvrez `.env` avec un éditeur (Bloc-notes, VS Code) et remplissez :

| Variable | Quoi mettre |
|---|---|
| `POSTGRES_PASSWORD`, `RABBITMQ_PASSWORD`, `KEYCLOAK_ADMIN_PASSWORD` | un mot de passe inventé, différent pour chacun |
| `PORTAIL_CLIENT_SECRET`, `DEMANDE_SERVICE_SECRET`, `DOCUMENT_SERVICE_SECRET` | un secret aléatoire chacun (commande ci-dessous) |
| `ECIVIL_ADMIN_EMAIL` | votre adresse : ce sera le compte administrateur E-Civil |
| `ECIVIL_ADMIN_MOT_DE_PASSE` | un mot de passe provisoire, à changer à la première connexion |
| `ECIVIL_MODE` | `demo` pour avoir des actes d'exemple dans le registre, `production` pour un registre vide |
| `PAYDUNYA_MASTER_KEY`, `PAYDUNYA_PRIVATE_KEY`, `PAYDUNYA_TOKEN` | vos clés PayDunya de test (étape 6) ; peuvent rester vides au début |
| `MAIL_…` | peuvent rester vides : les emails sont alors écrits dans les journaux au lieu d'être envoyés |

Pour générer un secret aléatoire, dans PowerShell :

```powershell
-join ((1..24) | % { '{0:x2}' -f (Get-Random -Max 256) })
```

(macOS / Linux : `openssl rand -hex 24`.) Lancez la commande trois fois, une par secret.

> Les mots de passe et secrets ne doivent contenir ni espace ni guillemet.

## 4. Construire et démarrer

Construire les 7 services (la première fois, Maven et ses dépendances se téléchargent : 5 à 10 minutes) :

```powershell
.\mvnw.cmd -DskipTests package
```

(macOS / Linux : `./mvnw -DskipTests package`.) La construction se termine par `BUILD SUCCESS`.

Tout démarrer en arrière-plan :

```bash
docker compose up --build -d
```

Le premier démarrage prend 2 à 4 minutes (téléchargement des images, création des bases, import du realm
Keycloak). Pour suivre l'état des conteneurs :

```bash
docker compose ps
```

Quand tous sont `running` (Keycloak, PostgreSQL et RabbitMQ en `healthy`), ouvrez **<http://localhost:8080>**.

| Adresse | Contenu |
|---|---|
| <http://localhost:8080> | le portail E-Civil |
| <http://localhost:8180> | la console Keycloak (serveur d'identité, rarement utile), identifiants `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` |
| <http://localhost:15672> | la console RabbitMQ (événements), identifiants `RABBITMQ_USER` / `RABBITMQ_PASSWORD` |

## 5. Créer les comptes

- **Administrateur** : cliquez sur *Connexion* avec `ECIVIL_ADMIN_EMAIL` et `ECIVIL_ADMIN_MOT_DE_PASSE`.
  Keycloak vous demande aussitôt de choisir votre vrai mot de passe.
- **Citoyen** : *Créer un compte* sur le portail ; le profil Citoyen est attribué automatiquement.
- **Agents** (délégué, agent du registre, officier) : dans **Git Bash** (macOS / Linux : le terminal), à la
  racine du projet :

  ```bash
  ./scripts/agents-demo.sh UnMotDePassePourLesAgents
  ```

  Le script crée `delegue@ecivil.sn`, `registre@ecivil.sn` et `officier@ecivil.sn` avec ce mot de passe et
  les range dans les profils *Délégué de commune*, *Agent du registre* et *Officier de l'état civil*.

  Sans le script, connecté en administrateur sur le portail : *Administration › Utilisateurs › Nouvel agent*,
  choisir le ou les profils et un mot de passe provisoire (l'agent le changera à sa première connexion).

- **Profils, rôles et permissions** : *Administration › Profils*, *Rôles* et *Permissions* (matrice
  rôles × permissions). Chaque modification est inscrite au *Journal*.

## Mettre à jour une installation existante

Depuis la racine du projet (PowerShell), plateforme démarrée ou non :

```bash
git pull
```

```bash
.\mvnw.cmd -DskipTests package
```

```bash
docker compose up --build -d
```

Les données sont conservées. Au premier démarrage :

- le service des habilitations crée sa base, les profils, rôles et permissions, range chaque compte existant
  dans le profil de son ancien rôle et ajoute les informations personnelles à la page d'inscription ;
- le catalogue passe aux natures et variétés (46 documents), les référentiels (communes, diaspora) sont créés ;
- les mentions marginales déjà saisies deviennent des mentions rattachées à leur acte.

Comptez deux à trois minutes avant que tous les services répondent (`docker compose ps`).

## 6. Activer le paiement (PayDunya, mode test)

Sans clés PayDunya, tout fonctionne sauf le paiement : les demandes s'arrêtent à « À payer ».

1. Créez un compte sur <https://app.paydunya.com>, puis une application en **mode test**.
2. Dans *Intégrez notre API*, copiez la clé principale, la clé privée et le token dans `.env`
   (`PAYDUNYA_MASTER_KEY`, `PAYDUNYA_PRIVATE_KEY`, `PAYDUNYA_TOKEN`), avec `PAYDUNYA_MODE=test`.
3. Dans le tableau de bord PayDunya, créez un **client fictif** de test : c'est avec lui que vous payez sur
   la page PayDunya, sans argent réel.
4. Redémarrez le service de paiement pour qu'il lise les clés :

   ```bash
   docker compose up -d paiement-service
   ```

## 7. Essayer : le certificat de célibat

Avec `ECIVIL_MODE=demo` :

1. **Citoyen** : créez un compte, ouvrez *Documents* → filtre *Certificats* → *Certificat de célibat* →
   *Faire une demande*. Personne concernée : **Mame Diarra Gueye**, sexe féminin, née le **09/01/2003** à
   **Saint-Louis**. Envoyez.
2. **Délégué** (`delegue@ecivil.sn`, dans une fenêtre de navigation privée) : *Espace agent* → *Demandes à
   examiner* → ouvrez la demande → *Valider et ouvrir au paiement*.
3. **Citoyen** : sur la demande, *Payer en ligne* → payez avec le client fictif PayDunya. De retour sur le
   portail, la demande passe « Acte retrouvé » : l'acte de naissance est trouvé et aucun mariage n'est inscrit.
4. **Officier** (`officier@ecivil.sn`) : *Espace agent* → *Signature et validation* → *Contrôler et signer* →
   *Signer et délivrer*.
5. **Citoyen** : *Télécharger* le certificat. Le QR code mène à la page de vérification.

Pour voir un refus : même demande pour **Babacar Mbaye**, né le **14/02/1972** à **Kaolack**. Il est marié :
la demande passe « Acte non retrouvé » avec la raison, et un agent décide de la suite.

## 8. Arrêter, relancer, repartir de zéro

| Action | Commande |
|---|---|
| Arrêter (les données sont conservées) | `docker compose down` |
| Relancer | `docker compose up -d` |
| Après une modification du code | `.\mvnw.cmd -DskipTests package` puis `docker compose up --build -d` |
| Voir les journaux d'un service | `docker compose logs -f demande-service` |
| Tout effacer (bases comprises) et repartir de zéro | `docker compose down -v` puis `docker compose up -d` |

## 9. Problèmes fréquents

| Symptôme | Cause et solution |
|---|---|
| `port is already allocated` sur **8080** | Un autre serveur occupe le port, souvent le **Tomcat** de l'ancienne version : arrêtez-le. |
| `port is already allocated` sur **8180** ou **15672** | Un autre programme utilise ce port : fermez-le. |
| `java` n'est pas reconnu, ou `JAVA_HOME` est signalé incorrect | Réinstallez Temurin en cochant *Set JAVA_HOME* et *Add to PATH*, puis rouvrez PowerShell. |
| `'C:\Users\Prénom' n'est pas reconnu` puis `Cannot start maven from wrapper` | Votre nom d'utilisateur Windows contient un espace. Récupérez la dernière version du projet (`git pull`) ou, en attendant, tapez `$env:MAVEN_USER_HOME = "C:\maven-wrapper"` avant `.\mvnw.cmd`. |
| `COPY services/…/target/….jar` : `not found` lors de `docker compose up` | Les services n'ont pas été construits dans ce dossier : lancez d'abord `.\mvnw.cmd -DskipTests package` (fin : `BUILD SUCCESS`). |
| `Cannot connect to the Docker daemon` | Docker Desktop n'est pas lancé. |
| Un service redémarre en boucle (`docker compose ps`) | Souvent la mémoire : augmentez-la dans Docker Desktop. Sinon, lisez `docker compose logs <service>`. |
| Erreur de connexion juste après le démarrage | Keycloak met une à deux minutes à être prêt : patientez puis rechargez la page. |
| « Paiement momentanément indisponible. » | Clés PayDunya absentes ou fausses (étape 6). |
| Un mot de passe de `.env` modifié n'est pas pris en compte | PostgreSQL et Keycloak ne lisent leurs mots de passe qu'à la création : `docker compose down -v` puis `docker compose up -d` (efface les données). |

PostgreSQL est accessible depuis votre ordinateur sur le port **5433** (pour ne pas gêner un PostgreSQL déjà
installé, qui utilise 5432), avec l'utilisateur `postgres` et `POSTGRES_PASSWORD`.
