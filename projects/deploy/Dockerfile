# Runs the Ktor server, which also serves the built web app.
# The server jar and the web bundle are both built on the Mac and copied in, so the VPS never runs Gradle.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY server/build/libs/server-all.jar /app/anudeep-projects.jar
COPY composeApp/build/dist/wasmJs/productionExecutable/ /app/web/

RUN useradd -m -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

ENV PORT=8585
EXPOSE 8585

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "/app/anudeep-projects.jar"]
