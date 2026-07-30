# Fichiers d'exemple d'import

Ces fichiers permettent de tester l'import asynchrone. Deux formats sont pris en charge :
**CSV** et **XLSX** (Apache POI). Un `.xlsx` reprend les **mêmes colonnes** que le CSV (première
ligne = en-tête, première feuille utilisée).

Le fichier téléversé est durci contre les archives malveillantes : taille bornée (25 Mio),
cohérence extension/format vérifiée, et pour les `.xlsx` protection anti « zip bomb » / XXE
(décompression bornée, entités externes XML désactivées).

## Initialisation complète du système (un seul fichier)

**`initialisation-systeme.xlsx`** est un **classeur unique** qui initialise un système vide en une
opération. Il contient une feuille par entité, chargée dans l'ordre des dépendances :

| Feuille | Contenu | Colonnes |
|---|---|---|
| `Nations` | **206 CNO** (tous les pays concernés par les JOJ) | `code`, `name` |
| `Disciplines` | Programme sportif | `name` |
| `Epreuves` | Épreuves datées (fenêtre des Jeux) | `label`, `discipline`, `date` |
| `Athletes` | Jeu d'athlètes **fictifs** représentatifs | `lastName`, `firstName`, `gender`, `birthDate`, `countryCode`, `discipline`, `heightCm`, `weightKg` |
| `Resultats` | Podiums de démonstration (médaille dérivée du rang) | `epreuveLabel`, `discipline`, `date`, `athleteLastName`, `athleteFirstName`, `athleteBirthDate`, `rank` |

Les feuilles sont chargées dans l'ordre des dépendances (une nation avant un athlète, une épreuve
et un athlète avant un résultat). Chaque résultat référence une épreuve (libellé + discipline +
date) et un athlète (nom + prénom + date de naissance) déjà présents.

Import en une passe (type `SYSTEME`) :
```
POST /api/admin/imports   file=initialisation-systeme.xlsx  jobType=SYSTEME  format=XLSX  mode=COMMIT  duplicateStrategy=SKIP
```
ou, depuis la console, **Imports → Type « Système complet » + Format XLSX**. Les nations déjà
présentes (référentiel Flyway) sont ignorées (`SKIP`). Les données source, éditables, sont dans
`sample-imports/init/*.csv` (une par entité).

## Endpoint

```
POST /api/admin/imports        (multipart, ROLE_ADMIN)
  file               : le fichier CSV ou XLSX
  jobType            : DISCIPLINE | ATHLETE
  format             : CSV | XLSX
  mode               : DRY_RUN (validation seule) | COMMIT (import réel)
  duplicateStrategy  : SKIP | UPDATE | REJECT
```

Suivi : `GET /api/admin/imports/{id}` (progression, compteurs) ·
rapport d'erreurs : `GET /api/admin/imports/{id}/errors` ·
annulation : `POST /api/admin/imports/{id}/cancel` ·
compensation : `POST /api/admin/imports/{id}/rollback`.

## Disciplines — colonnes

| Colonne | Obligatoire | Règle |
|---|---|---|
| `name` | oui | Nom unique (insensible à la casse) |

## Athlètes — colonnes

| Colonne | Obligatoire | Règle |
|---|---|---|
| `lastName` | oui | Non vide |
| `firstName` | oui | Non vide |
| `gender` | oui | `Homme`/`Femme` (ou Male/Female, M/F) → normalisé en MALE/FEMALE |
| `birthDate` | oui | `AAAA-MM-JJ` ou `JJ/MM/AAAA`, dans le passé |
| `countryCode` | oui | Code d'une nation **existante** (ex. FRA, USA, JAM) |
| `discipline` | oui | Nom d'une discipline **existante** |
| `heightCm` | oui | Entier 100–260 |
| `weightKg` | oui | Entier 30–250 |

> Les nations sont fournies par le référentiel (migration `V2`). Importez d'abord les **disciplines**,
> puis les **athlètes**.

## Fichiers fournis

| Fichier | Cas testé |
|---|---|
| `disciplines-valid.csv` | Import valide de disciplines |
| `athletes-valid.csv` | Import valide d'athlètes |
| `athletes-duplicates.csv` | Doublons (selon `duplicateStrategy`) |
| `athletes-invalid.csv` | Erreurs de validation (colonnes/valeurs) et références inconnues |
| `empty-file.csv` | Fichier sans donnée (en-tête seul) |
