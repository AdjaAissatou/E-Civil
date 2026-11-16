# Version 1 (mémoire) : base de données et corrections

Le code de la version 1 (Servlets/JSP + ServeurCentral) se trouve dans l'historique git, au commit `28d59f4` :

```bash
git worktree add ../e-civil-v1 28d59f4
```

## `base-e-civil-v1.sql`

Recrée la base PostgreSQL `E-Civil` de la version 1, qui avait été perdue (25 tables, la vue
`recherche_documents` et des données de départ). Le schéma est déduit des requêtes des DAO, puis complété
avec le MCD PowerAMC du 10/08/2025. L'en-tête du script liste les écarts entre le MCD et le code.

```bash
createdb -U postgres E-Civil
psql -U postgres -d E-Civil -f base-e-civil-v1.sql
```

La base du ServeurCentral (`serveur-central-etat-civil`) n'a pas besoin de script : Hibernate crée la table
`actes_registre` au démarrage.

## `corrections-v1/`

Versions corrigées de deux servlets, à copier dans `src/main/java/EtatCivil/controllers/` :

| Fichier | Bug corrigé |
|---|---|
| `GestionPermissionsServlet.java` | Redirigeait vers `gestion-permissions` alors que la page est déclarée sur `/gestionPermissions` : chaque ajout, modification ou suppression finissait sur une page introuvable. |
| `GestionDocumentsServlet.java` | Une ancienne copie redirigeait vers `gestion-documents` au lieu de `/gestionDocuments`. |
| les deux | Les messages (« Document ajouté avec succès ») étaient placés tels quels dans l'URL de redirection : les espaces et les accents sont maintenant encodés (`URLEncoder`), et l'adresse est préfixée du contexte de l'application (`request.getContextPath()`), pour fonctionner quel que soit le nom du `.war`. |

Ces deux fichiers compilent avec le reste du code du commit `28d59f4`.

La version actuelle (microservices) n'est concernée par rien de tout cela : ses bases sont créées par Flyway.
