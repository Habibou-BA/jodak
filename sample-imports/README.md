# Fichiers d'exemple d'import

Ces fichiers permettent de tester l'import asynchrone (format **CSV** ; XLSX à venir).

## Endpoint

```
POST /api/admin/imports        (multipart, ROLE_ADMIN)
  file               : le fichier CSV
  jobType            : DISCIPLINE | ATHLETE
  format             : CSV
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
