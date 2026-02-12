# ================================
# Stage 1: Build the application
# ================================
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy build configuration first (better cache)
COPY build.gradle settings.gradle gradlew test-report-config.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build fat jar
RUN ./gradlew bootJar --no-daemon -x test


# ================================
# Stage 2: Runtime image
# ================================
FROM bellsoft/liberica-openjdk-alpine:17

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy application jar
COPY --from=build /app/build/libs/*.jar app.jar

# Expose app port (Railway uses PORT env)
EXPOSE 8080

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]