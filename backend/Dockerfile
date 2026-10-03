# =====================================================================
#  Image Docker du backend DriveHub (Spring Boot, Java 21)
#
#  Construction :  docker build -t drivehub-api ./backend
#  Lancement    :  docker run --env-file backend/.env -p 8082:8082 drivehub-api
#
#  Deux étapes : 1) compiler avec Maven ; 2) image finale légère (JRE seul, sans les sources).
# =====================================================================

# ---------- Étape 1 : compilation ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
# Les dépendances d'abord : elles restent en cache tant que pom.xml ne change pas (constructions plus rapides)
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src src
# Les tests tournent dans la CI (GitHub Actions), avec une base PostgreSQL : on ne les relance pas ici
RUN ./mvnw -B package -DskipTests && cp target/drivehub-*.jar app.jar

# ---------- Étape 2 : image d'exécution ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
# Utilisateur sans droits d'administration (bonne pratique de sécurité)
RUN groupadd --system drivehub && useradd --system --gid drivehub --home /app drivehub \
    && mkdir -p /app/data && chown -R drivehub:drivehub /app
COPY --from=build /workspace/app.jar app.jar
USER drivehub

ENV PORT=8082 \
    STORAGE_LOCAL_DIR=/app/data/documents \
    THYMELEAF_CACHE=true \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8082

# L'hébergeur (ou Docker) vérifie que l'application répond
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS "http://localhost:${PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
