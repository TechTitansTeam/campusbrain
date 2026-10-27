# ==============================================================================
# Multi-stage Dockerfile for CampusBrain (Pragati University)
# Builds and runs on Render, Railway, Fly.io, AWS, or any Docker cloud host
# ==============================================================================

# Stage 1: Build JAR with Maven & Eclipse Temurin JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy project definition and source
COPY pom.xml .
COPY src ./src

# Build production executable JAR
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image with JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create persistent storage directories
RUN mkdir -p /app/data /app/uploads

# Copy built JAR from builder stage
COPY --from=builder /build/target/campusbrain-*.jar app.jar

# Dynamic port binding for cloud platforms
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
