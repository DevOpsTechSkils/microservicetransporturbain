# ─────────────────────────────────────────────────────────────────
# Le JAR est déjà compilé par le CI (mvn clean package).
# Ce Dockerfile copie simplement le JAR dans une image légère.
# ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR produit par Maven dans eureka-server/target/
COPY target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=5 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
