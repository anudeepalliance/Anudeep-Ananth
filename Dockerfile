# ==============================================================================
# ANUDEEP ANANTH PORTFOLIO — MULTI-STAGE DOCKER BUILD
# ==============================================================================
# JVM 21 Ktor fat JAR. Same Hostinger VPS pattern as PeekoPals: build on the
# box, run on the shared baedrop_default network behind shared-nginx.
# ==============================================================================

FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy AS builder

COPY . /app
WORKDIR /app

# Seed Gradle wrapper dist when present (VPS often times out on services.gradle.org).
# Path hash must match the wrapper for distributionUrl gradle-9.5.1-bin.zip.
RUN if [ -f /app/docker-build-cache/gradle-9.5.1-bin.zip ]; then \
      mkdir -p /root/.gradle/wrapper/dists/gradle-9.5.1-bin/iq79hdu3mqx29lgffhp8bfmx && \
      cp /app/docker-build-cache/gradle-9.5.1-bin.zip \
        /root/.gradle/wrapper/dists/gradle-9.5.1-bin/iq79hdu3mqx29lgffhp8bfmx/gradle-9.5.1-bin.zip && \
      touch /root/.gradle/wrapper/dists/gradle-9.5.1-bin/iq79hdu3mqx29lgffhp8bfmx/gradle-9.5.1-bin.zip.ok && \
      echo "Seeded Gradle 9.5.1 wrapper dist from docker-build-cache"; \
    else \
      echo "No offline Gradle seed — wrapper will download distributionUrl"; \
    fi

RUN chmod +x ./gradlew && \
    ./gradlew clean buildFatJar --no-daemon -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m"

FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar /app/anudeep-ananth.jar

RUN apt-get update && apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

ENV PORT=8484
EXPOSE 8484

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-XX:+UseG1GC", "-jar", "/app/anudeep-ananth.jar", "-P:ktor.deployment.port=8484"]
