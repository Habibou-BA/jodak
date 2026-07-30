# Guide de configuration CI/CD (GitHub)

Ce guide décrit **tout ce qu'il faut faire sur GitHub** pour que les workflows d'intégration et de
livraison continues se déclenchent et fonctionnent. Les workflows eux-mêmes sont dans
[`.github/`](.github) ; leur rôle est résumé dans le [README](README.md#intégration-continue-cicd).

> **En bref :** aucun secret à créer. Les tests utilisent des conteneurs éphémères (Testcontainers)
> et la publication d'image utilise le jeton intégré `GITHUB_TOKEN`. Il suffit de pousser le code
> et d'activer quelques options.

---

## 1. Créer le dépôt distant et pousser

```bash
# Dépôt public créé sur GitHub (vide, sans README ni .gitignore).
git remote add origin https://github.com/<OWNER>/<REPO>.git
git push -u origin main
git push origin --tags            # si vous utilisez des tags de version (v1.0.0…)
```

Le push sur `main` déclenche immédiatement les workflows **CI** et **CodeQL**.

---

## 2. Réglages du dépôt à activer (onglet _Settings_)

| Emplacement                       | Réglage                                  | Valeur attendue                                                                                                                                     |
| --------------------------------- | ---------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Actions → General**             | _Actions permissions_                    | _Allow all actions and reusable workflows_ (activé par défaut sur les dépôts publics)                                                               |
| **Actions → General**             | _Workflow permissions_                   | Peut rester sur _Read-only_ : nos workflows demandent explicitement les droits nécessaires via la clé `permissions:` (elle **prime** sur ce défaut) |
| **Code security → Dependabot**    | _Dependabot alerts_ + _security updates_ | Activés (recommandé) — complètent le fichier `dependabot.yml`                                                                                       |
| **Code security → Code scanning** | _CodeQL analysis_                        | Rien à faire : le workflow `codeql.yml` publie les résultats (gratuit sur dépôt **public**)                                                         |

> Sur un dépôt **privé**, CodeQL et les alertes de sécurité nécessitent _GitHub Advanced Security_.
> Ce projet est prévu pour un dépôt **public**, où tout est inclus.

---

## 3. Publication de l'image Docker sur GHCR

Le job `docker` du workflow **CI** construit et pousse l'image sur **GitHub Container Registry**
après des tests verts (uniquement sur `main` et les tags `v*`).

- **Authentification** : via `secrets.GITHUB_TOKEN` (fourni automatiquement) + permission
  `packages: write` déclarée dans le workflow. **Aucun secret à créer.**
- **Après le premier run réussi** : l'image apparaît dans l'onglet _Packages_ du dépôt, nommée
  `ghcr.io/<owner>/olympics-platform`.
- **Rendre l'image publique** (pour un `docker pull` anonyme) :
  _Packages → olympics-platform → Package settings → Danger Zone → Change visibility → Public_.
- Récupération :
  ```bash
  docker pull ghcr.io/<owner>/olympics-platform:latest
  ```

---

## 4. Variables d'environnement et secrets

### Pour l'intégration continue (état actuel) : **rien à configurer**

Les tests d'intégration démarrent leur **propre** PostgreSQL éphémère via Testcontainers : aucune
URL ni identifiant de base n'est nécessaire dans GitHub. Le pipeline actuel **ne requiert aucun
secret ni aucune variable**.

### Si vous ajoutez un déploiement (production)

L'application **ne requiert aucune authentification** : les seules variables utiles concernent la
base de données. Définies sur votre **hôte de déploiement**, ou (déploiement piloté par un workflow)
dans _Settings → Secrets and variables → Actions_.

| Clé                          | Type GitHub conseillé | Rôle                              |
| ---------------------------- | --------------------- | --------------------------------- |
| `SPRING_DATASOURCE_URL`      | Variable              | URL JDBC PostgreSQL de production |
| `SPRING_DATASOURCE_USERNAME` | Variable              | Utilisateur de la base            |
| `SPRING_DATASOURCE_PASSWORD` | **Secret**            | Mot de passe de la base           |

> Les valeurs par défaut (docker-compose) sont dans [`.env.example`](.env.example).

---

## 5. Déclencheurs des workflows

| Événement                | CI (tests) | CI (image GHCR)        | CodeQL          |
| ------------------------ | ---------- | ---------------------- | --------------- |
| Push sur `main`          | ✅         | ✅                     | ✅              |
| Push d'un tag `v*`       | ✅         | ✅ (tag sémantique)    | —               |
| Pull request vers `main` | ✅         | — (pas de publication) | ✅              |
| Hebdomadaire (lundi)     | —          | —                      | ✅ + Dependabot |
| Manuel (_Run workflow_)  | ✅         | selon la branche       | —               |

---

## 6. Vérification

1. Onglet **Actions** : les runs _CI_ et _CodeQL_ apparaissent et passent au vert.
2. Onglet **Security → Code scanning** : les éventuelles alertes CodeQL.
3. Onglet **Packages** : l'image `olympics-platform` après un push sur `main`.
4. Mettez à jour les **badges** du README (`OWNER/REPO` → votre dépôt).

---
