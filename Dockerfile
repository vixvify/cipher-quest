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
    && java --module-path /out/javafx --add-modules javafx.controls,javafx.fxml,javafx.media -version

FROM scratch AS game-files
COPY --from=build /out/ /app/
COPY --from=terminal /usr/local/bin/node /usr/local/bin/node
COPY --from=terminal /app/terminal/ /app/terminal/
COPY docker/start-game.sh /app/

FROM eclipse-temurin:25-jre-noble AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends fonts-noto-core libasound2t64 libgl1 libgl1-mesa-dri libgtk-3-0t64 \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 10001 cipherquest \
    && install -d -o cipherquest -g cipherquest /home/cipherquest/.cipherquest
WORKDIR /app

FROM runtime AS browser
RUN apt-get update \
    && apt-get install -y --no-install-recommends novnc openbox websockify x11-utils x11vnc xvfb \
    && install -d -m 1777 /tmp/.X11-unix \
    && rm -rf /var/lib/apt/lists/*
COPY docker/start-browser.sh ./
COPY --from=game-files / /
RUN sed -i 's/\r$//' start-browser.sh start-game.sh
USER cipherquest
ENV DISPLAY=:99
EXPOSE 6080
CMD ["/bin/bash", "/app/start-browser.sh"]

FROM runtime AS desktop
COPY --from=game-files / /
RUN sed -i 's/\r$//' start-game.sh
USER cipherquest
CMD ["/bin/sh", "/app/start-game.sh"]
