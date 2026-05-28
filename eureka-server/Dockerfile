# Étape 1 : Construire le JAR avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le pom.xml parent d'abord (optimisation cache Docker)
COPY ../pom.xml /app/parent-pom.xml

# Copier ce service
COPY pom.xml .
COPY src ./src

# Construire en sautant les tests (les tests se font en CI)
RUN mvn clean package -DskipTests

# Étape 2 : Image finale légère
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR construit
COPY --from=build /app/target/*.jar app.jar

# Port exposé
EXPOSE 8761

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
