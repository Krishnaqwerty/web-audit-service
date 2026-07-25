# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper & pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and package application
COPY src/ src/
RUN ./mvnw clean package -DskipTests --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR artifact
COPY --from=builder /app/target/*.jar app.jar

# Enforce unprivileged user
USER appuser:appgroup

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -Dspring.threads.virtual.enabled=true"

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
