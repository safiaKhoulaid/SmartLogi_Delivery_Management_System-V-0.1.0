# `Dockerfile`
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copier uniquement les sources nécessaires pour utiliser le cache Maven
COPY pom.xml .
COPY src ./src

# Build non interactif, tests sautés
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
