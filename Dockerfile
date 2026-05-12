# Multi-stage build for optimized image size
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S botuser && adduser -S botuser -G botuser

# Copy the JAR from build stage
COPY --from=build /app/target/discord-music-bot-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R botuser:botuser /app

# Switch to non-root user
USER botuser

# Health check (optional)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD pgrep -x java || exit 1

# Run the bot
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
