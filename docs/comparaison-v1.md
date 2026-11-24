# Version 1 et version actuelle : ce qui a été rétabli

Inventaire établi en relisant tout le code de la version 1 (commit `28d59f4` : servlets, DAO et pages JSP) et la
base reconstituée (`docs/memoire/base-e-civil-v1.sql`). Chaque élément qui avait disparu a été rétabli ; la
colonne de droite indique où il se trouve aujourd'hui.

## 1. Catalogue : natures et variétés

Le catalogue suit le modèle Nature / Variété :

- **15 natures** (familles) : Naissance, Reconnaissance, Mariage, Divorce, Décès, Copie, Jugement supplétif,
  Transcription, Mention marginale, Livret de famille, Célibat, Vie individuelle, Nationalité, Domicile, Coutume
  et capacité matrimoniale.
- **46 variétés** (documents délivrables) : les 36 natures de la version 1 et les documents du modèle proposé
  (certificats de naissance, de mariage, de décès et de reconnaissance, mentions de divorce et de légitimation,
  certificat de vie individuelle).

| Élément de la version 1 | Version actuelle |
|---|---|
| Variétés et leurs indicateurs de champs (`conjoint`, `temoins`, `parents`, `medecin`, `mentionmarginale`, `causedeces`) | rubriques de chaque variété : filiation, professions et domiciles, conjoint, témoins, médecin, décès, mention marginale, domicile, précisions |
| 36 natures avec tarif, **durée de validité**, délai | tarif, délai, durée de validité (imprimée sur le document, contrôlée à la vérification) |
| Mots-clés de recherche | mots-clés par variété, utilisés par la recherche du catalogue |
| Motifs par nature avec **pièces à fournir** | motifs communs ou propres à une variété, avec leurs pièces ; pièces à fournir de chaque variété |
| Actes « originaux », premier livret de famille | variétés « Acte de … (original) » et « Livret de famille » |
| Copie d'un document original | lien `copie de` entre une copie et son original (copies littérales, duplicata du livret) |
| Célibat (mariage civil, religieux, adoption), vie (retraite, pension, succession) | une variété par cas |
| Nationalité (3), domicile (3), non-appartenance, coutume | rétablis ; la non-appartenance et la coutume sont établies **sur comparution**, sans acte du registre |
| Reconnaissance (enfant naturel, anticipée, paternelle, maternelle) | une variété par cas, plus le certificat de reconnaissance |
| Gestion des natures et des documents par l'administrateur | *Administration › Documents et tarifs, Natures, Motifs* : création et modification complètes |

## 2. Formulaire de demande

Tous les champs de la version 1 sont de retour, demandés selon la variété : profession et domicile de la
personne, des parents et du conjoint ; date et lieu de naissance du conjoint ; deux témoins ; médecin (nom et
adresse) ; cause, date et lieu du décès ; mention marginale (type et détails) ; précisions (coutume, nationalité).
Le formulaire est prérempli avec le profil du citoyen.

## 3. Compte et espace du citoyen

| Élément de la version 1 | Version actuelle |
|---|---|
| Inscription : téléphone, date et lieu de naissance, sexe, profession, résidence, localité, nombre d'enfants, numéro de carte d'identité | demandés à l'inscription (profil Keycloak) ; téléphone, sexe, date de naissance et lieu de résidence obligatoires |
| Type d'utilisateur : résident ou diaspora | « Je réside au Sénégal / à l'étranger », avec le pays de résidence |
| Modifier son profil | page *Mon compte* |
| Notifications par email (oui / non) | *Mon compte* ; le service de notification n'écrit plus à qui les a refusées |
| Langue (français, wolof, anglais) | choix dans *Mon compte* ou en pied de page ; menus et pied de page traduits, le reste en français (traduction wolof à faire relire) |
| Compteurs des demandes par statut | en tête de *Mes demandes* |

## 4. Référentiels

| Élément de la version 1 | Version actuelle |
|---|---|
| Communes (62, plus la région de Louga : 66 communes, 14 régions) | suggestions dans la saisie du registre |
| Missions diplomatiques, types, zones de compétence, pays, villes | référentiel de la diaspora ; un document papier peut être **retiré à la mission** qui couvre le pays de résidence ; *Administration › Diaspora* |

## 5. Agents

| Élément de la version 1 | Version actuelle |
|---|---|
| Délégué et officier : compteurs par statut | en tête de chaque file |
| Filtres par statut, type de document, format, période | sur toutes les files (délégué, registre, officier, toutes les demandes) |
| Journal des modifications du registre (`audit_registre`) | journal de chaque acte : saisie, correction champ par champ, validation, mention, avec l'agent |
| Mentions marginales | rattachées à l'acte (type, date, référence, contenu) ; un divorce porté en marge d'un mariage le dissout |
| Professions, domiciles, médecin dans l'acte | énonciations de l'acte, reprises sur les actes originaux et copies littérales |

## 6. Administration

Tableau de bord, utilisateurs, profils, rôles, permissions et journal (déjà rétablis), auxquels s'ajoutent les
natures, les documents, les motifs et la diaspora.

## Mise à jour d'une installation existante

Les données sont conservées : les nouvelles tables sont créées, les documents existants rangés dans leur nature,
les mentions déjà saisies en texte deviennent des mentions rattachées, le journal de chaque acte reçoit sa saisie.
