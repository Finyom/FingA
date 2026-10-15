
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Maven-Konfiguration und Quellcode kopieren
COPY pom.xml .
COPY src ./src

# Spring-Boot-Anwendung bauen
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Erstellte JAR-Datei kopieren
COPY --from=build /app/target/*.jar app.jar

# Port für Render
EXPOSE 8080

# Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]
