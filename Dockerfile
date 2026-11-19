FROM node:24-bookworm-slim AS terminal
WORKDIR /app/terminal
COPY terminal/package.json terminal/package-lock.json ./
RUN npm ci --omit=dev --omit=optional --ignore-scripts --no-audit --no-fund
COPY terminal/worker.mjs terminal/mission.mjs ./

FROM maven:3.9-eclipse-temurin-25-noble AS build
WORKDIR /build
COPY pom.xml ./
COPY src/main/ src/main/
RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true package \
        org.apache.maven.plugins:maven-dependency-plugin:3.9.0:copy-dependencies -DincludeScope=runtime \
    && mkdir -p /out/javafx /out/lib \
    && cp target/cipherquest-*.jar /out/cipherquest.jar \
    && for jar in target/dependency/*.jar; do \
        case "$jar" in \
            */javafx-*-linux*.jar) cp "$jar" /out/javafx/ ;; \
            */javafx-*.jar) ;; \
            *) cp "$jar" /out/lib/ ;; \
        esac; \
    done \
    && java --module-path /out/javafx --add-modules javafx.controls,javafx.fxml -version

FROM eclipse-temurin:25-jre-noble
RUN apt-get update \
    && apt-get install -y --no-install-recommends fonts-noto-core libgl1 libgl1-mesa-dri libgtk-3-0t64 \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 10001 cipherquest \
    && install -d -o cipherquest -g cipherquest /home/cipherquest/.cipherquest
WORKDIR /app
COPY --from=build /out/ ./
COPY --from=terminal /usr/local/bin/node /usr/local/bin/node
COPY --from=terminal /app/terminal/ ./terminal/
USER cipherquest
CMD ["java", "--module-path", "/app/javafx", "--add-modules", "javafx.controls,javafx.fxml", \
     "--enable-native-access=javafx.graphics,ALL-UNNAMED", "-cp", "/app/cipherquest.jar:/app/lib/*", "com.cipherquest.Main"]
