# --- Étape 1 : build (Java 21 + Maven) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances : on copie d'abord le pom
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Compilation et packaging
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Étape 2 : runtime (JRE 21 seul) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
