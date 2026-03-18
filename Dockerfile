# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy module POMs first — layer caches dependency downloads between source changes
COPY pom.xml .
COPY nioflow-framework/pom.xml nioflow-framework/
COPY task-planner-app/pom.xml task-planner-app/
RUN mvn dependency:go-offline -q

# Copy all source and build the fat JAR
COPY nioflow-framework/src nioflow-framework/src
COPY task-planner-app/src task-planner-app/src
RUN mvn package -DskipTests -pl task-planner-app -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/task-planner-app/target/task-planner-app-*-jar-with-dependencies.jar app.jar
# Copy static assets from the build stage
COPY --from=build /app/task-planner-app/target/classes/public ./public

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

# Create an unprivileged runtime user for defense-in-depth.
RUN useradd --system --uid 10001 --create-home appuser \
	&& chown -R appuser:appuser /app

# Production tuning defaults — sensitive vars (DB_PASS, JWT_SECRET) must be
# supplied at runtime via environment; they have no defaults here.
ENV NIOFLOW_THREADS=20
ENV NIOFLOW_QUEUE_CAPACITY=200
ENV NIOFLOW_STATIC_DIR=/app/public
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "app.jar"]
