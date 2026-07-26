# --- Étape 1 : build (Java 21 + Maven) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances : on copie d'abord le pom
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Compilation et packaging (génère aussi les classes JAXB du SOAP)
COPY src ./src
RUN mvn -q -B clean package -DskipTests

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
