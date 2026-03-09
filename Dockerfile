# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/coreHTTP-1.0-SNAPSHOT.jar app.jar
# Add static assets
COPY src/main/resources/public ./public

# ENV configuration
ENV COREHTTP_THREADS=20
ENV COREHTTP_QUEUE_CAPACITY=200
ENV COREHTTP_STATIC_DIR=/app/public

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
