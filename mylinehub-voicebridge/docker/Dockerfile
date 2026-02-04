# ============================
# 1. Build stage
# ============================
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy the Maven project files first to leverage build caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw -q dependency:go-offline

# Copy source
COPY src src

# Build application
RUN ./mvnw -q -DskipTests package

# ============================
# 2. Runtime stage
# ============================
FROM eclipse-temurin:17-jre

# Create non-root user
RUN useradd -ms /bin/bash voicebridge

WORKDIR /opt/voicebridge

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Set permissions
RUN chown -R voicebridge:voicebridge /opt/voicebridge
USER voicebridge

# Expose ports (HTTPS/WSS, HTTP/WS, RTP)
EXPOSE 8082/tcp
EXPOSE 8083/tcp
EXPOSE 40000/udp

# JVM options can be overridden with:  docker run -e JAVA_OPTS="..."
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
