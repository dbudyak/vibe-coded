# Multi-stage build for smaller image size
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties ./

# Download dependencies (cached if build files haven't changed)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN gradle buildFatJar --no-daemon

# Runtime stage - using amazoncorretto for better ARM64 support
FROM amazoncorretto:17-alpine

WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*-all.jar ./app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
