# Ce que la version 1 faisait et que la version actuelle ne fait plus

Inventaire établi en relisant tout le code de la version 1 (commit `28d59f4` : 47 servlets, DAO et pages JSP)
et la base reconstituée (`docs/memoire/base-e-civil-v1.sql`), puis en comparant avec la version actuelle.

Légende : ❌ absent · ⚠️ présent en partie · ✅ présent (parfois sous une autre forme).

## 1. Catalogue des documents : natures et variétés

Dans la v1, **19 variétés** (Acte de naissance, Acte de mariage, Certificat de célibat…) regroupent
**36 natures** délivrables, chacune avec un tarif, une **durée de validité** et un délai. Chaque variété porte
des indicateurs qui décident des champs du formulaire : `conjoint`, `temoins`, `parents`, `medecin`,
`mentionmarginale`, `causedeces`, ainsi que des **mots-clés** pour la recherche.

| Élément de la v1 | Version actuelle | |
|---|---|---|
| Table des variétés (19) avec ses indicateurs de champs | remplacée par un type d'acte fixé dans le code (naissance, reconnaissance, mariage, divorce, décès) | ❌ |
| Table des natures (36) | 19 documents | ⚠️ |
| Durée de validité de chaque document | absente | ❌ |
| Mots-clés de recherche par variété | recherche sur le libellé seulement | ⚠️ |
| Motifs propres à chaque nature, avec les **pièces à fournir** (`documents_requis`) | motifs communs à tous les documents, sans pièces à fournir | ❌ |
| Gestion des natures par l'administrateur (ajout, modification) | tarif, délai et disponibilité seulement : impossible d'ajouter un document | ⚠️ |

Natures de la v1 absentes du catalogue actuel :

| Nature v1 | État |
|---|---|
| Acte de naissance / de mariage / de décès « original » | ❌ (seuls l'extrait et la copie littérale existent) |
| Livret de famille (premier livret) | ❌ (seul le duplicata existe) |
| Certificat de célibat : mariage civil, mariage religieux, adoption | ⚠️ un seul certificat (le but passe par le motif) |
| Certificat de vie : retraite, pension, succession | ⚠️ un seul certificat (le but passe par le motif) |
| Certificat de nationalité : sénégalaise, pour mariage, pour naturalisation | ❌ |
| Certificat de domicile : élections, scolarité, démarches administratives | ❌ |
| Reconnaissance : enfant naturel, anticipée, paternelle, maternelle | ⚠️ une seule « copie d'acte de reconnaissance » |
| Certificat de non-appartenance à la nationalité | ❌ |
| Certificat de coutume pour mariage mixte | ❌ |

Le modèle que tu proposes (Nature = famille : Naissance, Mariage, Décès, Reconnaissance, Mention marginale,
Célibat, Vie individuelle, Jugement supplétif, Copie ; Variété = document précis avec ses champs propres ;
`estOriginal` / `copieDe` ; mention rattachée à un document) n'existe **ni dans la v1 ni dans la version
actuelle** : dans la v1 les noms sont inversés (la variété est la famille, la nature le document) ; dans la
version actuelle, la famille est une simple valeur (`typeActe`) et il n'y a ni table des familles, ni lien
original / copie, ni mention rattachée à un document (les mentions marginales sont un texte libre de l'acte).
Ne figurent nulle part aujourd'hui : certificat de naissance, certificat de mariage, certificat de décès,
mention de divorce, mention de légitimation, certificat de vie individuelle.

## 2. Formulaire de demande

| Champ de la v1 | Version actuelle | |
|---|---|---|
| Personne concernée : prénom, nom, sexe, date et lieu de naissance | présents | ✅ |
| Profession et résidence de la personne concernée | absents | ❌ |
| Père et mère : nom, prénom | présents | ✅ |
| Père et mère : profession, résidence | absents | ❌ |
| Conjoint : nom, prénom | présents | ✅ |
| Conjoint : date et lieu de naissance, profession, résidence | absents | ❌ |
| Témoins 1 et 2 (nom, prénom) | absents | ❌ |
| Médecin (nom, prénom, adresse du cabinet) | absent de la demande (existe dans l'acte du registre) | ⚠️ |
| Décès : cause, date, lieu | date seulement | ⚠️ |
| Mention marginale : type et détails | absente de la demande | ❌ |
| Pour moi / pour un autre, format numérique ou papier, adresse et ville de livraison, motif | présents | ✅ |

## 3. Compte et espace du citoyen

| Élément de la v1 | Version actuelle | |
|---|---|---|
| Inscription : téléphone, date et lieu de naissance, sexe, profession, résidence, localité, nombre d'enfants, numéro de carte d'identité | prénom, nom, email et mot de passe seulement | ❌ |
| Type d'utilisateur : résident ou diaspora | absent | ❌ |
| Modifier son profil (prénom, nom, téléphone, nombre d'enfants) | absent du portail | ❌ |
| Préférences : notifications par email (oui / non), langue (français, wolof, anglais) | absentes (français seulement, emails toujours envoyés) | ❌ |
| Compteurs de ses demandes par statut | absents | ❌ |
| Suivi d'une demande avec son historique | présent | ✅ |
| Paiement Wave, Orange Money, carte | présent par PayDunya (plus Free Money) ; Stripe retiré | ✅ |

## 4. Référentiels

| Élément de la v1 | Version actuelle | |
|---|---|---|
| 62 communes réparties dans 14 régions | commune saisie en texte libre | ❌ |
| Missions diplomatiques, types de mission, zones de compétence, pays, villes (diaspora) | absents | ❌ |

## 5. Espaces des agents

| Élément de la v1 | Version actuelle | |
|---|---|---|
| Délégué : compteurs (total, en attente, validées, terminées) | absents | ❌ |
| Délégué : filtres par statut, type de document, date de début et de fin | statut et référence seulement | ⚠️ |
| Officier : compteurs (trouvés, signés, rejetés) | absents | ❌ |
| Officier : filtres par type d'acte, format, date | absents | ❌ |
| Saisie du registre (chargé de renseignement) : volume, folio, année, commune, centre, observations | présente (transcription) | ✅ |
| Journal des modifications du registre (`audit_registre`) | absent : on ne sait pas qui a modifié un acte ni quoi | ❌ |
| Documents produits depuis le registre (`generation_documents`) | présent (document-service) | ✅ |
| Recherche automatique de l'acte (planificateur toutes les 2 minutes) | présente, déclenchée par événement au paiement | ✅ |

## 6. Administration

| Élément de la v1 | Version actuelle | |
|---|---|---|
| Tableau de bord : utilisateurs, documents, rôles, permissions, activité récente | présent | ✅ |
| Utilisateurs, profils, rôles, permissions, matrice rôles × permissions | présents (plus complets : journal, règles de sûreté) | ✅ |
| Gestion des documents du registre (ajout, modification, suppression) | transcription et correction par les agents ; pas de suppression (volontaire : un acte validé ne se supprime pas) | ⚠️ |
