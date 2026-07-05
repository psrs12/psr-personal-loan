# Multi-stage build for all Spring Boot services
# Usage: docker build --build-arg SERVICE_NAME=application-management-service -t <tag> -f deploy/Dockerfile .

ARG SERVICE_NAME

# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS builder

ARG SERVICE_NAME
WORKDIR /build

COPY services/${SERVICE_NAME}/pom.xml pom.xml
COPY services/${SERVICE_NAME}/src src

RUN apk add --no-cache maven && \
    mvn dependency:go-offline -q && \
    mvn package -Dmaven.test.skip=true -q

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine AS runtime

ARG SERVICE_NAME
ENV SERVICE_NAME=${SERVICE_NAME}

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
