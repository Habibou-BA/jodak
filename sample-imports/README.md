# Fichier d'initialisation du système

L'administration importe un **unique fichier** qui initialise l'ensemble des données du système :
**`initialisation-systeme.xlsx`**, un classeur `.xlsx` avec **une feuille par entité**, chargées en
une passe dans l'ordre des dépendances.

Le fichier est durci contre les archives malveillantes : taille bornée (25 Mio), extension `.xlsx`
requise, protections anti « zip bomb » / XXE (décompression bornée, entités externes désactivées).

## Feuilles du classeur

| Feuille | Contenu | Colonnes |
|---|---|---|
| `Nations` | **206 CNO** (tous les pays concernés par les JOJ) | `code`, `name` |
| `Disciplines` | Programme sportif | `name` |
| `Epreuves` | Épreuves datées (fenêtre des Jeux) | `label`, `discipline`, `date` |
| `Athletes` | Jeu d'athlètes **fictifs** représentatifs | `lastName`, `firstName`, `gender`, `birthDate`, `countryCode`, `discipline`, `heightCm`, `weightKg` |
| `Resultats` | Podiums de démonstration (médaille dérivée du rang) | `epreuveLabel`, `discipline`, `date`, `athleteLastName`, `athleteFirstName`, `athleteBirthDate`, `rank` |

Chaque `Athlete` référence une nation (`countryCode`) et une discipline existantes ; chaque
`Resultat` référence une épreuve (libellé + discipline + date) et un athlète (nom + prénom +
naissance) déjà présents. Les données source, éditables, sont dans `init/*.csv` (une par entité) —
le classeur en est la compilation.

## Import

```
POST /api/admin/imports        (multipart, ROLE_ADMIN)
  file               : initialisation-systeme.xlsx (seul format accepté : .xlsx)
  mode               : COMMIT (import réel) | DRY_RUN (simulation, aucune écriture)
  duplicateStrategy  : SKIP (ignorer les existants) | UPDATE | REJECT
```
ou, depuis la console : **Imports → Lancer l'initialisation**.

Suivi : `GET /api/admin/imports/{id}` (progression, compteurs) ·
rapport d'erreurs : `GET /api/admin/imports/{id}/errors` ·
annulation : `POST /api/admin/imports/{id}/cancel` ·
compensation : `POST /api/admin/imports/{id}/rollback`.

## Règles de validation (par feuille)

- **Nations** — `code` : 3 lettres majuscules (CIO), unique ; `name` : unique.
- **Disciplines** — `name` : unique (insensible à la casse).
- **Epreuves** — `discipline` existante ; `date` `AAAA-MM-JJ` ou `JJ/MM/AAAA` ; unique (libellé,
  discipline, date).
- **Athletes** — `gender` `Homme`/`Femme` ; `birthDate` passée ; `heightCm` 100–260 ; `weightKg`
  30–250 ; `countryCode` et `discipline` existants ; unique (nom, prénom, naissance).
- **Resultats** — `rank` ≥ 1 (médaille : 1→or, 2→argent, 3→bronze) ; un seul athlète par rang et un
  seul résultat par athlète dans une épreuve.
