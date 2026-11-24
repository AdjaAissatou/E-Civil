# Utilisateurs, profils, rôles et permissions

Diagramme de classes : `diagramme-roles-permissions.png` (source Mermaid : `diagramme-roles-permissions.mmd`).

## Les quatre notions

| Notion | Ce qu'elle répond | Exemples (données d'origine) |
|---|---|---|
| **Profil** | *Qui est la personne* dans l'organisation | Citoyen résident, Citoyen de la diaspora, Délégué de commune, Officier de l'état civil, Administrateur |
| **Rôle** | *Quelle fonction* elle exerce dans le traitement d'une demande | Administrateur système, Officier d'état civil, Délégué, Chargé de renseignement, Utilisateur |
| **Permission** | *Quelle action élémentaire* elle a le droit de faire | `LECTURE_DOCUMENTS`, `SIGNATURE_DOCUMENTS`, `VALIDATION_DELEGUE`… (15) |
| **Compte** | *Comment* elle se connecte | email, mot de passe haché, actif ou non |

Un utilisateur a **un** profil ; un profil regroupe **un ou plusieurs** rôles ; un rôle accorde **plusieurs**
permissions. Pour savoir si un utilisateur peut agir, on remonte la chaîne :
`Utilisateur → Profil → Rôles → Permissions` (méthode `aLaPermission(code)`).

Les deux associations plusieurs-à-plusieurs deviennent des **classes d'association** (`ProfilRole`,
`RolePermission`), qui deviennent des tables de liaison dans le modèle relationnel (`role_permissions` /
`Association_18` dans la base d'origine).

## Profils proposés

| Profil | Rôles | Remarque |
|---|---|---|
| Citoyen résident | Utilisateur | |
| Citoyen de la diaspora | Utilisateur | `paysResidence` renseigné ; remplace la classe `TypeUtilisateur` |
| Délégué de commune | Délégué | |
| Agent du registre | Chargé de renseignement | saisie et mise à jour du registre central |
| Officier de l'état civil | Officier d'état civil | |
| Chef de centre | Officier d'état civil + Délégué | montre l'intérêt d'un profil à plusieurs rôles |
| Administrateur | Administrateur système | |

## Rôles et permissions (données d'origine)

| Permission | Administrateur système | Officier d'état civil | Délégué | Chargé de renseignement | Utilisateur |
|---|:-:|:-:|:-:|:-:|:-:|
| `LECTURE_DOCUMENTS` | ✔ | ✔ | ✔ | ✔ | ✔ |
| `ECRITURE_DOCUMENTS` | ✔ | ✔ | ✔ |  |  |
| `VALIDATION_ACTES` | ✔ | ✔ |  |  |  |
| `GESTION_UTILISATEURS` | ✔ |  |  |  |  |
| `STATISTIQUES` | ✔ | ✔ | ✔ | ✔ |  |
| `GESTION_PARAMETRES` | ✔ |  |  |  |  |
| `SAISIE_REGISTRES` |  |  |  | ✔ |  |
| `MISE_A_JOUR_REGISTRES` |  |  |  | ✔ |  |
| `SIGNATURE_DOCUMENTS` |  | ✔ |  |  |  |
| `VALIDATION_OFFICIELLE` |  | ✔ |  |  |  |
| `SUPERVISION_ACTES` |  |  | ✔ |  |  |
| `VALIDATION_DELEGUE` |  |  | ✔ |  |  |
| `IMPORT_DONNEES` | ✔ |  |  | ✔ |  |
| `EXPORT_DONNEES` | ✔ |  |  | ✔ |  |
| `GESTION_SERVEUR_CENTRAL` | ✔ |  |  | ✔ |  |

## Correspondance avec la version actuelle (Keycloak)

La version microservices délègue l'identité à Keycloak, qui offre exactement ces notions :

| Diagramme | Keycloak / E-Civil v3 |
|---|---|
| Compte | utilisateur Keycloak (le mot de passe n'est jamais stocké par l'application) |
| Profil | **groupe** Keycloak (un utilisateur y est placé, le groupe porte des rôles) |
| Rôle | **rôle du realm** : `CITOYEN`, `DELEGUE`, `AGENT_REGISTRE`, `OFFICIER`, `ADMIN` |
| Permission | rôle **composite** ou contrôle `@PreAuthorize` sur chaque action des services |

Aujourd'hui, la version actuelle vérifie directement les rôles sur chaque action. Les groupes et les permissions
fines du diagramme correspondent à une évolution possible, sans changement d'architecture.
