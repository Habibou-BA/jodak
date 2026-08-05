# syntax=docker/dockerfile:1

# --- Étape 1 : build (Java 21 + Maven) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# Le dépôt Maven est monté en cache BuildKit : il est réutilisé d'un build à
# l'autre sans jamais alourdir l'image. `maven.test.skip` évite de compiler les
# tests (et donc de télécharger Testcontainers) : `mvn verify` s'en charge.
# Compile aussi les classes JAXB du SOAP.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -Dmaven.test.skip=true package

# --- Étape 2 : runtime (JRE 21 Alpine, utilisateur non-root) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
