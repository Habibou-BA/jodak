# Plateforme JO — API REST & Web Service SOAP

Plateforme numérique centralisée des Jeux Olympiques : gestion des disciplines, athlètes, épreuves
et résultats, calcul automatique du tableau des médailles et statistiques. Elle expose
**simultanément** une **API REST** (applications Web/Mobile) et un **Web Service SOAP en lecture
seule** (système d'information historique).

> Projet pédagogique construit **progressivement**. Ce README s'enrichit à chaque étape.

## Technologies

Java 21 · Spring Boot 3.3 · Maven · PostgreSQL 16 · Spring Data JPA · Spring Validation ·
Spring Web · Spring WS (SOAP) · SpringDoc OpenAPI/Swagger · Lombok · Flyway · Docker /
Docker Compose · JUnit 5 · Mockito · Testcontainers.

## Documentation de conception

| Document | Contenu |
|---|---|
| [`docs/business-rules.md`](docs/business-rules.md) | Règles métier, décisions, validations, mappings |
| [`docs/architecture.md`](docs/architecture.md) | Architecture en couches, packages, flux |
| [`docs/data-model.md`](docs/data-model.md) | MCD/MLD, contraintes, stratégie Flyway |

## Prérequis

- **JDK 21** (build/run local). Via SDKMAN : `sdk install java 21-tem`
- **Maven 3.9+**
- **Docker** et **Docker Compose** (lancement conteneurisé)

## Lancement

### Option A — Docker Compose (recommandé)

Démarre PostgreSQL **et** l'application (profil `prod`) :

```bash
docker compose up --build
```

- API : http://localhost:8080
- Swagger UI : http://localhost:8080/swagger-ui.html

### Option B — Local (profil `dev`)

1. Démarrer uniquement PostgreSQL :

```bash
docker compose up -d db
```

2. Lancer l'application :

```bash
mvn spring-boot:run
```

Le profil `dev` est actif par défaut (base `olympics`, jeu de démonstration `db/seed`).

## Commandes Maven utiles

```bash
mvn clean verify          # build + tests
mvn spring-boot:run       # démarrer (profil dev)
mvn test                  # tests uniquement
mvn clean package         # produire le jar (target/olympics-platform-0.1.0.jar)
```

## Configuration & profils

| Profil | Datasource | Flyway | Usage |
|---|---|---|---|
| `dev` (défaut) | PostgreSQL local | `db/migration` + `db/seed` | Développement |
| `test` | Testcontainers | `db/migration` | Tests d'intégration |
| `prod` | Variables d'environnement | `db/migration` | Production / Docker |

Variables d'environnement (profil `prod`) : `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

## Organisation des packages (`com.jodak`)

```text
config · constants · controllers/rest · dtos · entities · enums · exceptions
mappers · repositories · services/{interfaces,implementations} · soap
specifications · utils · validators
```

Détails et responsabilités : [`docs/architecture.md`](docs/architecture.md).

## Base de données & migrations

- PostgreSQL, migrations **Flyway en SQL brut** (`src/main/resources/db/migration`).
- `hibernate.ddl-auto = validate` : le schéma n'est **jamais** modifié par Hibernate.

## API REST

- Préfixe : `/api/v1/`
- Documentation interactive : **Swagger UI** (`/swagger-ui.html`).

## SOAP

Web Service en **lecture seule** pour le SI historique (contrat XSD). *À venir en Phase 6.*

## Tests

- **Tests unitaires** (JUnit 5 + Mockito) — rapides, sans Docker :

  ```bash
  mvn test
  ```

- **Tests d'intégration** (`*IT`, Testcontainers PostgreSQL) — nécessitent Docker, via `mvn verify` :

  ```bash
  mvn verify
  ```

### Note macOS / Docker Desktop (Docker Engine ≥ 29)

Sur cette machine, le socket Docker par défaut (`/var/run/docker.sock`) est indisponible et Docker 29
impose une API minimale ≥ 1.40. Pour exécuter les tests d'intégration en local :

```bash
export DOCKER_HOST="unix://$HOME/Library/Containers/com.docker.docker/Data/docker.raw.sock"
export TESTCONTAINERS_RYUK_DISABLED=true
mvn -DargLine="-Dapi.version=1.43" verify
```

En CI (Linux, socket Docker standard), `mvn verify` fonctionne sans ces réglages.

## Collection Postman

Fournie et organisée par domaine (Athlètes, Disciplines, Épreuves, Résultats, Tableau des
médailles, Tableau de bord, SOAP). *À venir.*
