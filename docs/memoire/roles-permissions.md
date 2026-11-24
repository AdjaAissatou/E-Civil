# Utilisateurs, profils, rôles et permissions

Diagramme de classes : `diagramme-roles-permissions.png` (source Mermaid : `diagramme-roles-permissions.mmd`).
Il décrit le domaine du service `habilitation-service` tel qu'il est implémenté.

## Les quatre notions

| Notion | Question à laquelle elle répond | Exemples |
|---|---|---|
| **Utilisateur** | *Qui* se connecte | un citoyen, un agent (compte Keycloak : email, actif ou non) |
| **Profil** | *Quelle situation* dans l'organisation | Citoyen, Délégué de commune, Agent du registre, Officier de l'état civil, Chef de centre, Administrateur |
| **Rôle** | *Quelle fonction* exercée | Citoyen, Délégué, Agent du registre, Officier de l'état civil, Administrateur (+ rôles créés par l'administrateur) |
| **Permission** | *Quelle action élémentaire* est autorisée | `DEMANDES_VALIDER`, `DOCUMENTS_SIGNER`, `REGISTRE_VALIDER`… (15) |

Un utilisateur appartient à **un ou plusieurs** profils ; un profil regroupe **un ou plusieurs** rôles ; un rôle
accorde **une ou plusieurs** permissions. Les droits d'une personne sont l'union des permissions obtenues par
cette chaîne : `Utilisateur → Profils → Rôles → Permissions`.

Chaque service vérifie une **permission** sur chaque action (`@PreAuthorize("hasRole('DOCUMENTS_SIGNER')")`),
jamais un rôle ou un profil : l'administrateur peut donc réorganiser profils et rôles sans toucher au code.

## Les 15 permissions

| Famille | Code | Libellé |
|---|---|---|
| Citoyen | `DEMANDES_DEPOSER` | Déposer ses demandes |
| Instruction | `DEMANDES_CONSULTER` | Consulter toutes les demandes |
| Instruction | `DEMANDES_VALIDER` | Valider les demandes |
| Instruction | `DOCUMENTS_LIVRER` | Livrer les documents papier |
| Instruction | `DEMANDES_SUPERVISER` | Superviser les demandes (rejet à toute étape) |
| Registre | `REGISTRE_CONSULTER` | Consulter le registre national |
| Registre | `REGISTRE_SAISIR` | Saisir le registre |
| Registre | `REGISTRE_VALIDER` | Valider les actes |
| Registre | `DEMANDES_RECHERCHER` | Rechercher les actes des demandes |
| Officier | `DOCUMENTS_SIGNER` | Signer les documents |
| Administration | `CATALOGUE_GERER` | Gérer le catalogue |
| Administration | `STATISTIQUES_CONSULTER` | Consulter les statistiques |
| Administration | `UTILISATEURS_GERER` | Gérer les utilisateurs |
| Administration | `HABILITATIONS_GERER` | Gérer les habilitations |
| Administration | `JOURNAL_CONSULTER` | Consulter le journal |

Chaque permission porte aussi une description, ce qu'elle permet (*avantage*) et ce qu'elle n'autorise pas
(*interdiction*), modifiables dans *Administration › Permissions*. Les codes, eux, sont fixés par l'application.

## Configuration de départ

| Permission | Administrateur | Officier | Agent du registre | Délégué | Citoyen |
|---|:-:|:-:|:-:|:-:|:-:|
| `DEMANDES_DEPOSER` |  |  |  |  | ✔ |
| `DEMANDES_CONSULTER` | ✔ | ✔ | ✔ | ✔ |  |
| `DEMANDES_VALIDER` | ✔ |  |  | ✔ |  |
| `DOCUMENTS_LIVRER` | ✔ |  |  | ✔ |  |
| `DEMANDES_SUPERVISER` | ✔ |  |  |  |  |
| `REGISTRE_CONSULTER` | ✔ | ✔ | ✔ |  |  |
| `REGISTRE_SAISIR` | ✔ | ✔ | ✔ |  |  |
| `REGISTRE_VALIDER` | ✔ | ✔ |  |  |  |
| `DEMANDES_RECHERCHER` | ✔ | ✔ | ✔ |  |  |
| `DOCUMENTS_SIGNER` | ✔ | ✔ |  |  |  |
| `CATALOGUE_GERER`, `STATISTIQUES_CONSULTER`, `UTILISATEURS_GERER`, `HABILITATIONS_GERER`, `JOURNAL_CONSULTER` | ✔ |  |  |  |  |

| Profil | Rôles |
|---|---|
| Citoyen (*par défaut* : donné à chaque inscription) | Citoyen |
| Délégué de commune | Délégué |
| Agent du registre | Agent du registre |
| Officier de l'état civil | Officier de l'état civil |
| Chef de centre | Officier de l'état civil + Délégué |
| Administrateur | Administrateur |

## Règles de gestion

- La plateforme garde toujours au moins un administrateur actif (`UTILISATEURS_GERER` + `HABILITATIONS_GERER`) :
  toute modification qui le supprimerait est refusée (classe `Organisation`, qui simule le résultat avant de l'appliquer).
- Un administrateur ne peut ni se désactiver ni se retirer lui-même ses droits d'administration.
- Les rôles de base peuvent être modifiés mais pas supprimés ; un rôle utilisé par un profil ne peut pas être supprimé.
- Le profil par défaut ne peut pas être supprimé ; un profil qui a encore des membres non plus.
- Un agent est créé avec un mot de passe provisoire qu'il doit changer à sa première connexion.
- Toute opération est inscrite au journal (date, auteur, type, cible, détail), qui ne se modifie jamais.
- Après un changement, les personnes concernées sont déconnectées : elles retrouvent leurs nouveaux droits à la
  connexion suivante.

## Correspondance avec Keycloak

Keycloak reste la seule source de vérité des identités ; `habilitation-service` l'administre par son API.

| Diagramme | Keycloak |
|---|---|
| Utilisateur | utilisateur du realm `ecivil` (le mot de passe n'est jamais vu par l'application) |
| Profil | **groupe** ; le profil par défaut est un *default group* |
| Rôle | **rôle composite** du realm (attribut `ecivil.type=role`) |
| Permission | **rôle simple** du realm (attribut `ecivil.type=permission`) |
| Opération | table `operation` de la base `habilitation` |

Keycloak développe groupes et rôles composites dans le jeton (`realm_access.roles`) : le jeton d'une personne
contient directement ses permissions, que chaque service vérifie sans appel supplémentaire.

## Mise à jour d'une installation existante

Au premier démarrage, `habilitation-service` crée les permissions, rôles et profils manquants, puis range chaque
compte dans le profil correspondant à son ancien rôle (`ADMIN` → Administrateur, `OFFICIER` → Officier de l'état
civil…) et lui retire ce rôle direct. Cette migration n'a lieu qu'une fois ; les choix ultérieurs de
l'administrateur ne sont jamais écrasés.
